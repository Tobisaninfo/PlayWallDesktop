package de.tobias.playpad.viewcontroller.option.pad;

import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.conntent.UnkownPadContentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.IPadSettingsViewController;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class PadSettingsViewController extends ViewController implements IPadSettingsViewController {

	private Pad pad;

	@FXML private TabPane tabPane;
	protected List<PadSettingsTabViewController> tabs = new ArrayList<>();

	@FXML private Button finishButton;

	public PadSettingsViewController(Pad pad, Window owner) {
		super("padSettingsView", "de/tobias/playpad/assets/view/option/pad/", null, PlayPadMain.getUiResourceBundle());
		this.pad = pad;

		addTab(new GeneralPadTabViewController(pad));
		addTab(new LayoutPadTabViewController(pad));
		addTab(new PlayerPadTabViewController(pad));
		addTab(new TriggerPadTabViewController(pad));

		if (pad.getContent() != null) {
			try {
				// Get Pad Type specific tab
				PadContentConnect padContentConnect = PadContentRegistry
						.getPadContentConnect(pad.getContent().getType());
				PadSettingsTabViewController contentTab = padContentConnect.getSettingsViewController(pad);
				if (contentTab != null)
					addTab(contentTab);
			} catch (UnkownPadContentException e) {
				e.printStackTrace();
			}
		}

		// Listener
		PlayPadPlugin.getImplementation().getPadSettingsViewListener().forEach(l -> {
			try {
				l.onInit(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		getStage().initOwner(owner);

		// Show Current Settings
		showCurrentSettings();
		setTitle(pad);
	}

	private void setTitle(Pad pad) {
		if (pad.getStatus() != PadStatus.EMPTY) {
			getStage().setTitle(
					Localization.getString(Strings.UI_Window_PadSettings_Title, pad.getIndexReadable(), pad.getName()));
		} else {
			getStage().setTitle(
					Localization.getString(Strings.UI_Window_PadSettings_Title_Empty, pad.getIndexReadable()));
		}
	}

	@Override
	public void init() {
		addCloseKeyShortcut(() -> finishButton.fire());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(650);
		stage.setMinHeight(550);

		Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	private void showCurrentSettings() {
		for (PadSettingsTabViewController padSettingsTabViewController : tabs) {
			padSettingsTabViewController.loadSettings(pad);
		}
	}

	@Override
	public void addTab(PadSettingsTabViewController controller) {
		tabs.add(controller);

		Tab tab = new Tab(controller.getName(), controller.getParent());
		tabPane.getTabs().add(tab);
	}

	@Override
	public Pad getPad() {
		return pad;
	}

	@Override
	public boolean closeRequest() {
		saveChanges();

		// Listener
		PlayPadPlugin.getImplementation().getPadSettingsViewListener().forEach(l -> l.onClose(this));
		return true;
	}

	private void saveChanges() {
		for (PadSettingsTabViewController controller : tabs) {
			controller.saveSettings(pad);
		}
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		saveChanges();
		// Listener
		PlayPadPlugin.getImplementation().getPadSettingsViewListener().forEach(l -> l.onClose(this));
		getStage().close();
	}

}
