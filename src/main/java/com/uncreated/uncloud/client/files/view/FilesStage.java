package com.uncreated.uncloud.client.files.view;

import com.uncreated.uncloud.client.ClientApp;
import com.uncreated.uncloud.client.StageView;
import com.uncreated.uncloud.client.files.FileInfo;
import com.uncreated.uncloud.client.files.FilesController;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class FilesStage
		extends StageView<FilesController>
		implements FilesView
{
	private static final String ICON_FOLDER = "src/main/resources/icons/";
	private static final String[] ICONS = {
			"client_file.png",
			"server_file.png",
			"client_server_file.png",
			"server_folder.png",
			"client_server_folder.png",
			"client_folder.png",
			"create_folder.png",
			"back_folder.png",
			"add_file.png",
			"download.png",
			"upload.png",
			"delete_client.png",
			"delete_server.png",
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

	private FileInfo selectedFile;
	private ArrayList<FileInfo> files;
	private ToggleButton selectedButton;

	public FilesStage(ClientApp app)
	{
		super(app);

		setController(app.getFilesController());
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
			news("Can not to read interface components. " + e.getMessage());
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

	@Override
	public void showFolder(ArrayList<FileInfo> files, boolean rootFolder)
	{
		//hide progressbar
		selectFile(null);
		VBox filesPane = new VBox();

		if (root.getOnKeyPressed() == null)
		{
			root.setOnKeyPressed(event ->
			{
				System.out.println(event.getCode().toString());
				if (event.getCode() == KeyCode.F5)
				{
					controller.updateFiles();
				}
			});
		}

		filesPane.setSpacing(10);
		if (!rootFolder)
		{
			ToggleButton backButton = customButton("..", images.get("back_folder.png"));
			backButton.setAlignment(Pos.TOP_LEFT);
			backButton.setOnMouseClicked(event ->
			{
				if (selectedButton != null && selectedButton == backButton)
				{
					controller.openFolder(null);
				}
				else
				{
					if (selectedButton != null)
					{
						selectedButton.setEffect(null);
					}

					selectFile(null);
					backButton.setEffect(blueShadow);
					selectedButton = backButton;
				}
			});
			filesPane.getChildren().add(backButton);
		}
		for (FileInfo file : files)
		{
			StringBuilder img = new StringBuilder();
			if (file.isDownloaded())
			{
				img.append("client_");
			}
			if (file.isUploaded())
			{
				img.append("server_");
			}
			img.append(file.isDirectory() ? "folder.png" : "file.png");
			ToggleButton button = customButton(file.getName(), images.get(img.toString()));
			button.setTooltip(new Tooltip(file.getInfo()));
			setButtonSelectEvent(button, file);
			filesPane.setAlignment(Pos.CENTER_LEFT);
			filesPane.getChildren().add(button);
		}

		scrollFilesPane.setContent(filesPane);

		rootRightPane.getChildren().clear();
		rootRightPane.setCenter(scrollFilesPane);
	}

	@Override
	public void onFailRequest(String message)
	{
		//hide progress bar
		news(message);
	}

	@Override
	public void onProgress(int value, int max, String unit)
	{
		//set progress bar
		if (value == 0)//show
		{

		}
	}

	private void selectFile(FileInfo fileInfo)
	{
		selectedFile = fileInfo;
		controlPane.getChildren().clear();
		controlPane.getChildren().addAll(createFolderButton, addFileButton);
		if (fileInfo != null)
		{
			if (fileInfo.isDownloadAny())
			{
				controlPane.getChildren().add(downloadButton);
			}

			if (fileInfo.isUploadAny())
			{
				controlPane.getChildren().add(uploadButton);
			}

			if (fileInfo.isDeleteAnyClient())
			{
				controlPane.getChildren().add(deleteClientButton);
			}
			if (fileInfo.isDeleteAnyServer())
			{
				controlPane.getChildren().add(deleteServerButton);
			}
		}
	}

	private void setButtonSelectEvent(ToggleButton button, FileInfo file)
	{
		button.setOnMouseClicked(event ->
		{
			if (selectedFile == file)
			{
				if (file.isDirectory())
				{
					controller.openFolder(file);
				}
			}
			else
			{
				selectFile(file);
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
	public void onStart(Stage stage)
	{
		super.onStart(stage);
		root = new BorderPane();

		root.setLeft(createLeftPane(stage));
		root.setCenter(createRightPane(stage));

		selectFile(null);
		stage.getScene().setRoot(root);

		controller.updateFiles();
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
			app.openAuthStage();
		});

		leftRootPane.setTop(controlPane);
		leftRootPane.setBottom(logoutButton);

		createControlButtons(stage);

		return leftRootPane;
	}

	private void createControlButtons(Stage stage)
	{
		controlPane.setSpacing(10);

		createFolderButton = customButton(null, images.get("create_folder.png"));
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
					controller.createFolder(name);
				}
			}
		});

		addFileButton = customButton(null, images.get("add_file.png"));
		addFileButton.setTooltip(new Tooltip("Add file on client"));
		controlPane.getChildren().add(addFileButton);
		addFileButton.setOnAction(event ->
		{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select file for upload to server");
			List<File> files = fileChooser.showOpenMultipleDialog(stage);
			controller.copyFile(files);
		});

		downloadButton = customButton(null, images.get("download.png"));
		downloadButton.setTooltip(new Tooltip("Download"));
		controlPane.getChildren().add(downloadButton);
		downloadButton.setOnAction(event ->
		{
			controller.download(selectedFile);
			selectFile(null);
		});

		uploadButton = customButton(null, images.get("upload.png"));
		uploadButton.setTooltip(new Tooltip("Upload"));
		controlPane.getChildren().add(uploadButton);
		uploadButton.setOnAction(event ->
		{
			controller.upload(selectedFile);
			selectFile(null);
		});

		deleteClientButton = customButton(null, images.get("delete_client.png"));
		deleteClientButton.setTooltip(new Tooltip("Delete from client"));
		controlPane.getChildren().add(deleteClientButton);
		deleteClientButton.setOnAction(event ->
		{
			controller.removeFileFromClient(selectedFile);
			selectFile(null);
		});

		deleteServerButton = customButton(null, images.get("delete_server.png"));
		deleteServerButton.setTooltip(new Tooltip("Delete from server"));
		controlPane.getChildren().add(deleteServerButton);
		deleteServerButton.setOnAction(event ->
		{
			controller.removeFileFromServer(selectedFile);
			selectFile(null);
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
					controller.copyFile(file);
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
