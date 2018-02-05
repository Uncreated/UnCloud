package com.uncreated.uncloud.Client;

import com.uncreated.uncloud.Client.View.ClientView;
import com.uncreated.uncloud.Server.storage.FileInfo;
import com.uncreated.uncloud.Server.storage.FileTransfer;
import com.uncreated.uncloud.Server.storage.UserFiles;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ClientController
{
	private static final String ROOT_FOLDER = "C:/localFiles";

	private RequestHandler requestHandler;
	private ClientView clientView;

	private UserFiles userFiles;
	private String path = "/";
	private ArrayList<String> curFiles;
	private String selectedFile;

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
			unselectFile();
			RequestStatus<UserFiles> requestStatus = requestHandler.files();
			if (requestStatus.isOk())
				this.userFiles = requestStatus.getData();

			Platform.runLater(() ->
			{
				clientView.onUserFiles(requestStatus);
			});
		});
	}

	private RequestStatus writeFile(FileInfo fileInfo)
	{
		RequestStatus requestStatus = null;
		int szi = FileTransfer.getParts(fileInfo.getSize());
		for (int i = 0; i < szi; i++)
		{
			RequestStatus<FileTransfer> responseStatus = requestHandler.getFile(fileInfo.getPath(), 0);
			if (!responseStatus.isOk())
			{
				new File(responseStatus.getData().getPath()).delete();
				requestStatus = new RequestStatus(false, "File download has been interrupted");
				break;
			}
			try
			{
				responseStatus.getData().write(ROOT_FOLDER);
			} catch (IOException e)
			{
				requestStatus = new RequestStatus(false, "Can not write file");
				break;
			}
		}
		return requestStatus != null ? requestStatus : new RequestStatus(true);
	}

	public void writeFile()
	{
		runThread(() ->
		{
			RequestStatus requestStatus;
			if (selectedFile != null)
			{
				FileInfo fileInfo = userFiles.getFileInfo(path + selectedFile);
				if (fileInfo != null)
					requestStatus = writeFile(fileInfo);
				else
					requestStatus = new RequestStatus(false, "File not found");
			} else
				requestStatus = new RequestStatus(false, "File not selected");
			Platform.runLater(() ->
			{
				clientView.onGetFileResponse(requestStatus);
			});
		});
	}

	private RequestStatus readFile(File file)
	{
		RequestStatus requestStatus = null;

		FileInfo fileInfo = new FileInfo(path + file.getName(), file.length());
		int szi = FileTransfer.getParts(fileInfo.getSize());
		int n = 5;
		for (int i = 0; i < szi; i++)
		{
			FileTransfer fileTransfer = new FileTransfer(fileInfo.getPath(), i, FileTransfer.getSizeOfPart(fileInfo.getSize(), i));
			try
			{
				fileTransfer.read(file);
			} catch (IOException e)
			{
				requestStatus = new RequestStatus(false, "Can not read file");
				break;
			}
			RequestStatus responseStatus = requestHandler.setFile(fileTransfer);
			if (!responseStatus.isOk())
				n--;
			if (n < 0)
			{
				requestStatus = new RequestStatus(false, "File upload has been interrupted");
				break;
			}
		}
		return requestStatus != null ? requestStatus : new RequestStatus(true);
	}

	public void setFile(File file)
	{
		runThread(() ->
		{
			RequestStatus requestStatus;
			if (file.exists())
			{
				requestStatus = readFile(file);
			} else
				requestStatus = new RequestStatus(false, "File not found");
			Platform.runLater(() ->
			{
				clientView.onSetFileResponse(requestStatus);
			});
		});
	}

	public void removeFile()
	{
		runThread(() ->
		{
			RequestStatus requestStatus;
			String name = selectedFile;
			if (selectedFile != null)
			{
				FileInfo fileInfo = userFiles.getFileInfo(path + selectedFile);
				if (fileInfo != null)
				{
					requestStatus = requestHandler.removeFile(fileInfo.getPath());
					unselectFile();
					curFiles.remove(name);
					userFiles.remove(fileInfo);
				} else
					requestStatus = new RequestStatus(false, "File not found");
			} else
				requestStatus = new RequestStatus(false, "File not selected");
			Platform.runLater(() ->
			{
				clientView.onRemoveFileResponse(requestStatus);
				clientView.onFolderOpen(curFiles, path.equals("/"));
			});
		});
	}

	public void goBack()
	{
		unselectFile();
		if (path.length() > 1)
		{
			path = path.substring(0, path.lastIndexOf('/'));
			path = path.substring(0, path.lastIndexOf('/') + 1);
		}
		curFiles = userFiles.getFilesFromDirectory(path);
		clientView.onFolderOpen(curFiles, path.equals("/"));
	}

	private void goToFolder(String folderName)
	{
		unselectFile();
		if (curFiles != null)
			for (String file : curFiles)
				if (file.contains(folderName))
				{
					path += folderName;
					break;
				}
		curFiles = userFiles.getFilesFromDirectory(path);
		clientView.onFolderOpen(curFiles, path.equals("/"));
	}

	private void unselectFile()
	{
		selectedFile = null;
		clientView.onFileUnselected();
	}

	public void fileClick(String name)
	{
		if (name.endsWith("/"))
			goToFolder(name);
		else
		{
			selectedFile = name;
			clientView.onFileSelected();
		}
	}
}
