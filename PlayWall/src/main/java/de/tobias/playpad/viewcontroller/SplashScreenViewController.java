package de.tobias.playpad.viewcontroller;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.PlayPadImpl;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.initialize.PlayPadInitializeTask;
import de.tobias.playpad.initialize.PlayPadInitializer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.dialog.ExceptionDialog;

public class SplashScreenViewController extends NVC implements PlayPadInitializer.Listener {

	@FXML
	private Label titleLabel;
	@FXML
	private Label versionLabel;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label loadingLabel;

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
	public void init() {
		App app = ApplicationUtils.getApplication();
		titleLabel.setText(app.getInfo().getName());
		versionLabel.setText(app.getInfo().getVersion());
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
		Platform.runLater(() -> loadingLabel.setText(task.name()));
	}

	@Override
	public void finishTask(PlayPadInitializeTask task) {
		currentValue++;
		Platform.runLater(() -> progressBar.setProgress(currentValue / (double) maxValue));
	}

	@Override
	public void finishLoading() {
		Platform.runLater(() -> {
			closeStage();
			new LaunchDialog(new Stage()); // Show Launch Stage
		});
	}

	@Override
	public void abortedLoading() {
		Platform.setImplicitExit(false);
		Platform.runLater(this::closeStage);
	}

	@Override
	public void errorLoading(PlayPadInitializeTask task, Exception e) {
		Platform.runLater(() -> {
			ExceptionDialog dialog = new ExceptionDialog(e);
			dialog.setHeaderText("Error while loading PlayWall (" + task.name() + ")");
			dialog.initOwner(getContainingWindow());
			dialog.showAndWait();
			Platform.exit();
		});
	}
}
