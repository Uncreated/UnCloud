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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class FilesStage extends ViewStage
{
	private static final String ICON_FOLDER = "src/main/resources/icons/";
	private static final String[] ICONS = {
			"createFolder.png",
			"addFile.png",
			"download.png",
			"upload.png",
			"deleteClient.png",
			"deleteServer.png",
			"backFolder.png",
			"clientFile.png",
			"clientFolder.png",
			"serverFile.png",
			"serverFolder.png",
			"clientServerFile.png",
			"clientServerFolder.png",
			"logout.png"};

	private static DropShadow grayShadow = new DropShadow(20, Color.GRAY);
	private static DropShadow blueShadow = new DropShadow(20, Color.BLUE);

	private BorderPane rightPane;
	private VBox leftPane;

	private ToggleButton createFolderButton;
	private ToggleButton addFileButton;
	private ToggleButton downloadButton;
	private ToggleButton uploadButton;
	private ToggleButton deleteClientButton;
	private ToggleButton deleteServerButton;
	private ToggleButton logoutButton;

	private HashMap<String, Image> images;

	private FNode selectedFNode;
	private FolderNode curFolder;
	private ToggleButton selectedButton;

	FilesStage(ClientController clientController)
	{
		super(clientController);

		images = new HashMap<>();
		try
		{
			for (String fileName : ICONS)
				images.put(fileName, loadImage(fileName));
		} catch (IOException e)
		{
			e.printStackTrace();
			news(false, "Can not to read interface components. " + e.getMessage());
			Platform.exit();
		}
	}

	private ToggleButton customButton(String title, Image image)
	{
		ToggleButton button = new ToggleButton(title);
		button.setStyle("-fx-font: 22 arial; -fx-background-color: transparent;");
		//button.setPrefWidth(80);
		button.setAlignment(Pos.TOP_LEFT);
		ImageView imageView = new ImageView();
		button.setGraphic(imageView);
		imageView.imageProperty()
				.bind(Bindings.when(button.selectedProperty()).then(image).otherwise(image));

		button.addEventHandler(MouseEvent.MOUSE_ENTERED,
				event ->
				{
					if (selectedButton == null || selectedButton != button)
						button.setEffect(grayShadow);
				});

		button.addEventHandler(MouseEvent.MOUSE_EXITED,
				event ->
				{
					if (selectedButton == null || selectedButton != button)
						button.setEffect(null);
				});
		return button;
	}

	private void selectFNode(FNode fNode)
	{
		selectedFNode = fNode;
		leftPane.getChildren().clear();
		leftPane.getChildren().addAll(createFolderButton, addFileButton);
		if (fNode != null)
		{
			if (fNode instanceof FileNode)
			{
				if (!fNode.isOnClient())
					leftPane.getChildren().add(downloadButton);
				if (!fNode.isOnServer())
					leftPane.getChildren().add(uploadButton);

				if (fNode.isOnClient())
					leftPane.getChildren().add(deleteClientButton);
				if (fNode.isOnServer())
					leftPane.getChildren().add(deleteServerButton);
			} else if (fNode instanceof FolderNode)
			{
				FolderNode folderNode = (FolderNode) fNode;
				if (!folderNode.isFilesOnClient(true))
					leftPane.getChildren().add(downloadButton);

				if (!folderNode.isFilesOnServer(true))
					leftPane.getChildren().add(uploadButton);

				if (folderNode.isFilesOnClient(false))
					leftPane.getChildren().add(deleteClientButton);
				if (folderNode.isFilesOnServer(false))
					leftPane.getChildren().add(deleteServerButton);
			}
		}
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
			ToggleButton backButton = customButton(folderNode.getName(), images.get("backFolder.png"));
			backButton.setAlignment(Pos.TOP_LEFT);
			backButton.setOnMouseClicked(event ->
			{
				if (selectedButton != null && selectedButton == backButton)
					showFolder(folderNode.getParentFolder());
				else
				{
					if (selectedButton != null)
						selectedButton.setEffect(null);

					selectFNode(null);
					backButton.setEffect(blueShadow);
					selectedButton = backButton;
				}
			});
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
			setButtonSelectEvent(button, folder);
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
			setButtonSelectEvent(button, file);
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(button);
		}
		rightPane.getChildren().clear();
		rightPane.setCenter(filesPane);
	}

	private void setButtonSelectEvent(ToggleButton button, FNode fNode)
	{
		button.setOnMouseClicked(event ->
		{
			if (selectedFNode == fNode)
			{
				if (fNode instanceof FolderNode)
					showFolder((FolderNode) fNode);
			} else
			{
				selectFNode(fNode);
				if (selectedButton != null)
					selectedButton.setEffect(null);

				button.setEffect(blueShadow);
				selectedButton = button;
			}
		});
	}

	@Override
	public void onUpdateFiles(FolderNode mergedFiles)
	{
		String savedPath = "";
		if (curFolder != null)
			savedPath += curFolder.getFilePath();
		savedPath += "/";

		showFolder(mergedFiles.goTo(savedPath));
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

		BorderPane leftBotderPane = new BorderPane();
		leftPane = new VBox();
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

		rightPane = new BorderPane();
		ProgressIndicator progressIndicator = new ProgressIndicator();
		rightPane.setCenter(progressIndicator);

		scrollPane.setContent(rightPane);

		logoutButton = customButton(null, images.get("logout.png"));
		logoutButton.setOnAction(event ->
		{
			clientController.logout();
		});

		leftBotderPane.setTop(leftPane);
		leftBotderPane.setBottom(logoutButton);
		root.setLeft(leftBotderPane);
		root.setCenter(scrollPane);

		//buttons
		leftPane.setPadding(new Insets(10));
		leftPane.setSpacing(10);

		createFolderButton = customButton(null, images.get("createFolder.png"));
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

		addFileButton = customButton(null, images.get("addFile.png"));
		leftPane.getChildren().add(addFileButton);
		addFileButton.setOnAction(event ->
		{
			//copy to client folder
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select file for upload to server");
			File file = fileChooser.showOpenDialog(stage);
			if (file != null)
				clientController.copyFile(file, curFolder);
		});

		downloadButton = customButton(null, images.get("download.png"));
		leftPane.getChildren().add(downloadButton);
		downloadButton.setOnAction(event ->
		{
			clientController.download(selectedFNode);
			selectFNode(null);
		});

		uploadButton = customButton(null, images.get("upload.png"));
		leftPane.getChildren().add(uploadButton);
		uploadButton.setOnAction(event ->
		{
			clientController.upload(selectedFNode);
			selectFNode(null);
		});

		deleteClientButton = customButton(null, images.get("deleteClient.png"));
		leftPane.getChildren().add(deleteClientButton);
		deleteClientButton.setOnAction(event ->
		{
			clientController.removeFileFromClient(selectedFNode);
			selectFNode(null);
		});

		deleteServerButton = customButton(null, images.get("deleteServer.png"));
		leftPane.getChildren().add(deleteServerButton);
		deleteServerButton.setOnAction(event ->
		{
			clientController.removeFileFromServer(selectedFNode);
			selectFNode(null);
		});

		selectFNode(null);
		stage.getScene().setRoot(root);
	}

	private Image loadImage(String fileName) throws IOException
	{
		File file = new File(ICON_FOLDER + fileName);
		FileInputStream inputStream = new FileInputStream(file);
		Image image = new Image(new FileInputStream(file));
		inputStream.close();
		return image;
	}
}
