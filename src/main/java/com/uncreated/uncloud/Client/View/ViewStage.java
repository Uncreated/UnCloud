package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.ClientController;
import com.uncreated.uncloud.Client.RequestStatus;

import java.util.ArrayList;

public abstract class ViewStage implements ClientView
{
	protected ClientController clientController;

	public ViewStage(ClientController clientController)
	{
		this.clientController = clientController;
	}

	@Override
	public void onRegister(RequestStatus requestStatus)
	{

	}

	@Override
	public void onAuth(RequestStatus requestStatus)
	{

	}

	@Override
	public void onUserFiles(RequestStatus requestStatus)
	{

	}

	@Override
	public void onGetFileResponse(RequestStatus requestStatus)
	{

	}

	@Override
	public void onSetFileResponse(RequestStatus requestStatus)
	{

	}

	@Override
	public void onRemoveFileResponse(RequestStatus requestStatus)
	{

	}

	@Override
	public void onFileSelected()
	{

	}

	@Override
	public void onFileUnselected()
	{

	}

	@Override
	public void onFolderOpen(ArrayList<String> files, boolean rootFolder)
	{

	}
}
