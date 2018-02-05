package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.ClientController;
import com.uncreated.uncloud.Client.RequestStatus;
import com.uncreated.uncloud.Server.storage.FileNode;
import com.uncreated.uncloud.Server.storage.FolderNode;
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

import static com.uncreated.uncloud.Client.View.ClientApp.news;

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

	private FolderNode rootFolder;
	private FileNode selectedFile;
	private FolderNode curFolder;

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
		button.setStyle("-fx-font: 22 arial; -fx-background-color: transparent;");
		ImageView imageView = new ImageView();
		button.setGraphic(imageView);
		imageView.imageProperty()
				.bind(Bindings.when(button.selectedProperty()).then(image).otherwise(image));

		return button;
	}

	private void selectFile(FileNode fileNode)
	{
		selectedFile = fileNode;
		getFileButton.setDisable(selectedFile == null);
		deleteFileButton.setDisable(selectedFile == null);
	}

	@Override
	public void onUserFiles(RequestStatus requestStatus)
	{
		this.rootFolder = clientController.getRootFolder();
		showFolder(rootFolder);
	}

	private void showFolder(FolderNode folderNode)
	{
		curFolder = folderNode;
		selectFile(null);
		VBox filesPane = new VBox();

		filesPane.setPadding(new Insets(10, 0, 10, 10));
		filesPane.setSpacing(10);
		if (folderNode.getParentFolder() != null)
		{
			ToggleButton backButton = customButton("../" + folderNode.getName(), imageFolder);
			backButton.setOnMouseClicked(event ->
			{
				showFolder(folderNode.getParentFolder());
			});
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(backButton);
		}
		for (FolderNode folder : folderNode.getFolders())
		{
			ToggleButton button = customButton(folder.getName(), imageFolder);
			button.setOnMouseClicked(event ->
			{
				showFolder(folder);
			});
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(button);
		}
		for (FileNode file : folderNode.getFiles())
		{
			ToggleButton button = customButton(file.getName(), imageFile);
			button.setOnMouseClicked(event ->
			{
				selectFile(file);
			});
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(button);
		}
		rightPane.getChildren().clear();
		rightPane.setCenter(filesPane);
	}

	@Override
	public void onGetFileResponse(RequestStatus<FileNode> requestStatus)
	{
		if (!requestStatus.isOk())
			news(false, requestStatus.getMsg());
		else
			news(true, "File downloaded");
	}

	@Override
	public void onSetFileResponse(RequestStatus<FileNode> requestStatus)
	{
		if (!requestStatus.isOk())
			news(false, requestStatus.getMsg());
		else
		{
			if (curFolder == requestStatus.getData().getParentFolder())
				showFolder(curFolder);//reload

			news(true, "File uploaded to server");
		}
	}

	@Override
	public void onRemoveFileResponse(RequestStatus<FileNode> requestStatus)
	{
		if (!requestStatus.isOk())
			news(false, requestStatus.getMsg());
		else
		{
			if (curFolder == requestStatus.getData().getParentFolder())
				showFolder(curFolder);//reload

			news(true, "File removed from server");
		}
	}

	@Override
	public void onStart(Stage stage)
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
			clientController.setFile(file, curFolder);
		});

		getFileButton = customButton(null, imageGetFile);
		leftPane.getChildren().add(getFileButton);
		getFileButton.setOnAction(event ->
		{
			clientController.getFile(selectedFile);
		});

		deleteFileButton = customButton(null, imageDeleteFile);
		leftPane.getChildren().add(deleteFileButton);

		deleteFileButton.setOnAction(event ->
		{
			clientController.removeFile(selectedFile);
			selectFile(null);
		});

		selectFile(null);
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
		news(false, "Can not to read interface components");

		Platform.exit();
		return null;
	}
}
