package com.uncreated.uncloud.Server.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;

public class FileNode extends FNode
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

	public Long getSize()
	{
		return size;
	}

	@JsonIgnore
	public FileNode getFilePath()
	{
		StringBuilder builder = new StringBuilder(name);
		builder.insert(0, "/");
		FolderNode parent = parentFolder;
		while (parent.getParentFolder() != null)
		{
			builder.insert(0, parent.name);
			builder.insert(0, "/");
			parent = parent.getParentFolder();
		}
		return new FileNode(builder.toString(), size);
	}
}
