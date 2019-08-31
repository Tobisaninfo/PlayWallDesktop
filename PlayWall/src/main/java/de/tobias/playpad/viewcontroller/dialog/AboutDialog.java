package de.tobias.playpad.viewcontroller.dialog;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationInfo;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// TODO Extract into lang file
public class AboutDialog extends NVC {

	@FXML
	private Label libsLabel;

	@FXML
	private HBox codeContainer;

	@FXML
	private Label versionLabel;

	@FXML
	private Label authorLabel;

	@FXML
	private AnchorPane rootPane;

	@FXML
	private HBox websiteContainer;

	@FXML
	private Label graphicsLabel;

	private Window owner;

	public AboutDialog(Window owner) {
		this.owner = owner;
		load("view/dialog", "About", Localization.getBundle());
		NVCStage stage = applyViewControllerToStage().initOwner(owner).initModality(Modality.WINDOW_MODAL);
		addCloseKeyShortcut(stage::close);
	}

	@Override
	public void init() {
		FontIcon icon = new FontIcon(FontAwesomeType.CLOSE);
		rootPane.getChildren().add(icon);
		AnchorPane.setLeftAnchor(icon, 14.0);
		AnchorPane.setTopAnchor(icon, 14.0);

		icon.setOnMouseClicked((e) -> getStageContainer().ifPresent(NVCStage::close));

		ApplicationInfo info = ApplicationUtils.getApplication().getInfo();
		versionLabel.setText(info.getVersion());
		authorLabel.setText(info.getAuthor());
		graphicsLabel.setText(Localization.getString(Strings.UI_DIALOG_ABOUT_GRAPHICS));
		libsLabel.setText(Localization.getString(Strings.UI_DIALOG_ABOUT_LIBRARIES));

		Hyperlink websiteLink = new Hyperlink(Localization.getString(Strings.UI_DIALOG_ABOUT_WEBSITE));
		websiteLink.setPadding(Insets.EMPTY);
		websiteLink.setFocusTraversable(false);
		websiteLink.setOnAction(e -> {
			String url = ApplicationUtils.getApplication().getUserInfo(AppUserInfoStrings.class).website();
			openWebsite(url);
		});
		websiteContainer.getChildren().add(websiteLink);

		Hyperlink codeLink = new Hyperlink(Localization.getString(Strings.UI_DIALOG_ABOUT_CODE));
		codeLink.setPadding(Insets.EMPTY);
		codeLink.setFocusTraversable(false);
		codeLink.setOnAction(e -> {
			String url = ApplicationUtils.getApplication().getUserInfo(AppUserInfoStrings.class).repository();
			openWebsite(url);
		});
		codeContainer.getChildren().add(codeLink);
	}

	private void openWebsite(String url) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				Logger.error(e);
			}
		}
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());
		stage.setResizable(false);
		stage.initStyle(StageStyle.TRANSPARENT);

		stage.setWidth(650);
		stage.setHeight(400);

		double centerXPosition = owner.getX() + owner.getWidth() / 2d;
		double centerYPosition = owner.getY() + owner.getHeight() / 2d;

		stage.setX(centerXPosition - stage.getWidth() / 2d);
		stage.setY(centerYPosition - stage.getHeight() / 2d);

		stage.getScene().setFill(Color.TRANSPARENT);

		PlayPadPlugin.styleable().applyStyle(stage);
	}
}
