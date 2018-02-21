package com.uncreated.uncloud.client.files;

import com.uncreated.uncloud.client.Controller;
import com.uncreated.uncloud.client.files.view.FilesView;
import com.uncreated.uncloud.client.requests.RequestHandler;
import com.uncreated.uncloud.client.requests.RequestStatus;
import com.uncreated.uncloud.common.filestorage.FileNode;
import com.uncreated.uncloud.common.filestorage.FileTransfer;
import com.uncreated.uncloud.common.filestorage.FolderNode;
import com.uncreated.uncloud.common.filestorage.Storage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilesController
		extends Controller<FilesView>
{
	private Storage storage;
	private FolderNode mergedFolder;
	private String login;
	private String rootFolder;

	private FolderNode curFolder;

	private boolean firstRequest = true;

	public FilesController(RequestHandler requestHandler, String rootFolder)
	{
		super(requestHandler);

		storage = new Storage(rootFolder);
		this.rootFolder = rootFolder;
	}

	public void setLogin(String login)
	{
		this.login = login;
	}

	@Override
	public synchronized void onAttach(FilesView filesView)
	{
		super.onAttach(filesView);

		if (firstRequest)
		{
			updateFiles();
			firstRequest = false;
		}
	}

	public void updateFiles()
	{
		runThread(() ->
		{
			folderUpdateRequestResult(new RequestStatus(true));
		});
	}

	private <T extends FileNode> T getFileNode(FileInfo fileInfo)
	{
		if (fileInfo.isDirectory())
		{
			for (FolderNode folderNode : curFolder.getFolders())
			{
				if (folderNode.getName().equals(fileInfo.getName()))
				{
					return (T) folderNode;
				}
			}
		}
		else
		{
			for (FileNode fileNode : curFolder.getFiles())
			{
				if (fileNode.getName().equals(fileInfo.getName()))
				{
					return (T) fileNode;
				}
			}
		}
		return null;
	}

	public void openFolder(FileInfo fileInfo)
	{
		if (fileInfo == null)
		{
			if (curFolder.getParentFolder() != null)
			{
				curFolder = curFolder.getParentFolder();
				sendFileInfo(curFolder);

			}
		}
		else if (fileInfo.isDirectory())
		{
			FolderNode folderNode = getFileNode(fileInfo);
			if (folderNode != null)
			{
				curFolder = folderNode;
				sendFileInfo(curFolder);
			}
		}
	}

	private void sendFileInfo(FolderNode folderNode)
	{
		ArrayList<FileInfo> files = new ArrayList<>();
		for (FolderNode folder : folderNode.getFolders())
		{
			files.add(new FileInfo(folder));
		}
		for (FileNode file : folderNode.getFiles())
		{
			files.add(new FileInfo(file));
		}

		view.showFolder(files, folderNode.getParentFolder() == null);
	}

	private void folderUpdateRequestResult(RequestStatus requestStatus)
	{
		if (requestStatus.isOk())
		{
			requestStatus = getMergedFolder();
			if (requestStatus.isOk())
			{
				curFolder = mergedFolder.goTo(curFolder != null ? curFolder.getFilePath() : "/");
				mergedFolder.sort();
			}
		}

		RequestStatus reqStatus = requestStatus;
		call(() ->
		{
			if (reqStatus.isOk())
			{
				sendFileInfo(curFolder);
			}
			else
			{
				view.onFailRequest(reqStatus.getMsg());
			}
		});
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////OLD

	private RequestStatus getMergedFolder()
	{
		RequestStatus<FolderNode> requestStatus = requestHandler.files();
		if (requestStatus.isOk())
		{
			FolderNode serverFolder = requestStatus.getData();
			try
			{
				FolderNode clientFolder = storage.getFiles(login);
				mergedFolder = new FolderNode(clientFolder, serverFolder);
			}
			catch (FileNotFoundException e)
			{
				requestStatus = new RequestStatus<>(false, "Local folder not found");
			}
		}
		return requestStatus;
	}

	public void copyFile(List<File> fileList)
	{
		if (fileList != null && fileList.size() > 0)
		{
			copyFile(fileList.toArray(new File[fileList.size()]));
		}
	}

	public void copyFile(File... files)
	{
		runThread(() ->
		{
			RequestStatus requestStatus = null;
			for (File source : files)
			{
				try
				{
					File dest = new File(rootFolder + login + curFolder.getFilePath() + source.getName());
					dest.getParentFile().mkdirs();
					if (source.isDirectory())
					{
						FileUtils.copyDirectory(source, dest);
					}
					else
					{
						FileUtils.copyFile(source, dest);
					}
					requestStatus = new RequestStatus(true);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					requestStatus = new RequestStatus(false, e.getMessage());
				}
			}
			folderUpdateRequestResult(requestStatus);
		});
	}

	private RequestStatus downloadFile(FileNode fileNode)
	{
		RequestStatus requestStatus = null;
		String path = fileNode.getFilePath();
		int parts = fileNode.getParts();
		for (int i = 0; i < parts || i == 0; i++)
		{
			RequestStatus<FileTransfer> requestStatusPart = requestHandler.downloadFilePart(path, i);
			if (requestStatusPart.isOk())
			{
				try
				{
					requestStatusPart.getData().write(rootFolder + login);
					continue;
				}
				catch (IOException e)
				{
					requestStatus = new RequestStatus(false, "Can not write file");
					break;
				}
			}
			new File(requestStatusPart.getData().getPath()).delete();
			requestStatus = new RequestStatus(false, "File download has been interrupted");
			break;
		}
		return requestStatus != null ? requestStatus : new RequestStatus(true);
	}

	private RequestStatus downloadFolder(FolderNode folderNode)
	{
		RequestStatus requestStatus = null;
		if (!folderNode.isOnClient())
		{
			new File(rootFolder + login + folderNode.getFilePath()).mkdirs();
		}
		for (FolderNode folder : folderNode.getFolders())
		{
			requestStatus = downloadFolder(folder);
			if (!requestStatus.isOk())
			{
				return requestStatus;
			}
		}

		for (FileNode fileNode : folderNode.getFiles())
		{
			if (!fileNode.isOnClient())
			{
				requestStatus = downloadFile(fileNode);
				if (!requestStatus.isOk())
				{
					return requestStatus;
				}
			}
		}

		return requestStatus != null ? requestStatus : new RequestStatus(true);
	}

	public void download(FileInfo fileInfo)
	{
		runThread(() ->
		{
			RequestStatus requestStatus;

			if (fileInfo.isDirectory())
			{
				requestStatus = downloadFolder(getFileNode(fileInfo));
			}
			else
			{
				requestStatus = downloadFile(getFileNode(fileInfo));
			}

			folderUpdateRequestResult(requestStatus);
		});
	}

	private RequestStatus uploadFile(FileNode fileNode)
	{
		RequestStatus requestStatus = null;

		String path = fileNode.getFilePath();
		int szi = fileNode.getParts();
		File file = new File(rootFolder + login + path);
		for (int i = 0; i < szi || i == 0; i++)
		{
			FileTransfer fileTransfer = new FileTransfer(path, i, FileTransfer.getSizeOfPart(fileNode.getSize(), i));
			try
			{
				fileTransfer.read(file);
				RequestStatus responseStatus = requestHandler.setFile(fileTransfer);
				if (!responseStatus.isOk())
				{
					requestStatus = new RequestStatus(false, "File upload has been interrupted");
					break;
				}

			}
			catch (IOException e)
			{
				requestStatus = new RequestStatus(false, "Can not read file");
				break;
			}
		}
		return requestStatus != null ? requestStatus : new RequestStatus(true);
	}

	private RequestStatus uploadFolder(FolderNode folderNode)
	{
		RequestStatus requestStatus = null;

		if (!folderNode.isOnServer())
		{
			requestStatus = requestHandler.createFolder(folderNode.getFilePath());
		}
		for (FolderNode folder : folderNode.getFolders())
		{
			requestStatus = uploadFolder(folder);
			if (!requestStatus.isOk())
			{
				return requestStatus;
			}
		}

		for (FileNode fileNode : folderNode.getFiles())
		{
			if (!fileNode.isOnServer())
			{
				requestStatus = uploadFile(fileNode);
				if (!requestStatus.isOk())
				{
					return requestStatus;
				}
			}
		}

		return requestStatus != null ? requestStatus : new RequestStatus(true);
	}

	public void upload(FileInfo fileInfo)
	{
		runThread(() ->
		{
			RequestStatus requestStatus;
			if (fileInfo.isDirectory())
			{
				requestStatus = uploadFolder(getFileNode(fileInfo));
			}
			else
			{
				requestStatus = uploadFile(getFileNode(fileInfo));
			}

			folderUpdateRequestResult(requestStatus);
		});
	}

	public void removeFileFromClient(FileInfo fileInfo)
	{
		runThread(() ->
		{
			RequestStatus requestStatus;
			try
			{
				FileNode fNode = getFileNode(fileInfo);
				storage.removeFile(login, fNode.getFilePath());
				requestStatus = new RequestStatus(true);
			}
			catch (IOException | NullPointerException e)
			{
				e.printStackTrace();
				requestStatus = new RequestStatus(false, e.getMessage());
			}
			folderUpdateRequestResult(requestStatus);
		});
	}

	public void removeFileFromServer(FileInfo fileInfo)
	{
		runThread(() ->
		{
			FileNode fileNode = getFileNode(fileInfo);
			if (fileNode != null)
			{
				folderUpdateRequestResult(requestHandler.removeFile(fileNode.getFilePath()));
			}
		});
	}

	public void createFolder(String name)
	{
		runThread(() ->
		{
			folderUpdateRequestResult(requestHandler.createFolder(curFolder.getFilePath() + name));
		});
	}
}
