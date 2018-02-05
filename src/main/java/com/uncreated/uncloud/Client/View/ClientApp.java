package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.ClientController;
import com.uncreated.uncloud.Client.RequestStatus;
import com.uncreated.uncloud.Server.storage.FileNode;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class ClientApp extends Application implements ClientView
{
	private ClientController clientController;

	private Stage stage;

	private AuthStage authStage;
	private FilesStage filesStage;

	public static void main(String[] args)
	{
		launch(ClientApp.class, args);
	}

	public ClientApp()
	{
		clientController = new ClientController(this);
		authStage = new AuthStage(clientController);
		filesStage = new FilesStage(clientController);
	}

	public static void news(boolean good, String message)
	{
		Alert alert = new Alert(good ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, message, ButtonType.OK);
		alert.setTitle("123");
		alert.show();
	}

	@Override
	public void onRegister(RequestStatus requestStatus)
	{
		if (!requestStatus.isOk())
			news(false, requestStatus.getMsg());
		else
			news(true, "You have successfully registered");
	}

	@Override
	public void onAuth(RequestStatus requestStatus)
	{
		if (!requestStatus.isOk())
		{
			news(false, requestStatus.getMsg());
			authStage.onAuth(requestStatus);
		} else
			filesStage.onStart(stage);
	}

	@Override
	public void onUserFiles(RequestStatus requestStatus)
	{
		filesStage.onUserFiles(requestStatus);
	}

	@Override
	public void onGetFileResponse(RequestStatus<FileNode> requestStatus)
	{
		filesStage.onGetFileResponse(requestStatus);
	}

	@Override
	public void onSetFileResponse(RequestStatus<FileNode> requestStatus)
	{
		filesStage.onSetFileResponse(requestStatus);
	}

	@Override
	public void onRemoveFileResponse(RequestStatus<FileNode> requestStatus)
	{
		filesStage.onRemoveFileResponse(requestStatus);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		stage = primaryStage;
		authStage.onStart(stage);
	}
}
