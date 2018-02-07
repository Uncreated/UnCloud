package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.ClientController;
import com.uncreated.uncloud.Client.RequestStatus;
import com.uncreated.uncloud.Common.FileStorage.FolderNode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import static com.uncreated.uncloud.Client.View.ViewStage.news;

public class ClientApp extends Application implements ClientView
{
	private Stage stage;

	private AuthStage authStage;
	private FilesStage filesStage;
	private ViewStage curStage;

	public static void main(String[] args)
	{
		launch(ClientApp.class, args);
	}

	public ClientApp()
	{
		reload();
	}

	@Override
	public void onFailRequest(RequestStatus requestStatus)
	{
		curStage.onFailRequest(requestStatus);
	}

	@Override
	public void onUpdateFiles(FolderNode mergedFiles)
	{
		curStage.onUpdateFiles(mergedFiles);
	}

	@Override
	public void onRegister(RequestStatus requestStatus)
	{
		curStage.onRegister(requestStatus);
	}

	@Override
	public void onAuth(RequestStatus requestStatus)
	{
		if (!requestStatus.isOk())
		{
			news(false, requestStatus.getMsg());
			authStage.onAuth(requestStatus);
		} else
		{
			curStage = filesStage;
			filesStage.onStart(stage);
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		stage = primaryStage;
		curStage = authStage;
		authStage.onStart(stage);
	}

	@Override
	public void onLogout()
	{
		reload();
		try
		{
			start(stage);
		} catch (Exception e)
		{
			e.printStackTrace();
			news(false, e.getMessage());
			Platform.exit();
		}
	}

	private void reload()
	{
		ClientController clientController = new ClientController(this);
		authStage = new AuthStage(clientController);
		filesStage = new FilesStage(clientController);
	}
}
