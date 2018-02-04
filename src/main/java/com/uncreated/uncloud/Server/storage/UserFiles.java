package com.uncreated.uncloud.Server.storage;

import com.uncreated.uncloud.Server.Answer;

import java.util.ArrayList;
import java.util.Arrays;

public class UserFiles implements Answer
{
	FileInfo[] files;

	public UserFiles()
	{
	}

	public UserFiles(FileInfo[] files)
	{
		this.files = files;
	}

	public UserFiles(ArrayList<FileInfo> files)
	{
		this.files = files.toArray(new FileInfo[files.size()]);
	}

	public FileInfo[] getFiles()
	{
		return files;
	}

	public ArrayList<String> getFilesFromDirectory(String path)
	{
		ArrayList<String> names = new ArrayList<>();
		for (FileInfo fileInfo : files)
		{
			if (fileInfo.path.length() > path.length() && fileInfo.path.startsWith(path))
			{
				String name = fileInfo.path.substring(path.length());
				if (name.contains("/"))
					name = name.substring(0, name.indexOf('/') + 1);
				if (!names.contains(name))
					names.add(name);
			}
		}
		return names;
	}

	public FileInfo getFileInfo(String path)
	{
		for (FileInfo fileInfo : files)
			if (fileInfo.path.equals(path))
				return fileInfo;

		return null;
	}

	public void remove(FileInfo fileInfo)
	{
		ArrayList<FileInfo> list = new ArrayList<>(Arrays.asList(files));
		list.remove(fileInfo);
		files = new FileInfo[list.size()];
		list.toArray(files);
	}
}
