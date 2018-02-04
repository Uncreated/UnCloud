package com.uncreated.uncloud.Server.storage;

import java.io.File;

public class FileInfo
{
	String path;
	Long size;

	public FileInfo()
	{
	}

	public FileInfo(String path, Long size)
	{
		this.path = path;
		this.size = size;
	}

	public String getPath()
	{
		return path;
	}

	public Long getSize()
	{
		return size;
	}

	public String getName()
	{
		return (new File(path)).getName();
	}
}
