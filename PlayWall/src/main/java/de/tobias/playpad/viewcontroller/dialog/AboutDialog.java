package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.profile.Profile;
import de.tobias.utils.application.ApplicationInfo;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.ui.NVC;
import de.tobias.utils.ui.NVCStage;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
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
		load("view/dialog", "About", PlayPadMain.getUiResourceBundle());
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
		graphicsLabel.setText(Localization.getString(Strings.UI_Dialog_About_Graphics));
		libsLabel.setText(Localization.getString(Strings.UI_Dialog_About_Libraries));

		Hyperlink websiteLink = new Hyperlink(Localization.getString(Strings.UI_Dialog_About_Website));
		websiteLink.setPadding(Insets.EMPTY);
		websiteLink.setFocusTraversable(false);
		websiteLink.setOnAction(e -> {
			String url = info.getUserInfo().get(AppUserInfoStrings.WEBSITE).toString();
			openWebsite(url);
		});
		websiteContainer.getChildren().add(websiteLink);

		Hyperlink codeLink = new Hyperlink(Localization.getString(Strings.UI_Dialog_About_Code));
		codeLink.setPadding(Insets.EMPTY);
		codeLink.setFocusTraversable(false);
		codeLink.setOnAction(e -> {
			String url = info.getUserInfo().get(AppUserInfoStrings.REPOSITORY).toString();
			openWebsite(url);
		});
		codeContainer.getChildren().add(codeLink);
	}

	private void openWebsite(String url) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException | URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);
		stage.setResizable(false);
		stage.initStyle(StageStyle.TRANSPARENT);

		stage.setWidth(650);
		stage.setHeight(400);

		double centerXPosition = owner.getX() + owner.getWidth() / 2d;
		double centerYPosition = owner.getY() + owner.getHeight() / 2d;

		stage.setX(centerXPosition - stage.getWidth() / 2d);
		stage.setY(centerYPosition - stage.getHeight() / 2d);

		stage.getScene().setFill(Color.TRANSPARENT);

		ModernGlobalDesign design = Profile.currentProfile().getProfileSettings().getDesign();
		PlayPadPlugin.getModernDesignHandler().getModernGlobalDesignHandler().applyCss(design, stage);
	}
}
