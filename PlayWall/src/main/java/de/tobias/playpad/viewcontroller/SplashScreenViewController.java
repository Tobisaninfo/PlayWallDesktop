package de.tobias.playpad.viewcontroller;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.PlayPadImpl;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.initialize.PlayPadInitializeTask;
import de.tobias.playpad.initialize.PlayPadInitializer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreenViewController extends NVC implements PlayPadInitializer.Listener {

	@FXML
	private ProgressBar progressBar;

	private int maxValue;
	private int currentValue;

	public static void show(PlayPadImpl instance, Stage primaryStage) {
		new SplashScreenViewController(instance, primaryStage);
	}

	private SplashScreenViewController(PlayPadImpl instance, Stage primaryStage) {
		load("view", "SplashScreen", controller -> {
			controller.applyViewControllerToStage(primaryStage).show();
			instance.startup(new LoginViewController(), this);
		});
	}

	@Override
	public void initStage(Stage stage) {
		stage.initStyle(StageStyle.UNDECORATED);
		PlayPadPlugin.styleable().applyStyle(stage);
	}

	@Override
	public void startLoading(int count) {
		this.maxValue = count;
	}

	@Override
	public void startTask(PlayPadInitializeTask task) {

	}

	@Override
	public void finishTask(PlayPadInitializeTask task) {
		currentValue++;

		progressBar.setProgress(currentValue / (double) maxValue);
	}

	@Override
	public void finishLoading() {
		Platform.runLater(() -> {
			closeStage();
			new LaunchDialog(new Stage()); // Show Launch Stage
		});
	}
}
