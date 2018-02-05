package com.uncreated.uncloud.Server.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class FolderNode extends FNode
{
	private ArrayList<FolderNode> folders;
	private ArrayList<FileNode> files;

	public FolderNode()
	{
	}

	public FolderNode(String name)
	{
		super(name);
		folders = new ArrayList<>();
		files = new ArrayList<>();
	}

	public FolderNode(File rootFolder) throws FileNotFoundException
	{
		this(rootFolder.getName());

		if (!rootFolder.exists() || !rootFolder.isDirectory())
			throw new FileNotFoundException();

		File[] subFiles = rootFolder.listFiles();
		for (File file : subFiles)
		{
			if (file.isDirectory())
				folders.add(new FolderNode(file));
			else
				files.add(new FileNode(file));
		}
	}

	public void add(FolderNode folder)
	{
		folders.add(folder);
	}

	public void add(FileNode file)
	{
		files.add(file);
	}

	public ArrayList<FolderNode> getFolders()
	{
		return folders;
	}

	public ArrayList<FileNode> getFiles()
	{
		return files;
	}

	public void initRelations()
	{
		for (FileNode fileNode : files)
			fileNode.parentFolder = this;
		for (FolderNode folderNode : folders)
		{
			folderNode.parentFolder = this;
			folderNode.initRelations();
		}
	}
}
