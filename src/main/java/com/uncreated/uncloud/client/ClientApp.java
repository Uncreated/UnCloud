package com.uncreated.uncloud.client;

import com.uncreated.uncloud.client.auth.AuthController;
import com.uncreated.uncloud.client.auth.view.AuthStage;
import com.uncreated.uncloud.client.files.FilesController;
import com.uncreated.uncloud.client.files.view.FilesStage;
import com.uncreated.uncloud.client.requests.RequestHandler;
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


	private FilesController filesController;
	private AuthController authController;
	private RequestHandler requestHandler;

	public static void main(String[] args)
	{
		launch(ClientApp.class, args);
	}

	public ClientApp()
	{
		instance = this;

		requestHandler = new RequestHandler();

		authController = new AuthController(requestHandler);

		authStage = new AuthStage(this);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		this.stage = primaryStage;
		authStage.onStart(stage);
	}

	public FilesController getFilesController()
	{
		return filesController;
	}

	public AuthController getAuthController()
	{
		return authController;
	}

	public void openAuthStage()
	{
		filesStage.onPause();


		try
		{
			authStage.onStart(stage);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Platform.exit();
		}
	}

	public void openFilesStage()
	{
		authStage.onPause();
		authController.clear();

		filesController = new FilesController(requestHandler, "C:/UnCloud/Client/");
		filesController.setLogin(authController.getSelLogin());
		filesStage = new FilesStage(this);
		filesStage.onStart(stage);
	}
}
