package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.ClientController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class FilesStage extends ViewStage
{
	private BorderPane rightPane;

	private ToggleButton getFileButton;
	private ToggleButton deleteFileButton;

	private Image imageAddFile;
	private Image imageGetFile;
	private Image imageDeleteFile;
	private Image imageFile;
	private Image imageFolder;

	public FilesStage(ClientController clientController)
	{
		super(clientController);

		imageFolder = loadImage("C:/UnCloud/folderIcon.png");
		imageFile = loadImage("C:/UnCloud/fileIcon.png");
		imageAddFile = loadImage("C:/UnCloud/addFileIcon.png");
		imageGetFile = loadImage("C:/UnCloud/getFileIcon.png");
		imageDeleteFile = loadImage("C:/UnCloud/deleteFileIcon.png");
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


	public void stageFiles(Stage stage)
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
		ClientApp.news(false, "Can not to read interface components");

		Platform.exit();
		return null;
	}
}
