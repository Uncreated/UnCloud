package com.uncreated.uncloud.client.view;

import com.uncreated.uncloud.client.ClientApp;
import com.uncreated.uncloud.client.ClientController;
import com.uncreated.uncloud.client.RequestStatus;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class AuthStage
		extends ViewStage
{
	private static final String PREF_REMEMBER = "prefKeyRemember";
	private static final String PREF_LOGIN = "prefKeyLogin";
	private static final String PREF_PASSWORD = "prefKeyPassword";

	private ProgressIndicator progressIndicator;
	private TextField loginTextField;
	private TextField passwordTextField;
	private Button registerButton;
	private Button authButton;
	private CheckBox checkBox;

	private Preferences preferences;
	private boolean autoAuth = false;


	public AuthStage(ClientController clientController)
	{
		super(clientController);
	}

	@Override
	public void onStart(Stage stage)
	{
		super.onStart(stage);
		preferences = Preferences.userNodeForPackage(AuthStage.class);

		Text loginText = new Text("Login:");
		loginTextField = new TextField();
		Text passwordText = new Text("Password:");
		passwordTextField = new TextField();
		registerButton = new Button("Register");
		authButton = new Button("Authorize");

		progressIndicator = new ProgressIndicator();

		HBox hBox = new HBox(registerButton, authButton);
		hBox.setSpacing(15);

		checkBox = new CheckBox("Remember me");

		VBox vBox = new VBox(loginText,
				loginTextField,
				passwordText,
				passwordTextField,
				hBox,
				checkBox,
				progressIndicator);
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

		setLoading(false);

		if (preferences.getBoolean(PREF_REMEMBER, false))
		{
			checkBox.setSelected(true);
			String login = preferences.get(PREF_LOGIN, "");
			String password = preferences.get(PREF_PASSWORD, "");

			loginTextField.setText(login);
			passwordTextField.setText(password);

			if (autoAuth)
			{
				autoAuth = false;
				authButton.fire();
			}
		}

		if (stage.getScene() != null)
		{
			stage.getScene().setRoot(root);
		}
		else
		{
			stage.setScene(new Scene(root, 1, 1));
		}
		stage.setMinHeight(540);
		stage.setMinWidth(960);
		stage.centerOnScreen();
		stage.show();
	}

	public void setAutoAuth(boolean autoAuth)
	{
		this.autoAuth = autoAuth;
	}

	@Override
	public void onFailRequest(RequestStatus requestStatus)
	{
		news(false, requestStatus.getMsg());
	}

	@Override
	public void onAuth(RequestStatus requestStatus)
	{
		setLoading(false);
		if (requestStatus.isOk())
		{
			preferences.putBoolean(PREF_REMEMBER, checkBox.isSelected());
			preferences.put(PREF_LOGIN, checkBox.isSelected() ? loginTextField.getText() : "");
			preferences.put(PREF_PASSWORD, checkBox.isSelected() ? passwordTextField.getText() : "");
			ClientApp.getInstance().openFilesStage();
		}
		else
		{
			news(false, requestStatus.getMsg());
		}
	}

	private void setLoading(boolean on)
	{
		progressIndicator.setVisible(on);
		loginTextField.setDisable(on);
		passwordTextField.setDisable(on);
		registerButton.setDisable(on);
		authButton.setDisable(on);
	}

	@Override
	public void onRegister(RequestStatus requestStatus)
	{
		setLoading(false);
		if (!requestStatus.isOk())
		{
			news(false, requestStatus.getMsg());
		}
		else
		{
			news(true, "You have successfully registered");
		}
	}

	private void onAuthClick(TextField loginTextField, TextField passwordTextField, boolean auth)
	{
		setLoading(true);
		String login = loginTextField.getText();
		String password = passwordTextField.getText();
		if (login.length() > 0 && password.length() > 0)
		{
			if (auth)
			{
				clientController.auth(login, password);
			}
			else
			{
				clientController.register(login, password);
			}
		}
		else
		{
			news(false, "Incorrect login or password");
		}
	}
}
