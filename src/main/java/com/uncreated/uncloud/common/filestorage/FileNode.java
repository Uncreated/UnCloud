package com.uncreated.uncloud.common.filestorage;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;

public class FileNode
		extends FNode
{
	private Long size;

	public FileNode()
	{
	}

	public FileNode(File file)
	{
		this(file.getName(), file.length());
	}

	public FileNode(String name, Long size)
	{
		super(name);
		this.size = size;
	}

	@Override
	public long getSize()
	{
		return size;
	}

	@JsonIgnore
	public int getParts()
	{
		return FileTransfer.getParts(size);
	}
}
