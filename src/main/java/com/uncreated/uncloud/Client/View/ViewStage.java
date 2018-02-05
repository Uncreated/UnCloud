package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.ClientController;
import com.uncreated.uncloud.Client.RequestStatus;
import com.uncreated.uncloud.Server.storage.FileNode;
import javafx.stage.Stage;

public abstract class ViewStage implements ClientView
{
	protected ClientController clientController;

	public ViewStage(ClientController clientController)
	{
		this.clientController = clientController;
	}

	public abstract void onStart(Stage stage);

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
	public void onGetFileResponse(RequestStatus<FileNode> requestStatus)
	{

	}

	@Override
	public void onSetFileResponse(RequestStatus<FileNode> requestStatus)
	{

	}

	@Override
	public void onRemoveFileResponse(RequestStatus<FileNode> requestStatus)
	{

	}
}
