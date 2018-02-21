package com.uncreated.uncloud.client.files;

import com.uncreated.uncloud.common.filestorage.FileNode;
import com.uncreated.uncloud.common.filestorage.FolderNode;

public class FileInfo
{
	private String name;
	private String info;
	private boolean downloaded;
	private boolean uploaded;
	private boolean directory;
	private boolean downloadAny;
	private boolean uploadAny;
	private boolean deleteAnyClient;
	private boolean deleteAnyServer;

	FileInfo(FileNode fileNode)
	{
		init(fileNode);

		this.directory = false;
		this.downloadAny = !this.downloaded;
		this.uploadAny = !this.uploaded;
		this.deleteAnyClient = this.downloaded;
		this.deleteAnyServer = this.uploaded;
	}

	FileInfo(FolderNode folderNode)
	{
		init(folderNode);

		this.directory = true;
		this.downloadAny = !folderNode.isFilesOnClient(true);
		this.uploadAny = !folderNode.isFilesOnServer(false);
		this.deleteAnyClient = folderNode.isFilesOnClient(false);
		this.deleteAnyServer = folderNode.isFilesOnServer(false);
	}

	private void init(FileNode fileNode)
	{
		this.name = fileNode.getName();
		this.info = makeInfo(fileNode);
		this.downloaded = fileNode.isOnClient();
		this.uploaded = fileNode.isOnServer();
	}

	private String makeInfo(FileNode fileNode)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Name: ");
		builder.append(fileNode.getName());
		builder.append('\n');
		builder.append("Path: ");
		if (fileNode.getParentFolder() == null)
		{
			builder.append('/');
		}
		else
		{
			builder.append(fileNode.getParentFolder().getFilePath());
		}
		builder.append('\n');

		builder.append("Size: ");
		builder.append(fileNode.getSizeString());

		builder.append('\n');
		builder.append("Location: ");
		if (fileNode.isOnClient())
		{
			builder.append("client");
		}
		if (fileNode.isOnClient() && fileNode.isOnServer())
		{
			builder.append(", ");
		}
		if (fileNode.isOnServer())
		{
			builder.append("server");
		}
		return builder.toString();
	}

	public String getName()
	{
		return name;
	}

	public String getInfo()
	{
		return info;
	}

	public boolean isDownloaded()
	{
		return downloaded;
	}

	public boolean isUploaded()
	{
		return uploaded;
	}

	public boolean isDirectory()
	{
		return directory;
	}

	public boolean isDownloadAny()
	{
		return downloadAny;
	}

	public boolean isUploadAny()
	{
		return uploadAny;
	}

	public boolean isDeleteAnyClient()
	{
		return deleteAnyClient;
	}

	public boolean isDeleteAnyServer()
	{
		return deleteAnyServer;
	}
}
