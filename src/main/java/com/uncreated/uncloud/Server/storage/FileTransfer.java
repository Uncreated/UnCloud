package com.uncreated.uncloud.Server.storage;

import com.uncreated.uncloud.Server.Answer;

public class FileTransfer implements Answer
{
	String path;
	Integer part;
	byte[] data;

	public FileTransfer()
	{
	}

	public FileTransfer(String path, Integer part, int size)
	{
		this.path = path;
		this.part = part;
		this.data = new byte[size];
	}

	public String getPath()
	{
		return path;
	}

	public Integer getPart()
	{
		return part;
	}

	public byte[] getData()
	{
		return data;
	}
}
