package de.tobias.playpad.viewcontroller.option.pad;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.viewcontroller.IPadSettingsViewController;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

public class PadSettingsViewController extends NVC implements IPadSettingsViewController {

	private Pad pad;

	@FXML
	private TabPane tabPane;
	private List<PadSettingsTabViewController> tabs = new ArrayList<>();

	private Control pathLookupButton;

	@FXML
	private Button finishButton;

	public PadSettingsViewController(Pad pad, Window owner) {
		load("view/option/pad", "PadSettingsView", PlayPadMain.getUiResourceBundle());
		this.pad = pad;

		addTab(new GeneralPadTabViewController(pad));
		addTab(new DesignPadTabViewController(pad));
		addTab(new PlayerPadTabViewController(pad));
		addTab(new TriggerPadTabViewController(pad));

		if (pad.getContent() != null) {
			try {
				// Get Pad Type specific tab
				String type = pad.getContent().getType();
				PadContentRegistry registry = PlayPadPlugin.getRegistryCollection().getPadContents();

				PadContentFactory padContentFactory = registry.getFactory(type);
				PadSettingsTabViewController contentTab = padContentFactory.getSettingsViewController(pad);

				if (contentTab != null)
					addTab(contentTab);
			} catch (NoSuchComponentException e) {
				e.printStackTrace();
			}
		}

		setupPathLookupButton();

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		nvcStage.addCloseHook(this::onFinish);
		addCloseKeyShortcut(() -> finishButton.fire());

		// Show Current Settings
		showCurrentSettings();
		setTitle(pad);
	}

	private void setupPathLookupButton() {
		PathLookupListener pathLookupListener = new PathLookupListener();

		if (pad.getContent() != null) {
			final ObservableList<MediaPath> paths = pad.getPaths();
			if (paths.size() == 1) {
				Button button = new Button(PlayPadMain.getUiResourceBundle().getString("padSettings.button.path"));

				MediaPath path = paths.get(0);
				button.setUserData(path);
				button.setOnAction(pathLookupListener);

				pathLookupButton = button;
			} else if (paths.size() > 1) {
				MenuButton button = new MenuButton(PlayPadMain.getUiResourceBundle().getString("padSettings.button.path"));

				for (MediaPath path : paths) {
					MenuItem item = new MenuItem(path.getFileName());
					button.getItems().add(item);

					item.setUserData(path);
					item.setOnAction(pathLookupListener);
				}

				pathLookupButton = button;
			}

			Parent parent = getParent();
			if (parent instanceof AnchorPane && pathLookupButton != null) {
				AnchorPane anchorPane = (AnchorPane) parent;
				anchorPane.getChildren().add(pathLookupButton);

				AnchorPane.setLeftAnchor(pathLookupButton, 14.0);
				AnchorPane.setBottomAnchor(pathLookupButton, 14.0);
			}
		}
	}

	private void setTitle(Pad pad) {
		String title;
		if (pad.getStatus() != PadStatus.EMPTY) {
			title = Localization.getString(Strings.UI_Window_PadSettings_Title, pad.getPositionReadable(), pad.getName());
		} else {
			title = Localization.getString(Strings.UI_Window_PadSettings_Title_Empty, pad.getPositionReadable());
		}
		getStageContainer().ifPresent(nvcStage -> nvcStage.getStage().setTitle(title));
	}

	@Override
	public void init() {

	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(650);
		stage.setMinHeight(550);

		ModernGlobalDesign design = Profile.currentProfile().getProfileSettings().getDesign();
		PlayPadPlugin.getModernDesignHandler().getModernGlobalDesignHandler().applyStyleSheet(design, stage);
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
		getStageContainer().ifPresent(NVCStage::close);
		return true;
	}
}
