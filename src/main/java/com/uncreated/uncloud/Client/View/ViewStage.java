package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.ClientController;
import com.uncreated.uncloud.Client.RequestStatus;
import com.uncreated.uncloud.Common.FileStorage.FolderNode;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public abstract class ViewStage implements ClientView
{
	protected ClientController clientController;

	ViewStage(ClientController clientController)
	{
		this.clientController = clientController;
	}

	public abstract void onStart(Stage stage);

	protected static void news(boolean good, String message)
	{
		Alert alert = new Alert(good ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, message, ButtonType.OK);
		alert.setTitle(good ? "Successfully" : "Fail");
		alert.showAndWait();
	}

	@Override
	public void onFailRequest(RequestStatus requestStatus)
	{
		news(false, requestStatus.getMsg());
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
	public void onUpdateFiles(FolderNode mergedFiles)
	{

	}

	@Override
	public void onLogout()
	{

	}


}
