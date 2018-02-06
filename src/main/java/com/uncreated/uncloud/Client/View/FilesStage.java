package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.ClientController;
import com.uncreated.uncloud.Client.RequestStatus;
import com.uncreated.uncloud.Common.FileStorage.FNode;
import com.uncreated.uncloud.Common.FileStorage.FileNode;
import com.uncreated.uncloud.Common.FileStorage.FolderNode;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class FilesStage extends ViewStage
{
	private static final String ICON_FOLDER = "C:/UnCloud/Icons/";
	private static final String[] ICONS = {
			"addFile.png",
			"clientFile.png",
			"clientFolder.png",
			"clientServerFile.png",
			"clientServerFolder.png",
			"createFolder.png",
			"deleteFile.png",
			"file.png",
			"folder.png",
			"getFile.png",
			"serverFile.png",
			"serverFolder.png"};

	private BorderPane rightPane;

	private ToggleButton getFileButton;
	private ToggleButton deleteFileButton;

	private HashMap<String, Image> images;

	private FolderNode rootFolder;
	private FNode selectedFNode;
	private FolderNode curFolder;

	public FilesStage(ClientController clientController)
	{
		super(clientController);

		images = new HashMap<>();
		for (String fileName : ICONS)
			images.put(fileName, loadImage(fileName));
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

	private void selectFNode(FNode fNode)
	{
		selectedFNode = fNode;
		getFileButton.setDisable(fNode == null);
		deleteFileButton.setDisable(fNode == null);
	}

	private void showFolder(FolderNode folderNode)
	{
		curFolder = folderNode;
		selectFNode(null);
		VBox filesPane = new VBox();

		filesPane.setPadding(new Insets(10, 0, 10, 10));
		filesPane.setSpacing(10);
		if (folderNode.getParentFolder() != null)
		{
			ToggleButton backButton = customButton("../" + folderNode.getName(), images.get("folder.png"));
			backButton.setOnMouseClicked(event ->
			{
				showFolder(folderNode.getParentFolder());
			});
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(backButton);
		}
		for (FolderNode folder : folderNode.getFolders())
		{
			Image image = images.get("folder.png");
			if (folder.isOnClient() && folder.isOnServer())
				image = images.get("clientServerFolder.png");
			else if (folder.isOnClient())
				image = images.get("clientFolder.png");
			else if (folder.isOnServer())
				image = images.get("serverFolder.png");
			ToggleButton button = customButton(folder.getName(), image);
			button.setOnMouseClicked(event ->
			{
				if (selectedFNode == folder)
					showFolder(folder);
				else
					selectFNode(folder);
			});
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(button);
		}
		for (FileNode file : folderNode.getFiles())
		{
			Image image = images.get("file.png");
			if (file.isOnClient() && file.isOnServer())
				image = images.get("clientServerFile.png");
			else if (file.isOnClient())
				image = images.get("clientFile.png");
			else if (file.isOnServer())
				image = images.get("serverFile.png");
			ToggleButton button = customButton(file.getName(), image);
			button.setOnMouseClicked(event ->
			{
				selectFNode(file);
			});
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(button);
		}
		rightPane.getChildren().clear();
		rightPane.setCenter(filesPane);
	}

	@Override
	public void onGetFileResponse(RequestStatus<FNode> requestStatus)
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
	public void onUpdateFiles(FolderNode mergedFiles)
	{
		String savedPath = "";
		if (curFolder != null)
			savedPath += curFolder.getFilePath().getName();
		savedPath += "/";

		rootFolder = mergedFiles;
		showFolder(rootFolder.goTo(savedPath));
	}

	@Override
	public void onFailRequest(RequestStatus requestStatus)
	{
		news(false, requestStatus.getMsg());
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

		ToggleButton createFolderButton = customButton(null, images.get("createFolder.png"));
		leftPane.getChildren().add(createFolderButton);
		createFolderButton.setOnAction(event ->
		{
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Create folder");
			dialog.setHeaderText("Enter folder name");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent())
			{
				String name = result.get();
				if (name.length() > 0)
				{
					clientController.createFolder(name, curFolder);
				}
			}
		});

		ToggleButton addFileButton = customButton(null, images.get("addFile.png"));
		leftPane.getChildren().add(addFileButton);
		addFileButton.setOnAction(event ->
		{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select file for upload to server");
			File file = fileChooser.showOpenDialog(stage);
			clientController.setFile(file, curFolder);
		});

		getFileButton = customButton(null, images.get("getFile.png"));
		leftPane.getChildren().add(getFileButton);
		getFileButton.setOnAction(event ->
		{
			//clientController.getFile(selectedFile);
		});

		deleteFileButton = customButton(null, images.get("deleteFile.png"));
		leftPane.getChildren().add(deleteFileButton);

		deleteFileButton.setOnAction(event ->
		{
			if (selectedFNode.isOnClient() && selectedFNode.isOnServer())
			{
				//dialog
				//if c1 == client
				//if c2 == server
			} else
				clientController.removeFile(selectedFNode);
			selectFNode(null);
		});

		selectFNode(null);
		stage.getScene().setRoot(root);
	}

	private Image loadImage(String fileName)
	{
		try
		{
			File file = new File(ICON_FOLDER + fileName);
			FileInputStream inputStream = new FileInputStream(file);
			Image image = new Image(new FileInputStream(file));
			inputStream.close();
			return image;
		} catch (IOException e)
		{
			e.printStackTrace();
			news(false, "Can not to read interface components");
			Platform.exit();
			return null;
		}
	}
}
