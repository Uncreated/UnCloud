package com.uncreated.uncloud.common.filestorage;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class FNode
		implements Comparable<FNode>
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
	public String getFilePath()
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
		return builder.toString();
	}

	@Override
	public int compareTo(FNode o)
	{
		return name.toLowerCase().compareTo(o.name.toLowerCase());
	}
}
