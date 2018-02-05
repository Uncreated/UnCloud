package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.ClientController;
import com.uncreated.uncloud.Client.RequestStatus;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.ArrayList;

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
		}
		else
			filesStage.stageFiles(stage);
	}

	@Override
	public void onUserFiles(RequestStatus requestStatus)
	{
		if (!requestStatus.isOk())
			news(false, requestStatus.getMsg());
		else
			clientController.fileClick("/");
	}

	@Override
	public void onFolderOpen(ArrayList<String> files, boolean rootFolder)
	{
		filesStage.onFolderOpen(files, rootFolder);
	}

	@Override
	public void onGetFileResponse(RequestStatus requestStatus)
	{
		if (!requestStatus.isOk())
			news(false, requestStatus.getMsg());
		else
			news(true, "File downloaded");
	}

	@Override
	public void onSetFileResponse(RequestStatus requestStatus)
	{
		if (!requestStatus.isOk())
			news(false, requestStatus.getMsg());
		else
			news(true, "File uploaded to server");
	}

	@Override
	public void onRemoveFileResponse(RequestStatus requestStatus)
	{
		if (!requestStatus.isOk())
			news(false, requestStatus.getMsg());
	}

	@Override
	public void onFileSelected()
	{
		filesStage.onFileSelected();
	}

	@Override
	public void onFileUnselected()
	{
		filesStage.onFileUnselected();
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		stage = primaryStage;
		authStage.stageAuth(stage);
	}
}
