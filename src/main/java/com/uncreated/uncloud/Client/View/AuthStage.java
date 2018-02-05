package com.uncreated.uncloud.Client.View;

import com.uncreated.uncloud.Client.ClientController;
import com.uncreated.uncloud.Client.RequestStatus;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AuthStage extends ViewStage
{
	private ProgressIndicator progressIndicator;
	private TextField loginTextField;
	private TextField passwordTextField;
	private Button registerButton;
	private Button authButton;

	public AuthStage(ClientController clientController)
	{
		super(clientController);
	}

	@Override
	public void onStart(Stage stage)
	{
		Text loginText = new Text("Login:");
		loginTextField = new TextField();
		Text passwordText = new Text("Password:");
		passwordTextField = new TextField();
		registerButton = new Button("Register");
		authButton = new Button("Authorize");

		progressIndicator = new ProgressIndicator();

		HBox hBox = new HBox(registerButton, authButton);
		hBox.setSpacing(15);

		VBox vBox = new VBox(loginText, loginTextField, passwordText, passwordTextField, hBox, progressIndicator);
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

		stage.setScene(new Scene(root, 1, 1));
		stage.setMinHeight(540);
		stage.setMinWidth(960);
		stage.centerOnScreen();
		stage.show();
	}

	@Override
	public void onAuth(RequestStatus requestStatus)
	{
		setLoading(false);

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
	}

	private void onAuthClick(TextField loginTextField, TextField passwordTextField, boolean auth)
	{
		setLoading(true);
		String login = loginTextField.getText();
		String password = passwordTextField.getText();
		if (login.length() > 0 && password.length() > 0)
		{
			if (auth)
				clientController.auth(login, password);
			else
				clientController.register(login, password);
		} else
			ClientApp.news(false, "Incorrect login or password");
	}
}
