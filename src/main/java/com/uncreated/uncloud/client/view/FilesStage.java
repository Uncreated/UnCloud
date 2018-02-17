package com.uncreated.uncloud.client.view;

import com.uncreated.uncloud.client.ClientApp;
import com.uncreated.uncloud.client.ClientController;
import com.uncreated.uncloud.client.RequestStatus;
import com.uncreated.uncloud.common.filestorage.FNode;
import com.uncreated.uncloud.common.filestorage.FileNode;
import com.uncreated.uncloud.common.filestorage.FolderNode;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class FilesStage
		extends ViewStage
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

	private BorderPane root;
	private VBox controlPane;
	private BorderPane rootRightPane;
	private ScrollPane scrollFilesPane;

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

	public FilesStage(ClientController clientController)
	{
		super(clientController);

		images = new HashMap<>();
		try
		{
			for (String fileName : ICONS)
			{
				images.put(fileName, loadImage(fileName));
			}
		}
		catch (IOException e)
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
					{
						button.setEffect(grayShadow);
					}
				});

		button.addEventHandler(MouseEvent.MOUSE_EXITED,
				event ->
				{
					if (selectedButton == null || selectedButton != button)
					{
						button.setEffect(null);
					}
				});
		return button;
	}

	private void selectFNode(FNode fNode)
	{
		selectedFNode = fNode;
		controlPane.getChildren().clear();
		controlPane.getChildren().addAll(createFolderButton, addFileButton);
		if (fNode != null)
		{
			if (fNode instanceof FileNode)
			{
				if (!fNode.isOnClient())
				{
					controlPane.getChildren().add(downloadButton);
				}
				if (!fNode.isOnServer())
				{
					controlPane.getChildren().add(uploadButton);
				}

				if (fNode.isOnClient())
				{
					controlPane.getChildren().add(deleteClientButton);
				}
				if (fNode.isOnServer())
				{
					controlPane.getChildren().add(deleteServerButton);
				}
			}
			else if (fNode instanceof FolderNode)
			{
				FolderNode folderNode = (FolderNode) fNode;
				if (!folderNode.isFilesOnClient(true))
				{
					controlPane.getChildren().add(downloadButton);
				}

				if (!folderNode.isFilesOnServer(true))
				{
					controlPane.getChildren().add(uploadButton);
				}

				if (folderNode.isFilesOnClient(false))
				{
					controlPane.getChildren().add(deleteClientButton);
				}
				if (folderNode.isFilesOnServer(false))
				{
					controlPane.getChildren().add(deleteServerButton);
				}
			}
		}
	}

	private void showFolder(FolderNode folderNode)
	{
		curFolder = folderNode;
		selectFNode(null);
		VBox filesPane = new VBox();

		if (root.getOnKeyPressed() == null)
		{
			root.setOnKeyPressed(event ->
			{
				System.out.println(event.getCode().toString());
				if (event.getCode() == KeyCode.F5)
				{
					clientController.updateFiles();
				}
			});
		}
		//filesPane.setPadding(new Insets(10, 0, 10, 10));
		filesPane.setSpacing(10);
		folderNode.sort();
		if (folderNode.getParentFolder() != null)
		{
			ToggleButton backButton = customButton(folderNode.getName(), images.get("backFolder.png"));
			backButton.setAlignment(Pos.TOP_LEFT);
			backButton.setOnMouseClicked(event ->
			{
				if (selectedButton != null && selectedButton == backButton)
				{
					showFolder(folderNode.getParentFolder());
				}
				else
				{
					if (selectedButton != null)
					{
						selectedButton.setEffect(null);
					}

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
			{
				image = images.get("clientServerFolder.png");
			}
			else if (folder.isOnClient())
			{
				image = images.get("clientFolder.png");
			}
			else if (folder.isOnServer())
			{
				image = images.get("serverFolder.png");
			}
			ToggleButton button = customButton(folder.getName(), image);
			button.setTooltip(makeTooltip(folder));
			setButtonSelectEvent(button, folder);
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(button);
		}
		for (FileNode file : folderNode.getFiles())
		{
			Image image = images.get("file.png");
			if (file.isOnClient() && file.isOnServer())
			{
				image = images.get("clientServerFile.png");
			}
			else if (file.isOnClient())
			{
				image = images.get("clientFile.png");
			}
			else if (file.isOnServer())
			{
				image = images.get("serverFile.png");
			}
			ToggleButton button = customButton(file.getName(), image);
			button.setTooltip(makeTooltip(file));
			setButtonSelectEvent(button, file);
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(button);
		}

		scrollFilesPane.setContent(filesPane);

		rootRightPane.getChildren().clear();
		rootRightPane.setCenter(scrollFilesPane);
	}

	private Tooltip makeTooltip(FNode fNode)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Name: ");
		builder.append(fNode.getName());
		builder.append('\n');
		builder.append("Path: ");
		if (fNode.getParentFolder() == null)
		{
			builder.append('/');
		}
		else
		{
			builder.append(fNode.getParentFolder().getFilePath());
		}
		builder.append('\n');

		builder.append("Size: ");
		builder.append(fNode.getSizeString());

		builder.append('\n');
		builder.append("Location: ");
		if (fNode.isOnClient())
		{
			builder.append("client");
		}
		if (fNode.isOnClient() && fNode.isOnServer())
		{
			builder.append(", ");
		}
		if (fNode.isOnServer())
		{
			builder.append("server");
		}
		return new Tooltip(builder.toString());
	}

	private void setButtonSelectEvent(ToggleButton button, FNode fNode)
	{
		button.setOnMouseClicked(event ->
		{
			if (selectedFNode == fNode)
			{
				if (fNode instanceof FolderNode)
				{
					showFolder((FolderNode) fNode);
				}
			}
			else
			{
				selectFNode(fNode);
				if (selectedButton != null)
				{
					selectedButton.setEffect(null);
				}

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
		{
			savedPath += curFolder.getFilePath();
		}
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
		super.onStart(stage);
		root = new BorderPane();

		root.setLeft(createLeftPane(stage));
		root.setCenter(createRightPane(stage));

		selectFNode(null);
		stage.getScene().setRoot(root);

		clientController.updateFiles();
	}

	private BorderPane createLeftPane(Stage stage)
	{
		BorderPane leftRootPane = new BorderPane();
		leftRootPane.setStyle("-fx-border-color: gray");

		controlPane = new VBox();

		logoutButton = customButton(null, images.get("logout.png"));
		logoutButton.setTooltip(new Tooltip("Logout"));
		logoutButton.setOnAction(event ->
		{
			clientController.logout();
			ClientApp.getInstance().reload();
		});

		leftRootPane.setTop(controlPane);
		leftRootPane.setBottom(logoutButton);

		createControlButtons(stage);

		return leftRootPane;
	}

	private void createControlButtons(Stage stage)
	{
		controlPane.setSpacing(10);

		createFolderButton = customButton(null, images.get("createFolder.png"));
		createFolderButton.setTooltip(new Tooltip("Create folder on server"));
		controlPane.getChildren().add(createFolderButton);
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
		addFileButton.setTooltip(new Tooltip("Add file on client"));
		controlPane.getChildren().add(addFileButton);
		addFileButton.setOnAction(event ->
		{
			//copy to client folder
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select file for upload to server");
			File file = fileChooser.showOpenDialog(stage);
			if (file != null)
			{
				clientController.copyFile(file, curFolder);
			}
		});

		downloadButton = customButton(null, images.get("download.png"));
		downloadButton.setTooltip(new Tooltip("Download"));
		controlPane.getChildren().add(downloadButton);
		downloadButton.setOnAction(event ->
		{
			clientController.download(selectedFNode);
			selectFNode(null);
		});

		uploadButton = customButton(null, images.get("upload.png"));
		uploadButton.setTooltip(new Tooltip("Upload"));
		controlPane.getChildren().add(uploadButton);
		uploadButton.setOnAction(event ->
		{
			clientController.upload(selectedFNode);
			selectFNode(null);
		});

		deleteClientButton = customButton(null, images.get("deleteClient.png"));
		deleteClientButton.setTooltip(new Tooltip("Delete from client"));
		controlPane.getChildren().add(deleteClientButton);
		deleteClientButton.setOnAction(event ->
		{
			clientController.removeFileFromClient(selectedFNode);
			selectFNode(null);
		});

		deleteServerButton = customButton(null, images.get("deleteServer.png"));
		deleteServerButton.setTooltip(new Tooltip("Delete from server"));
		controlPane.getChildren().add(deleteServerButton);
		deleteServerButton.setOnAction(event ->
		{
			clientController.removeFileFromServer(selectedFNode);
			selectFNode(null);
		});
	}

	private BorderPane createRightPane(Stage stage)
	{
		rootRightPane = new BorderPane();
		rootRightPane.setStyle("-fx-border-color: gray");

		//vbox with progress and title
		ProgressIndicator progressIndicator = new ProgressIndicator();
		Text progressTitle = new Text("Loading files...");

		VBox vBox = new VBox(10, progressIndicator, progressTitle);
		vBox.setMaxSize(70, 100);

		rootRightPane.setCenter(vBox);

		createScrollPane();

		return rootRightPane;
	}

	private void createScrollPane()
	{
		scrollFilesPane = new ScrollPane();
		scrollFilesPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollFilesPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

		scrollFilesPane.setOnDragOver(event ->
		{
			if (event.getGestureSource() != scrollFilesPane && event.getDragboard().hasFiles())
			{
				event.acceptTransferModes(TransferMode.COPY);
			}

			event.consume();
		});
		scrollFilesPane.setOnDragDropped(event ->
		{
			Dragboard dragboard = event.getDragboard();
			boolean success = false;
			if (dragboard.hasFiles())
			{
				for (File file : dragboard.getFiles())
				{
					clientController.copyFile(file, curFolder);
				}
				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});
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
