package de.tobias.playpad.viewcontroller;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.server.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by tobias on 21.02.17.
 */
public class AuthViewController extends NVC {

	public interface AuthBasedRunnable {
		boolean run(String username, String password) throws IOException;
	}

	@FXML
	private TextField usernameTextField;
	@FXML
	private PasswordField passwordTextField;
	@FXML
	private Button loginButton;
	@FXML
	private Label infoLabel;

	private Session session;

	private AuthBasedRunnable authBasedRunnable;

	public AuthViewController(String info, AuthBasedRunnable authBasedRunnable) {
		load("view/dialog", "AuthDialog", PlayPadMain.getUiResourceBundle());
		infoLabel.setText(info);

		applyViewControllerToStage();

		this.authBasedRunnable = authBasedRunnable;
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(500);
		stage.setMinHeight(250);
		stage.setWidth(500);
		stage.setHeight(250);
		stage.setTitle(Localization.getString(Strings.UI_Dialog_Login_Title));

		PlayPadPlugin.getDesignHandler().applyStyleSheet(stage);
	}

	@FXML
	private void loginHandler(ActionEvent event) {
		String username = usernameTextField.getText();
		String password = passwordTextField.getText();

		try {
			boolean success = authBasedRunnable.run(username, password);
			if (success) {
				closeStage();
			}
		} catch (IOException e) {
			e.printStackTrace();
			showErrorMessage(e.getMessage());
		}
	}
}
