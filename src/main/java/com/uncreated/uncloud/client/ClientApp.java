package com.uncreated.uncloud.client;

import com.uncreated.uncloud.client.view.AuthStage;
import com.uncreated.uncloud.client.view.FilesStage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ClientApp
		extends Application
{
	private static ClientApp instance;
	private Stage stage;

	private AuthStage authStage;
	private FilesStage filesStage;

	public static ClientApp getInstance()
	{
		return instance;
	}

	public static void main(String[] args)
	{
		launch(ClientApp.class, args);
	}

	public ClientApp()
	{
		ClientController clientController = new ClientController("C:/UnCloud/Client/");
		authStage = new AuthStage(clientController);
		instance = this;
		initStages();
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		this.stage = primaryStage;
		authStage.onStart(stage);
	}

	private void initStages()
	{
		ClientController clientController = new ClientController("C:/UnCloud/Client/");
		authStage = new AuthStage(clientController);
		filesStage = new FilesStage(clientController);
	}

	public void reload()
	{
		initStages();
		authStage.setAutoAuth(false);
		try
		{
			start(stage);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Platform.exit();
		}
	}

	public void openFilesStage()
	{
		filesStage.onStart(stage);
	}
}
