package com.uncreated.uncloud.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public abstract class StageView<T extends Controller>
		implements View
{
	protected ClientApp app;
	protected T controller;

	public StageView(ClientApp app)
	{
		this.app = app;
	}

	public void onStart(Stage stage)
	{
		controller.onAttach(this);
	}

	public void onPause()
	{
		controller.onDetach();
	}

	protected void setController(T controller)
	{
		this.controller = controller;
	}

	protected void news(String msg)
	{
		Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
		alert.showAndWait();
	}

	@Override
	public void call(Runnable runnable)
	{
		Platform.runLater(runnable);
	}
}
