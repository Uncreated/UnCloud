package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.RequestStatus;
import com.uncreated.uncloud.Common.FileStorage.FNode;
import com.uncreated.uncloud.Common.FileStorage.FileNode;
import com.uncreated.uncloud.Common.FileStorage.FolderNode;

public interface ClientView
{
	void onRegister(RequestStatus requestStatus);

	void onAuth(RequestStatus<FolderNode> requestStatus);

	void onGetFileResponse(RequestStatus<FNode> requestStatus);

	void onSetFileResponse(RequestStatus<FileNode> requestStatus);

	void onUpdateFiles(FolderNode mergedFiles);

	void onFailRequest(RequestStatus requestStatus);
}
