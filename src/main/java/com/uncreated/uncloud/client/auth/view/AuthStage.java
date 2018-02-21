package com.uncreated.uncloud.client.auth.view;

import com.uncreated.uncloud.client.ClientApp;
import com.uncreated.uncloud.client.StageView;
import com.uncreated.uncloud.client.auth.AuthController;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Set;
import java.util.prefs.Preferences;

public class AuthStage
		extends StageView<AuthController>
		implements AuthView
{
	private static final String KEY_AUTH_INF = "keyAuthInf";

	private static DropShadow grayShadow = new DropShadow(20, Color.BLACK);

	private BorderPane root;
	private ProgressIndicator progressIndicator;
	private TextField loginTextField;
	private TextField passwordTextField;
	private Button registerButton;
	private Button authButton;
	private Button changeUser;

	private Preferences preferences;
	private Set<String> logins;

	private boolean withPass = true;
	private boolean loginInput = true;
	private boolean passwordInput = true;

	public AuthStage(ClientApp app)
	{
		super(app);

		preferences = Preferences.userNodeForPackage(AuthStage.class);

		setController(app.getAuthController());

		Text loginText = new Text("Login:");
		loginTextField = new TextField();
		Text passwordText = new Text("Password:");
		passwordTextField = new TextField();
		registerButton = new Button("Register");
		authButton = new Button("Authorize");
		changeUser = new Button("Change user");

		progressIndicator = new ProgressIndicator();

		HBox hBox = new HBox(registerButton, authButton);
		hBox.setSpacing(15);

		VBox vBox = new VBox(loginText,
				loginTextField,
				passwordText,
				passwordTextField,
				hBox,
				progressIndicator);
		vBox.setMaxWidth(250);
		vBox.setFillWidth(false);
		vBox.setSpacing(10);
		vBox.setAlignment(Pos.CENTER);

		root = new BorderPane();
		root.setCenter(vBox);

		root.setPadding(new Insets(20));

		root.setBottom(changeUser);
		changeUser.setAlignment(Pos.BOTTOM_LEFT);
		changeUser.setOnMouseClicked(event ->
		{
			Stage dialogStage = new Stage(StageStyle.UTILITY);
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setTitle("Select User");

			ScrollPane scrollPane = new ScrollPane();
			scrollPane.setFitToWidth(true);
			scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			VBox rootPane = new VBox();
			rootPane.setMaxHeight(400);

			VBox contentPane = new VBox();
			contentPane.setSpacing(10);
			scrollPane.setContent(contentPane);
			scrollPane.setFitToHeight(false);

			BorderPane newUserPane = makeListItem("New User", event1 ->
			{
				dialogStage.hide();
				selectNewUser();
			});
			contentPane.getChildren().add(newUserPane);
			for (String login : logins)
			{
				contentPane.getChildren().add(makeListItem(login, event1 ->
				{
					dialogStage.hide();
					controller.selectUser(login);
				}));
			}
			contentPane.setFillWidth(true);
			contentPane.setAlignment(Pos.TOP_CENTER);

			rootPane.getChildren().addAll(newUserPane, scrollPane);

			Scene dialogScene = new Scene(rootPane);
			dialogStage.setScene(dialogScene);
			dialogStage.setMinWidth(200);
			dialogStage.setMinHeight(300);
			dialogStage.setMaxWidth(200);
			dialogStage.setMaxHeight(800);
			dialogStage.centerOnScreen();
			dialogStage.show();
		});

		registerButton.setOnAction(event ->
		{
			onRegisterClick();
		});

		authButton.setOnAction(event ->
		{
			onAuthClick();
		});
	}

	private BorderPane makeListItem(String login, EventHandler<MouseEvent> eventHandler)
	{
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(5));
		Label loginLabel = new Label(login);
		borderPane.setCenter(loginLabel);

		borderPane.setOnMouseClicked(eventHandler);

		borderPane.addEventHandler(MouseEvent.MOUSE_ENTERED,
				event ->
				{
					borderPane.setEffect(grayShadow);
				});

		borderPane.addEventHandler(MouseEvent.MOUSE_EXITED,
				event ->
				{
					borderPane.setEffect(null);
				});
		return borderPane;
	}

	@Override
	public void onStart(Stage stage)
	{
		super.onStart(stage);

		if (stage.getScene() != null)
		{
			stage.getScene().setRoot(root);
		}
		else
		{
			stage.setScene(new Scene(root, 1, 1));
			stage.setMinHeight(540);
			stage.setMinWidth(960);
			stage.centerOnScreen();
			stage.show();
		}
	}

	@Override
	public String getJsonAuthInf()
	{
		return preferences.get(KEY_AUTH_INF, null);
	}

	@Override
	public void setJsonAuthInf(String json)
	{
		preferences.put(KEY_AUTH_INF, json);
	}

	@Override
	public void setUsers(Set<String> logins)
	{
		this.logins = logins;
	}

	@Override
	public void selectUser(String login, boolean autoAuth)
	{
		withPass = false;

		registerButton.setVisible(false);

		loginTextField.setText(login);
		loginInput = false;
		loginTextField.setEditable(false);

		passwordTextField.setText("*****");
		passwordInput = false;
		passwordTextField.setEditable(false);

		if (autoAuth)
		{
			authButton.fire();
		}
	}

	@Override
	public void onAuthOk()
	{
		hideLoading();
		app.openFilesStage();
	}

	@Override
	public void onRegisterOk()
	{
		hideLoading();
		news("You are successfully registered");
	}

	@Override
	public void onRequestTimeout()
	{
		hideLoading();
		news("Request timed out");
	}

	@Override
	public void onRequestIncorrect()
	{
		hideLoading();
		news("Incorrect login or password");
		passwordTextField.setText("");
		passwordInput = true;
		passwordTextField.setEditable(passwordInput);
		withPass = true;
	}

	private void showLoading()
	{
		progressIndicator.setVisible(true);
		loginTextField.setEditable(false);
		passwordTextField.setEditable(false);
		registerButton.setDisable(true);
		authButton.setDisable(true);
		changeUser.setDisable(true);
	}

	private void hideLoading()
	{
		progressIndicator.setVisible(false);
		loginTextField.setEditable(loginInput);
		passwordTextField.setEditable(passwordInput);
		registerButton.setDisable(false);
		authButton.setDisable(false);
		changeUser.setDisable(false);
	}

	private void selectNewUser()
	{
		withPass = true;

		registerButton.setVisible(true);

		loginTextField.setText("");
		loginInput = true;
		loginTextField.setEditable(loginInput);

		passwordTextField.setText("");
		passwordInput = true;
		passwordTextField.setEditable(passwordInput);
	}

	private void onAuthClick()
	{
		String login = loginTextField.getText();
		if (login.length() > 0)
		{
			if (withPass)
			{
				String password = passwordTextField.getText();
				if (password.length() > 0)
				{
					showLoading();
					controller.auth(login, password);
				}
				else
				{
					news("Empty password");
				}
			}
			else
			{
				showLoading();
				controller.auth();
			}
		}
		else
		{
			news("Empty login");
		}
	}

	public void onRegisterClick()
	{
		String login = loginTextField.getText();
		if (login.length() > 0)
		{
			String password = passwordTextField.getText();
			if (password.length() > 0)
			{
				showLoading();
				controller.register(login, password);
			}
			else
			{
				news("Empty password");
			}
		}
		else
		{
			news("Empty login");
		}
	}
}
