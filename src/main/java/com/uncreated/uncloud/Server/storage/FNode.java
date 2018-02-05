package com.uncreated.uncloud.Server.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class FNode
{
	protected String name;

	protected FolderNode parentFolder;

	public FNode()
	{
	}

	public FNode(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@JsonIgnore
	public FolderNode getParentFolder()
	{
		return parentFolder;
	}

	public void setParentFolder(FolderNode parentFolder)
	{
		this.parentFolder = parentFolder;
	}
}
