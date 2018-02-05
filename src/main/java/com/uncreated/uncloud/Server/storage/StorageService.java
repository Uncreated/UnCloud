package com.uncreated.uncloud.Server.storage;

import com.uncreated.uncloud.Server.RequestException;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.uncreated.uncloud.Common.PART_SIZE;

public class StorageService
{
	private static final String ROOT_FOLDER = "C:/UnCloud/";

	public FolderNode getUserFiles(String login) throws RequestException
	{
		try
		{
			File userFolder = new File(ROOT_FOLDER + login);
			if (!userFolder.exists())
				userFolder.mkdir();

			return new FolderNode(userFolder);
		} catch (FileNotFoundException e)
		{
			throw new RequestException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public FileTransfer getFile(String login, String filePath, int part) throws RequestException
	{
		try
		{
			File file = new File(ROOT_FOLDER + login + filePath);
			if (!file.exists())
				throw new RequestException("Incorrect filename(" + filePath + ")");

			long shift = PART_SIZE * part;
			int size = (int) (file.length() - shift);
			if (size < 0)
				throw new RequestException("Incorrect part number");

			if (size > PART_SIZE)
				size = PART_SIZE;

			FileTransfer fileTransfer = new FileTransfer(filePath, part, size);
			fileTransfer.read(ROOT_FOLDER + login);

			return fileTransfer;

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
		File file = new File(ROOT_FOLDER + login + filePath);
		if (!file.exists())
			throw new RequestException("Incorrect filename");

		if (!file.delete())
			throw new RequestException("Internal error. Can not to delete file.", HttpStatus.INTERNAL_SERVER_ERROR);
	}


	public void setFile(String login, FileTransfer fileTransfer) throws RequestException
	{
		try
		{
			File file = new File(ROOT_FOLDER + login + fileTransfer.path);
			if (!file.exists())
				file.createNewFile();

			fileTransfer.write(file);

		} catch (IOException e)
		{
			throw new RequestException("Internal error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
