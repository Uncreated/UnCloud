package com.uncreated.uncloud.Client;

import com.uncreated.uncloud.Client.View.ClientView;
import com.uncreated.uncloud.Server.storage.FileNode;
import com.uncreated.uncloud.Server.storage.FileTransfer;
import com.uncreated.uncloud.Server.storage.FolderNode;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;

public class ClientController
{
	private static final String ROOT_FOLDER = "C:/localFiles";

	private RequestHandler requestHandler;
	private ClientView clientView;

	private FolderNode rootFolder;

	public ClientController(ClientView clientView)
	{
		this.clientView = clientView;
		requestHandler = new RequestHandler();
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

	public void auth(String login, String password)
	{
		runThread(() ->
		{
			RequestStatus requestStatus = requestHandler.auth(login, password);
			Platform.runLater(() ->
			{
				clientView.onAuth(requestStatus);
			});
		});
	}

	public void userFiles()
	{
		runThread(() ->
		{
			RequestStatus<FolderNode> requestStatus = requestHandler.files();
			if (requestStatus.isOk())
			{
				this.rootFolder = requestStatus.getData();
				this.rootFolder.initRelations();
			}

			Platform.runLater(() ->
			{
				clientView.onUserFiles(requestStatus);
			});
		});
	}

	private RequestStatus<FileNode> getFile(FileNode fileNode, int parts)
	{
		RequestStatus<FileNode> requestStatus = null;
		for (int i = 0; i < parts; i++)
		{
			RequestStatus<FileTransfer> requestStatusPart = requestHandler.getFile(fileNode, i);
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
			RequestStatus<FileNode> requestStatus = getFile(fileNode.getFilePath(), FileTransfer.getParts(fileNode.getSize()));
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

	public void removeFile(FileNode fileNode)
	{
		runThread(() ->
		{
			RequestStatus<FileNode> requestStatus = requestHandler.removeFile(fileNode.getFilePath());
			if (requestStatus.isOk())
			{
				fileNode.getParentFolder().getFiles().remove(fileNode);
				requestStatus.setData(fileNode);
			}
			Platform.runLater(() ->
			{
				clientView.onRemoveFileResponse(requestStatus);
			});
		});
	}

	public FolderNode getRootFolder()
	{
		return rootFolder;
	}
}
