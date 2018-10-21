package de.tobias.playpad.viewcontroller;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.server.LoginException;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.Session;
import de.tobias.playpad.server.SessionDelegate;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.ui.NVC;
import de.tobias.utils.ui.NVCStage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static de.tobias.utils.util.Localization.getString;

/**
 * Created by tobias on 21.02.17.
 */
public class LoginViewController extends NVC implements SessionDelegate {

	@FXML
	private Label infoLabel;
	@FXML
	private ImageView imageView;

	@FXML
	private TextField usernameTextField;
	@FXML
	private PasswordField passwordTextField;

	private Session session;

	public LoginViewController() {
	}

	@Override
	public void init() {
		App app = ApplicationUtils.getApplication();

		// Setup launchscreen labels and image
		infoLabel.setText(getString(Strings.UI_Dialog_Launch_Info, app.getInfo().getName(), app.getInfo().getVersion()));
		try {
			imageView.setImage(new Image(LaunchDialog.IMAGE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initStage(Stage stage) {
		stage.setOnCloseRequest(e -> Platform.exit());

		stage.setTitle(getString(Strings.UI_Dialog_Login_Title));
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setResizable(false);
		stage.setWidth(650);
		stage.setHeight(300);
		stage.centerOnScreen();
	}

	@FXML
	private void loginHandler(ActionEvent event) {
		Server server = PlayPadPlugin.getServerHandler().getServer();

		String username = usernameTextField.getText();
		String password = passwordTextField.getText();

		try {
			String key = server.getSession(username, password);
			session = new Session(key);
			session.save();
			getStageContainer().ifPresent(NVCStage::close);
		} catch (IOException | LoginException e) {
			e.printStackTrace();
			showErrorMessage(e.getMessage());
		}
	}

	@FXML
	private void registerHandler(ActionEvent event) {
		URI uri = URI.create(ApplicationUtils.getApplication().getInfo().getUserInfo().get(AppUserInfoStrings.ACCOUNT_REGISTER).toString());
		try {
			Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Session getSession() {
		load("view/dialog", "LoginDialog", PlayPadMain.getUiResourceBundle());
		applyViewControllerToStage();

		Optional<NVCStage> stage = getStageContainer();
		stage.ifPresent(NVCStage::showAndWait);
		return session;
	}
}
