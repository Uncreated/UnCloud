package com.uncreated.uncloud.Client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ClientApp extends Application implements ClientView
{
	private ClientController clientController;

	private VBox buttonsPane;
	private BorderPane rightPane;

	private Stage stage;
	private ToggleButton getFileButton;
	private ToggleButton deleteFileButton;

	private String path;

	private Image imageAddFile;
	private Image imageGetFile;
	private Image imageDeleteFile;
	private Image imageFile;
	private Image imageFolder;

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

	private ToggleButton customButton(String title, Image image)
	{
		ToggleButton button = new ToggleButton(title);
		button.setPrefHeight(100);
		//button.setPrefWidth(500);
		button.setStyle("-fx-font: 22 arial; -fx-background-color: transparent;");
		ImageView imageView = new ImageView();
		button.setGraphic(imageView);
		imageView.imageProperty()
				.bind(Bindings.when(button.selectedProperty()).then(image).otherwise(image));

		return button;
	}

	@Override
	public void onFolderOpen(ArrayList<String> files, boolean rootFolder)
	{
		VBox filesPane = new VBox();

		filesPane.setPadding(new Insets(10, 0, 10, 10));
		filesPane.setSpacing(10);
		if (!rootFolder)
		{
			ToggleButton backButton = customButton("../", imageFolder);
			backButton.setOnMouseClicked(event ->
			{
				clientController.goBack();
			});
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(backButton);
		}
		for (String name : files)
		{
			ToggleButton button = customButton(name, name.endsWith("/") ? imageFolder : imageFile);
			button.setOnMouseClicked(event ->
			{
				clientController.fileClick(name);
			});
			filesPane.setAlignment(Pos.CENTER_LEFT);
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
	}

	@Override
	public void onFileSelected()
	{
		getFileButton.setDisable(false);
		deleteFileButton.setDisable(false);
	}

	@Override
	public void onFileUnselected()
	{
		getFileButton.setDisable(true);
		deleteFileButton.setDisable(true);
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
		ToggleButton addFileButton = customButton(null, imageAddFile);
		leftPane.getChildren().add(addFileButton);
		addFileButton.setOnAction(event ->
		{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select file for upload to server");
			File file = fileChooser.showOpenDialog(stage);
			clientController.setFile(file);
		});

		getFileButton = customButton(null, imageGetFile);
		leftPane.getChildren().add(getFileButton);
		getFileButton.setOnAction(event ->
		{
			clientController.writeFile();
		});

		deleteFileButton = customButton(null, imageDeleteFile);
		leftPane.getChildren().add(deleteFileButton);

		deleteFileButton.setOnAction(event ->
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

		imageFolder = loadImage("C:/UnCloud/folderIcon.png");
		imageFile = loadImage("C:/UnCloud/fileIcon.png");
		imageAddFile = loadImage("C:/UnCloud/addFileIcon.png");
		imageGetFile = loadImage("C:/UnCloud/getFileIcon.png");
		imageDeleteFile = loadImage("C:/UnCloud/deleteFileIcon.png");
	}

	private Image loadImage(String fileName)
	{
		try
		{
			File file = new File(fileName);
			return new Image(new FileInputStream(file));
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		news(false, "Can not to read interface components");

		Platform.exit();
		return null;
	}
}
