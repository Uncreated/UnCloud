package com.uncreated.uncloud.Server.storage;

import com.uncreated.uncloud.Common.FileStorage.FileTransfer;
import com.uncreated.uncloud.Common.FileStorage.FolderNode;
import com.uncreated.uncloud.Common.FileStorage.Storage;
import com.uncreated.uncloud.Server.RequestException;
import org.springframework.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;

public class StorageService
{
	private static final String ROOT_FOLDER = "C:/UnCloud/Server/";

	private Storage storage;

	public StorageService()
	{
		storage = new Storage(ROOT_FOLDER);
	}

	public FolderNode getFiles(String login) throws RequestException
	{
		try
		{
			return storage.getFiles(login);
		} catch (FileNotFoundException e)
		{
			throw new RequestException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public FileTransfer getFilePart(String login, String filePath, int part) throws RequestException
	{
		try
		{
			return storage.getFilePart(login, filePath, part);
		} catch (FileNotFoundException e)
		{
			throw new RequestException("Incorrect filename(" + filePath + ")");
		} catch (IOException e)
		{
			throw new RequestException("Internal error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void removeFile(String login, String filePath) throws RequestException
	{
		try
		{
			if (!storage.removeFile(login, filePath))
				throw new RequestException("Internal error. Can not to delete file.", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (IOException e)
		{
			throw new RequestException("File not found" + filePath);
		}
	}

	public void setFile(String login, FileTransfer fileTransfer) throws RequestException
	{
		try
		{
			storage.setFilePart(login, fileTransfer);
		} catch (IOException e)
		{
			throw new RequestException("Internal error. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void createFolder(String login, String path) throws RequestException
	{
		try
		{
			storage.createFolder(login, path);
		} catch (IOException e)
		{
			throw new RequestException("Internal error. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
