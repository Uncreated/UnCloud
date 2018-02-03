package com.uncreated.uncloud.Server.storage;

import com.uncreated.uncloud.Server.Answer;

import java.util.ArrayList;

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
}
