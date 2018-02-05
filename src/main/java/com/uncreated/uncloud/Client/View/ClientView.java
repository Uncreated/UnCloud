package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.RequestStatus;
import com.uncreated.uncloud.Server.storage.FileNode;

public interface ClientView
{
	void onRegister(RequestStatus requestStatus);

	void onAuth(RequestStatus requestStatus);

	void onUserFiles(RequestStatus requestStatus);

	void onGetFileResponse(RequestStatus<FileNode> requestStatus);

	void onSetFileResponse(RequestStatus<FileNode> requestStatus);

	void onRemoveFileResponse(RequestStatus<FileNode> requestStatus);
}
