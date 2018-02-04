package com.uncreated.uncloud.Client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

public class ClientApp extends Application implements ClientView
{
	private ClientController clientController;

	private VBox buttonsPane;
	private BorderPane rightPane;

	private Stage stage;
	private Button getFileButton;
	private Button removeFileButton;

	public static void main(String[] args)
	{
		launch(ClientApp.class, args);
	}

	public ClientApp()
	{
		clientController = new ClientController(this);
	}

	private void news(boolean good, String message)
	{
		Alert alert = new Alert(good ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, message, ButtonType.OK);
		alert.setTitle("123");
		alert.showAndWait();
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
			news(false, requestStatus.getMsg());
		else
			stageFiles();
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
		FlowPane filesPane = new FlowPane();

		filesPane.setPadding(new Insets(10, 0, 10, 10));
		filesPane.setHgap(10);
		filesPane.setVgap(10);
		if (!rootFolder)
		{
			Button button = new Button("<- Back");
			button.setPrefHeight(100);
			button.setPrefWidth(100);
			button.setOnMouseClicked(event ->
			{
				clientController.goBack();
			});
			filesPane.getChildren().add(button);
		}
		for (String name : files)
		{
			Button button = new Button(name);
			button.setPrefHeight(100);
			button.setPrefWidth(100);
			button.setOnMouseClicked(event ->
			{
				clientController.fileClick(name);
			});
			filesPane.getChildren().add(button);
		}
		rightPane.getChildren().clear();
		rightPane.setCenter(filesPane);
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
		else
			news(true, "File was deleted from server");
	}

	@Override
	public void onFileSelected()
	{
		getFileButton.setDisable(false);
		removeFileButton.setDisable(false);
	}

	@Override
	public void onFileUnselected()
	{
		getFileButton.setDisable(true);
		removeFileButton.setDisable(true);
	}

	private void stageFiles()
	{
		BorderPane root = new BorderPane();

		VBox leftPane = new VBox();
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

		rightPane = new BorderPane();
		ProgressIndicator progressIndicator = new ProgressIndicator();
		rightPane.setCenter(progressIndicator);

		scrollPane.setContent(rightPane);

		root.setLeft(leftPane);
		root.setCenter(scrollPane);

		//buttons
		leftPane.setPadding(new Insets(10));
		leftPane.setSpacing(10);
		Button addFileButton = new Button("Add File");
		addFileButton.setPrefWidth(100);
		addFileButton.setPrefHeight(100);
		leftPane.getChildren().add(addFileButton);

		getFileButton = new Button("Get File");
		getFileButton.setPrefWidth(100);
		getFileButton.setPrefHeight(100);
		leftPane.getChildren().add(getFileButton);
		getFileButton.setOnAction(event ->
		{
			clientController.writeFile();
		});

		removeFileButton = new Button("Remove File");
		removeFileButton.setPrefWidth(100);
		removeFileButton.setPrefHeight(100);
		leftPane.getChildren().add(removeFileButton);

		removeFileButton.setOnAction(event ->
		{
			clientController.removeFile();
		});

		onFileUnselected();
		stage.getScene().setRoot(root);

		clientController.userFiles();
	}

	private void stageAuth(Stage primaryStage) throws Exception
	{
		stage = primaryStage;

		Text loginText = new Text("Login:");
		TextField loginTextField = new TextField();
		Text passwordText = new Text("Password:");
		TextField passwordTextField = new TextField();
		Button registerButton = new Button("Register");
		Button authButton = new Button("Authorize");

		HBox hBox = new HBox(registerButton, authButton);
		hBox.setSpacing(15);

		VBox vBox = new VBox(loginText, loginTextField, passwordText, passwordTextField, hBox);
		vBox.setMaxWidth(250);
		vBox.setFillWidth(false);
		vBox.setSpacing(10);
		vBox.setAlignment(Pos.CENTER);

		BorderPane root = new BorderPane();
		root.setCenter(vBox);

		registerButton.setOnAction(event ->
		{
			onAuthClick(loginTextField, passwordTextField, false);
		});

		authButton.setOnAction(event ->
		{
			onAuthClick(loginTextField, passwordTextField, true);
		});

		stage.setScene(new Scene(root, 1, 1));
		stage.setMinHeight(540);
		stage.setMinWidth(960);
		stage.centerOnScreen();
		stage.show();
	}

	private void onAuthClick(TextField loginTextField, TextField passwordTextField, boolean auth)
	{
		String login = loginTextField.getText();
		String password = passwordTextField.getText();
		if (login.length() > 0 && password.length() > 0)
		{
			if (auth)
				clientController.auth(login, password);
			else
				clientController.register(login, password);
		} else
			news(false, "Incorrect login or password");
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		stageAuth(primaryStage);
	}
}
