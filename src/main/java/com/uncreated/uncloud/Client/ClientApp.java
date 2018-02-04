package com.uncreated.uncloud.Client;

import com.uncreated.uncloud.Server.RequestException;
import com.uncreated.uncloud.Server.storage.FileInfo;
import com.uncreated.uncloud.Server.storage.FileTransfer;
import com.uncreated.uncloud.Server.storage.UserFiles;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.uncreated.uncloud.Common.PART_SIZE;

public class ClientApp extends Application
{
	ClientController clientController = new ClientController();

	VBox buttonsPane;
	FlowPane filesPane;

	Stage stage;
	FileInfo selectedFile;

	public static void main(String[] args)
	{
		launch(ClientApp.class, args);
		/*ClientController controller = new ClientController();

		try
		{
			controller.register("masha", "siski");
		} catch (RequestException e)
		{
			e.printStackTrace();
		}

		try
		{
			Session session = controller.auth("masha", "siski");

			try
			{
				UserFiles userFiles = controller.files();
				FileInfo[] files =  userFiles.getFiles();
				FileInfo fileInfo = files[0];
				try
				{
					FileTransfer fileTransfer = controller.getFile(fileInfo.getPath(), 0);
					try
					{
						controller.removeFile(fileInfo.getPath());
						try
						{
							controller.setFile(fileTransfer);
						} catch (RequestException e)
						{
							e.printStackTrace();
						}
					} catch (RequestException e)
					{
						e.printStackTrace();
					}
				} catch (RequestException e)
				{
					e.printStackTrace();
				}
			} catch (RequestException e)
			{
				e.printStackTrace();
			}
		} catch (RequestException e)
		{
			e.printStackTrace();
		}*/
	}

	private void stageFiles()
	{
		UserFiles userFiles;
		try
		{
			userFiles = clientController.files();
		} catch (RequestException e)
		{
			Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load file list", ButtonType.OK);
			alert.showAndWait();
			return;
		}

		Button button;

		BorderPane root = new BorderPane();

		VBox leftPane = new VBox();
		ScrollPane rightPane = new ScrollPane();
		rightPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

		root.setLeft(leftPane);
		root.setCenter(rightPane);

		//buttons
		leftPane.setPadding(new Insets(10));
		leftPane.setSpacing(10);
		button = new Button("Add File");
		button.setPrefWidth(100);
		button.setPrefHeight(100);
		leftPane.getChildren().add(button);

		button = new Button("Get File");
		button.setPrefWidth(100);
		button.setPrefHeight(100);
		leftPane.getChildren().add(button);
		button.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (selectedFile != null)
				{
					Alert alert = new Alert(Alert.AlertType.ERROR,
							"File was not saved",
							ButtonType.OK);
					try
					{
						FileTransfer fileTransfer = clientController.getFile(selectedFile.getPath(), 0);
						File file = new File("C:/localFiles"+fileTransfer.getPath());
						file.getParentFile().mkdirs();
						if (!file.exists())
							file.createNewFile();

						int size = fileTransfer.getData().length;

						if (size > 0)
						{
							long shift = PART_SIZE * fileTransfer.getPart();
							if (size > PART_SIZE)
								size = PART_SIZE;

							FileOutputStream outputStream = new FileOutputStream( file);
							outputStream.write(fileTransfer.getData(), (int) shift, size);
							outputStream.close();
						}
						alert = new Alert(Alert.AlertType.INFORMATION,
								"File was successfully saved",
								ButtonType.OK);
					} catch (RequestException | IOException e)
					{
						e.printStackTrace();
					}
					alert.showAndWait();
				}
			}
		});

		button = new Button("Remove File");
		button.setPrefWidth(100);
		button.setPrefHeight(100);
		leftPane.getChildren().

				add(button);

		button.setOnAction(new EventHandler<ActionEvent>()

		{
			@Override
			public void handle(ActionEvent event)
			{
				if (selectedFile != null)
				{
					try
					{
						clientController.removeFile(selectedFile.getPath());
						selectedFile = null;
						stageFiles();
					} catch (RequestException e)
					{
						e.printStackTrace();
					}
				}
			}
		});

		//Files
		FlowPane filesPane = new FlowPane();

		filesPane.setPadding(new

				Insets(10, 0, 10, 10));
		filesPane.setHgap(10);
		filesPane.setVgap(10);
		for (
				FileInfo fileInfo : userFiles.getFiles())

		{
			button = new Button(fileInfo.getPath() + "\n" + fileInfo.getSize() + "b");
			button.setPrefWidth(100);
			button.setPrefHeight(100);
			filesPane.getChildren().add(button);
			button.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					selectedFile = fileInfo;
				}
			});
		}

		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(filesPane);
		rightPane.setContent(borderPane);

		stage.getScene().

				setRoot(root);
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

		registerButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				String login = loginTextField.getText();
				String password = passwordTextField.getText();
				Alert alert = new Alert(Alert.AlertType.WARNING,
						"Incorrect login or password",
						ButtonType.OK);
				if (login.length() > 0 && password.length() > 0)
				{
					try
					{
						clientController.register(login, password);
						alert = new Alert(Alert.AlertType.INFORMATION,
								"You have successfully registered",
								ButtonType.OK);
					} catch (RequestException e)
					{
						alert.showAndWait();
					}
				}
				alert.showAndWait();
			}
		});

		authButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				String login = loginTextField.getText();
				String password = passwordTextField.getText();
				Alert alert = new Alert(Alert.AlertType.WARNING,
						"Incorrect login or password",
						ButtonType.OK);
				if (login.length() > 0 && password.length() > 0)
				{
					try
					{
						clientController.auth(login, password);
						stageFiles();
					} catch (RequestException e)
					{
						alert.showAndWait();
					}
				} else
					alert.showAndWait();
			}
		});

		stage.setScene(new Scene(root, 1, 1));
		stage.setMinHeight(540);
		stage.setMinWidth(960);
		stage.centerOnScreen();
		stage.show();
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		stageAuth(primaryStage);
	}
}
