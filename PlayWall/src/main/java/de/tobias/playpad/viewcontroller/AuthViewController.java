package de.tobias.playpad.viewcontroller;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

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

	private AuthBasedRunnable authBasedRunnable;

	public AuthViewController(Window owner, String info, AuthBasedRunnable authBasedRunnable) {
		load("view/dialog", "AuthDialog", Localization.getBundle());
		infoLabel.setText(info);

		final NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);

		this.authBasedRunnable = authBasedRunnable;
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setMinWidth(500);
		stage.setMinHeight(250);
		stage.setWidth(500);
		stage.setHeight(250);
		stage.setTitle(Localization.getString(Strings.UI_DIALOG_LOGIN_TITLE));

		PlayPadPlugin.styleable().applyStyle(stage);
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
			Logger.error(e);
			showErrorMessage(e.getMessage());
		}
	}
}
