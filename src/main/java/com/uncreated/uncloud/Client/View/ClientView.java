package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.RequestStatus;
import com.uncreated.uncloud.Common.FileStorage.FolderNode;

public interface ClientView
{
	void onRegister(RequestStatus requestStatus);

	void onAuth(RequestStatus requestStatus);

	void onUpdateFiles(FolderNode mergedFiles);

	void onFailRequest(RequestStatus requestStatus);

	void onLogout();
}
