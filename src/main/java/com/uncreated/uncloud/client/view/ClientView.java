package com.uncreated.uncloud.client.view;

import com.uncreated.uncloud.client.RequestStatus;
import com.uncreated.uncloud.common.filestorage.FolderNode;

public interface ClientView
{
	void onRegister(RequestStatus requestStatus);

	void onAuth(RequestStatus requestStatus);

	void onUpdateFiles(FolderNode mergedFiles);

	void onFailRequest(RequestStatus requestStatus);

	void onLogout();

	void call(Runnable runnable);
}
