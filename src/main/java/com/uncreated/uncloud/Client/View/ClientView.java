package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.RequestStatus;

import java.util.ArrayList;

public interface ClientView
{
	void onRegister(RequestStatus requestStatus);

	void onAuth(RequestStatus requestStatus);

	void onUserFiles(RequestStatus requestStatus);

	void onGetFileResponse(RequestStatus requestStatus);

	void onSetFileResponse(RequestStatus requestStatus);

	void onRemoveFileResponse(RequestStatus requestStatus);

	void onFileSelected();

	void onFileUnselected();

	void onFolderOpen(ArrayList<String> files, boolean rootFolder);
}
