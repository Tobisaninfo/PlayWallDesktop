package de.tobias.playpad.viewcontroller;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.server.LoginException;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.Session;
import de.tobias.playpad.server.SessionDelegate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static de.thecodelabs.utils.util.Localization.getString;

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
		this.session = Session.EMPTY;
	}

	@Override
	public void init() {
		App app = ApplicationUtils.getApplication();

		// Setup launchscreen labels and image
		infoLabel.setText(getString(Strings.UI_Dialog_Launch_Info, app.getInfo().getName(), app.getInfo().getVersion()));
		try {
			imageView.setImage(new Image(LaunchDialog.IMAGE));
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadPlugin.styleable().applyStyle(stage);

		stage.setTitle(getString(Strings.UI_Dialog_Login_Title));
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.initModality(Modality.APPLICATION_MODAL);

		stage.setResizable(false);
		stage.setWidth(650);
		stage.setHeight(350);
		stage.centerOnScreen();

		usernameTextField.requestFocus();
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
			Logger.error(e);
			showErrorMessage(e.getMessage());
		}
	}

	@FXML
	private void registerHandler(ActionEvent event) {
		URI uri = URI.create(ApplicationUtils.getApplication().getInfo().getUserInfo().get(AppUserInfoStrings.ACCOUNT_REGISTER).toString());
		try {
			Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@FXML
	private void skipHandler(ActionEvent event) {
		closeStage();
	}

	@Override
	public Session getSession() {
		load("view/dialog", "LoginDialog", Localization.getBundle());
		applyViewControllerToStage();

		Optional<NVCStage> stage = getStageContainer();
		stage.ifPresent(NVCStage::showAndWait);
		return session;
	}
}
