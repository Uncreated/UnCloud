package com.uncreated.uncloud.Common.FileStorage;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FNode
{
	protected String name;

	protected FolderNode parentFolder;

	protected boolean onClient;
	protected boolean onServer;

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

	@JsonIgnore
	public boolean isOnServer()
	{
		return onServer;
	}

	@JsonIgnore
	public boolean isOnClient()
	{
		return onClient;
	}

	public void setLoc(boolean onClient, boolean onServer)
	{
		this.onClient = onClient;
		this.onServer = onServer;
	}

	@JsonIgnore
	public FNode getFilePath()
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
		/*if (this instanceof FolderNode)
			builder.append("/");*/
		return new FNode(builder.toString());
	}
}
