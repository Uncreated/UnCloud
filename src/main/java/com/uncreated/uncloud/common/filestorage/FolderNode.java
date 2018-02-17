package com.uncreated.uncloud.common.filestorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

public class FolderNode
		extends FNode
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
		{
			throw new FileNotFoundException();
		}

		File[] subFiles = rootFolder.listFiles();
		if (subFiles != null)
		{
			for (File file : subFiles)
			{
				if (file.isDirectory())
				{
					folders.add(new FolderNode(file));
				}
				else
				{
					files.add(new FileNode(file));
				}
			}
		}
	}

	public FolderNode(FolderNode clientFiles, FolderNode serverFiles)//merged folder
	{
		this(clientFiles.name);
		clientFiles.setLoc(true, false);
		serverFiles.setLoc(false, true);

		for (FileNode clientFile : clientFiles.files)
		{
			files.add(clientFile);
			for (FileNode serverFile : serverFiles.files)
			{
				if (clientFile.name.equals(serverFile.name))
				{
					clientFile.setLoc(true, true);
					serverFile.setLoc(true, true);
					break;
				}
			}
		}

		for (FileNode serverFile : serverFiles.files)
		{
			if (!serverFile.isOnClient())
			{
				files.add(serverFile);
			}
		}


		for (FolderNode clientFolder : clientFiles.folders)
		{
			for (FolderNode serverFolder : serverFiles.folders)
			{
				if (clientFolder.name.equals(serverFolder.name))
				{
					FolderNode subMergedFolder = new FolderNode(clientFolder, serverFolder);
					subMergedFolder.onClient = true;
					subMergedFolder.onServer = true;
					clientFolder.onClient = true;
					clientFolder.onServer = true;
					serverFolder.onClient = true;
					serverFolder.onServer = true;
					folders.add(subMergedFolder);
					break;
				}
			}
			if (!clientFolder.isOnServer())
			{
				folders.add(clientFolder);
			}
		}

		for (FolderNode serverFolder : serverFiles.folders)
		{
			if (!serverFolder.isOnClient())
			{
				folders.add(serverFolder);
			}
		}

		initRelations();
	}

	@Override
	public void setLoc(boolean client, boolean server)
	{
		super.setLoc(client, server);

		for (FileNode fileNode : files)
		{
			fileNode.setLoc(client, server);
		}

		for (FolderNode folderNode : folders)
		{
			folderNode.setLoc(client, server);
		}
	}

	public ArrayList<FolderNode> getFolders()
	{
		return folders;
	}

	public ArrayList<FileNode> getFiles()
	{
		return files;
	}

	private void initRelations()
	{
		for (FileNode fileNode : files)
		{
			fileNode.parentFolder = this;
		}
		for (FolderNode folderNode : folders)
		{
			folderNode.parentFolder = this;
			folderNode.initRelations();
		}
	}

	public FolderNode goTo(String path)
	{
		if (path.length() == 1)
		{
			return this;
		}

		String nextName = path.substring(1);
		nextName = nextName.substring(0, nextName.indexOf('/'));
		path = path.substring(nextName.length() + 1);

		for (FolderNode folder : folders)
		{
			if (folder.getName().equals(nextName))
			{
				return folder.goTo(path);
			}
		}

		return this;
	}

	@Override
	public String getFilePath()
	{
		StringBuilder builder = new StringBuilder("/");
		FolderNode folder = this;
		FolderNode parent;
		while ((parent = folder.getParentFolder()) != null)
		{
			builder.insert(0, folder.name);
			builder.insert(0, "/");
			folder = parent;
		}
		return builder.toString();
	}

	public boolean isFilesOnClient(boolean onlyAll)
	{
		if (isOnClient() != onlyAll)
		{
			return isOnClient();
		}

		for (FileNode fileNode : files)
		{
			if (fileNode.isOnClient() != onlyAll)
			{
				return fileNode.isOnClient();
			}
		}

		for (FolderNode folderNode : folders)
		{
			boolean res = folderNode.isFilesOnClient(onlyAll);
			if (res != onlyAll)
			{
				return res;
			}
		}

		return onlyAll;
	}

	public void sort()
	{
		if (folders.size() > 0)
		{
			Collections.sort(folders);
		}
		if (folders.size() > 0)
		{
			Collections.sort(files);
		}
	}

	public boolean isFilesOnServer(boolean onlyAll)
	{
		if (isOnServer() != onlyAll)
		{
			return isOnServer();
		}

		for (FileNode fileNode : files)
		{
			if (fileNode.isOnServer() != onlyAll)
			{
				return fileNode.isOnServer();
			}
		}

		for (FolderNode folderNode : folders)
		{
			boolean res = folderNode.isFilesOnServer(onlyAll);
			if (res != onlyAll)
			{
				return res;
			}
		}

		return onlyAll;
	}
}
