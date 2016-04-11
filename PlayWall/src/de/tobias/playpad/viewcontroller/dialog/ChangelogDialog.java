package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class ChangelogDialog extends ViewController {

	@FXML private WebView contentView;
	private JSBridge bridge;

	public ChangelogDialog() {
		super("changelogDialog", "de/tobias/playpad/assets/dialog/", null, PlayPadMain.getUiResourceBundle());

		if (ApplicationUtils.getApplication().isUpdated()) {
			bridge = new JSBridge();

			contentView.getEngine().getLoadWorker().stateProperty().addListener((ov, oldState, newState) ->
			{
				if (newState == State.SUCCEEDED) {
					JSObject window = (JSObject) contentView.getEngine().executeScript("window");
					window.setMember("app", bridge);
					getStage().show();
				}
			});

			String url = ApplicationUtils.getApplication().getInfo().getUserInfo().getString(AppUserInfoStrings.CHANGELOG_URL) + "&version="
					+ ApplicationUtils.getApplication().getOldVersionNumber();
			contentView.getEngine().load(url);
			getStage().show();
		}
	}

	@Override
	public void init() {
		addCloseKeyShortcut(() -> getStage().close());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setWidth(500);
		stage.setMinWidth(500);
		stage.setMaxWidth(500);

		stage.setMinHeight(700);
		stage.setTitle(Localization.getString(Strings.UI_Window_Changelog_Title));
		stage.setAlwaysOnTop(true);
	}

	public class JSBridge {

		public void close() {
			getStage().close();
		}
	}
}
