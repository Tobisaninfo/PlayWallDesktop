package de.tobias.playpad.viewcontroller.option.pad;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.content.Playlistable;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.viewcontroller.IPadSettingsViewController;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class PadSettingsViewController extends NVC implements IPadSettingsViewController {

	private final Pad pad;

	@FXML
	private TabPane tabPane;
	private final List<PadSettingsTabViewController> tabs = new ArrayList<>();

	@FXML
	private Button finishButton;

	public PadSettingsViewController(Pad pad, IMainViewController mainViewController) {
		load("view/option/pad", "PadSettingsView", Localization.getBundle());
		this.pad = pad;

		addTab(new GeneralPadTabViewController(pad));
		if (pad.getContent() instanceof Playlistable) {
			addTab(new PlaylistTabViewController(pad));
		}
		addTab(new DesignPadTabViewController(pad));
		addTab(new PlayerPadTabViewController(pad));
		addTab(new TriggerPadTabViewController(pad, mainViewController));

		if (pad.getContent() != null) {
			try {
				final String type = pad.getContent().getType();
				final PadContentRegistry registry = PlayPadPlugin.getRegistries().getPadContents();

				final PadContentFactory padContentFactory = registry.getFactory(type);
				final PadSettingsTabViewController contentTab = padContentFactory.getSettingsViewController(pad);

				if (contentTab != null) {
					addTab(contentTab);
				}
			} catch (NoSuchComponentException e) {
				Logger.error(e);
			}
		}

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(mainViewController.getStage());
		nvcStage.addCloseHook(this::onFinish);
		addCloseKeyShortcut(() -> finishButton.fire());

		// Show Current Settings
		showCurrentSettings();
		setTitle(pad);
	}

	private void setTitle(Pad pad) {
		String title;
		if (pad.getStatus() != PadStatus.EMPTY) {
			title = Localization.getString(Strings.UI_WINDOW_PAD_SETTINGS_TITLE, pad.getPositionReadable(), pad.getName());
		} else {
			title = Localization.getString(Strings.UI_WINDOW_PAD_SETTINGS_TITLE_EMPTY, pad.getPositionReadable());
		}
		getStageContainer().ifPresent(nvcStage -> nvcStage.getStage().setTitle(title));
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setMinWidth(650);
		stage.setMinHeight(600);

		PlayPadPlugin.styleable().applyStyle(stage);
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


	@FXML
	private void finishButtonHandler(ActionEvent event) {
		onFinish();
		getStageContainer().ifPresent(NVCStage::close);
	}

	/**
	 * Diese Methode wird aufgerufen, wenn das Fenster geschlossen wird (Per X oder Finish Button). Hier geschehen alle Aktionen zum
	 * manuellen Speichern.
	 */
	private boolean onFinish() {
		// Speichern der einzelen Tabs
		for (PadSettingsTabViewController controller : tabs) {
			controller.saveSettings(pad);
		}
		return true;
	}
}
