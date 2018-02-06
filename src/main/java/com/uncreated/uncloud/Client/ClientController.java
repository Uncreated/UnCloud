package com.uncreated.uncloud.Client;

import com.uncreated.uncloud.Client.View.ClientView;
import com.uncreated.uncloud.Common.FileStorage.FNode;
import com.uncreated.uncloud.Common.FileStorage.FileNode;
import com.uncreated.uncloud.Common.FileStorage.FolderNode;
import com.uncreated.uncloud.Common.FileStorage.Storage;
import com.uncreated.uncloud.Server.storage.FileTransfer;
import javafx.application.Platform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ClientController
{
	private static final String ROOT_FOLDER = "C:/UnCloud/Client/";

	private Storage storage;

	private RequestHandler requestHandler;
	private ClientView clientView;

	private FolderNode mergedFolder;
	private String login;

	public ClientController(ClientView clientView)
	{
		this.clientView = clientView;
		requestHandler = new RequestHandler();
		storage = new Storage(ROOT_FOLDER);
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
			Platform.runLater(() ->
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
			} catch (FileNotFoundException e)
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
			RequestStatus<FolderNode> requestStatus = requestHandler.auth(login, password);
			if (requestStatus.isOk())
			{
				this.login = login;
				requestStatus = getMergedFolder();
			}
			final RequestStatus<FolderNode> reqStatus = requestStatus;
			Platform.runLater(() ->
			{
				clientView.onAuth(reqStatus);
				if (reqStatus.isOk())
				{
					clientView.onUpdateFiles(mergedFolder);
				}
			});
		});
	}

	private RequestStatus<FNode> getFile(FNode fnode, int parts)
	{
		RequestStatus<FNode> requestStatus = null;
		for (int i = 0; i < parts; i++)
		{
			RequestStatus<FileTransfer> requestStatusPart = requestHandler.getFile(fnode, i);
			if (!requestStatusPart.isOk())
			{
				new File(requestStatusPart.getData().getPath()).delete();
				requestStatus = new RequestStatus<>(false, "File download has been interrupted");
				break;
			}
			try
			{
				requestStatusPart.getData().write(ROOT_FOLDER);
			} catch (IOException e)
			{
				requestStatus = new RequestStatus<>(false, "Can not write file");
				break;
			}
		}
		return requestStatus != null ? requestStatus : new RequestStatus(true);
	}

	public void getFile(FileNode fileNode)
	{
		runThread(() ->
		{
			RequestStatus<FNode> requestStatus = getFile(fileNode.getFilePath(), FileTransfer.getParts(fileNode.getSize()));
			if (requestStatus.isOk())
			{
				requestStatus.setData(fileNode);

			}
			Platform.runLater(() ->
			{
				clientView.onGetFileResponse(requestStatus);
			});
		});
	}

	private RequestStatus<FileNode> setFile(File file, FileNode fileNode)
	{
		RequestStatus<FileNode> requestStatus = null;

		String path = fileNode.getFilePath().getName();
		int szi = FileTransfer.getParts(fileNode.getSize());
		int n = 5;
		for (int i = 0; i < szi; i++)
		{
			FileTransfer fileTransfer = new FileTransfer(path, i, FileTransfer.getSizeOfPart(fileNode.getSize(), i));
			try
			{
				fileTransfer.read(file);
			} catch (IOException e)
			{
				requestStatus = new RequestStatus<>(false, "Can not read file");
				break;
			}
			RequestStatus responseStatus = requestHandler.setFile(fileTransfer);
			if (!responseStatus.isOk())
				n--;
			if (n < 0)
			{
				requestStatus = new RequestStatus<>(false, "File upload has been interrupted");
				break;
			}
		}
		return requestStatus != null ? requestStatus : new RequestStatus(true);
	}

	public void setFile(File file, FolderNode curFolder)
	{
		runThread(() ->
		{
			RequestStatus<FileNode> requestStatus;
			if (file.exists())
			{
				FileNode fileNode = new FileNode(file);
				fileNode.setParentFolder(curFolder);
				requestStatus = setFile(file, fileNode).setData(fileNode);
				if (requestStatus.isOk())
				{
					curFolder.add(fileNode);
					curFolder.initRelations();
					requestStatus.setData(fileNode);
				}
			} else
				requestStatus = new RequestStatus<>(false, "File not found");
			Platform.runLater(() ->
			{
				clientView.onSetFileResponse(requestStatus);
			});
		});
	}


	public void removeFileFromClient(FNode fNode)
	{
		try
		{
			storage.removeFile(login, fNode.getFilePath().getName());
			folderUpdateRequestResult(getMergedFolder());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void removeFile(FNode fNode)
	{
		if (fNode.isOnClient())
			removeFileFromClient(fNode);
		if (fNode.isOnServer())
			removeFileFromServer(fNode);
	}

	public void removeFileFromServer(FNode fNode)
	{
		FNode fNodePack = fNode.getFilePath();
		runThread(() ->
		{
			RequestStatus requestStatus = requestHandler.removeFile(fNodePack);
			if (requestStatus.isOk())
				requestStatus = getMergedFolder();

			folderUpdateRequestResult(requestStatus);
		});
	}

	public void createFolder(String name, FolderNode curFolder)
	{
		runThread(() ->
		{
			FNode fNode = new FNode(curFolder.getFilePath().getName() + name);
			RequestStatus requestStatus = requestHandler.createFolder(fNode);
			if (requestStatus.isOk())
				requestStatus = getMergedFolder();

			folderUpdateRequestResult(requestStatus);
		});
	}

	private void folderUpdateRequestResult(RequestStatus requestStatus)
	{
		Platform.runLater(() ->
		{
			if (requestStatus.isOk())
				clientView.onUpdateFiles(mergedFolder);
			else
				clientView.onFailRequest(requestStatus);
		});
	}
}
