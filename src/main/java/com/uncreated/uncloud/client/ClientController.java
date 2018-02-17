package com.uncreated.uncloud.client;

import com.uncreated.uncloud.client.view.ClientView;
import com.uncreated.uncloud.common.filestorage.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class ClientController
{
	private Storage storage;

	private RequestHandler requestHandler;
	private ClientView clientView;

	private FolderNode mergedFolder;
	private String login;

	private String rootFolder;

	public ClientController(String rootFolder)
	{
		requestHandler = new RequestHandler();
		storage = new Storage(rootFolder);
		this.rootFolder = rootFolder;
	}

	public void setClientView(ClientView clientView)
	{
		this.clientView = clientView;
	}

	private void runThread(Runnable r)
	{
		new Thread(r).start();
	}

	public void register(String login, String password)
	{
		runThread(() ->
		{
			RequestStatus requestStatus = requestHandler.register(login, password);
			call(() ->
			{
				clientView.onRegister(requestStatus);
			});
		});
	}

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

	public void auth(String login, String password)
	{
		runThread(() ->
		{
			RequestStatus requestStatus = requestHandler.auth(login, password);
			call(() ->
			{
				clientView.onAuth(requestStatus);
			});

			if (requestStatus.isOk())
			{
				this.login = login;
				//folderUpdateRequestResult(requestStatus);
			}
		});
	}

	public void updateFiles()
	{
		runThread(() ->
		{
			folderUpdateRequestResult(new RequestStatus(true));
		});
	}

	public void copyFile(File source, FolderNode curNode)
	{
		runThread(() ->
		{
			RequestStatus requestStatus;
			try
			{
				File dest = new File(rootFolder + login + curNode.getFilePath() + source.getName());
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
			folderUpdateRequestResult(requestStatus);
		});
	}

	private RequestStatus downloadFile(FileNode fileNode)
	{
		RequestStatus requestStatus = null;
		String path = fileNode.getFilePath();
		int parts = fileNode.getParts();
		for (int i = 0; i < parts; i++)
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

	public void download(FNode fNode)
	{
		runThread(() ->
		{
			RequestStatus requestStatus;
			if (fNode instanceof FolderNode)
			{
				requestStatus = downloadFolder((FolderNode) fNode);
			}
			else
			{
				requestStatus = downloadFile((FileNode) fNode);
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
		for (int i = 0; i < szi; i++)
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

	public void upload(FNode fNode)
	{
		runThread(() ->
		{
			RequestStatus requestStatus;
			if (fNode instanceof FolderNode)
			{
				requestStatus = uploadFolder((FolderNode) fNode);
			}
			else
			{
				requestStatus = uploadFile((FileNode) fNode);
			}

			folderUpdateRequestResult(requestStatus);
		});
	}

	public void removeFileFromClient(FNode fNode)
	{
		RequestStatus requestStatus;
		try
		{
			storage.removeFile(login, fNode.getFilePath());
			requestStatus = new RequestStatus(true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			requestStatus = new RequestStatus(false, e.getMessage());
		}
		folderUpdateRequestResult(requestStatus);
	}

	public void removeFileFromServer(FNode fNode)
	{
		runThread(() ->
		{
			folderUpdateRequestResult(requestHandler.removeFile(fNode.getFilePath()));
		});
	}

	public void createFolder(String name, FolderNode curFolder)
	{
		runThread(() ->
		{
			folderUpdateRequestResult(requestHandler.createFolder(curFolder.getFilePath() + name));
		});
	}

	private void folderUpdateRequestResult(RequestStatus requestStatus)
	{
		if (requestStatus.isOk())
		{
			requestStatus = getMergedFolder();
		}

		RequestStatus reqStatus = requestStatus;
		call(() ->
		{
			if (reqStatus.isOk())
			{
				clientView.onUpdateFiles(mergedFolder);
			}
			else
			{
				clientView.onFailRequest(reqStatus);
			}
		});
	}

	public void logout()
	{
		this.clientView.onLogout();
		this.clientView = null;
	}

	private void call(Runnable runnable)
	{
		if (clientView != null)
		{
			clientView.call(runnable);
		}
	}
}