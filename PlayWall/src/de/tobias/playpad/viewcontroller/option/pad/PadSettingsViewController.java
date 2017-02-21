package de.tobias.playpad.viewcontroller.option.pad;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.ContentFactory;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.content.path.MultiPathContent;
import de.tobias.playpad.pad.content.path.SinglePathContent;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.IPadSettingsViewController;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class PadSettingsViewController extends NVC implements IPadSettingsViewController {

	private Pad pad;

	@FXML private TabPane tabPane;
	private List<PadSettingsTabViewController> tabs = new ArrayList<>();

	private Control pathLookupButton;
	private PathLookupListener pathLookupListener;

	@FXML private Button finishButton;

	public PadSettingsViewController(Pad pad, Window owner) {
		load("de/tobias/playpad/assets/view/option/pad/", "padSettingsView", PlayPadMain.getUiResourceBundle());
		this.pad = pad;

		addTab(new GeneralPadTabViewController(pad));
		addTab(new DesignPadTabViewController(pad));
		addTab(new PlayerPadTabViewController(pad));
//		addTab(new TriggerPadTabViewController(pad)); TODO Add Trigger Tab when rewritten

		if (pad.getContent() != null) {
			try {
				// Get Pad Type specific tab
				String type = pad.getContent().getType();
				PadContentRegistry registry = PlayPadPlugin.getRegistryCollection().getPadContents();

				ContentFactory contentFactory = registry.getFactory(type);
				PadSettingsTabViewController contentTab = contentFactory.getSettingsViewController(pad);

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
		pathLookupListener = new PathLookupListener(this);

		if (pad.getContent() != null) {
			PadContent content = pad.getContent();
			// nur EIN Path
			if (content instanceof SinglePathContent) {
				Button button = new Button(PlayPadMain.getUiResourceBundle().getString("padSettings.button.path"));

				// Referenz auf das Model
				Path path = ((SinglePathContent) content).getPath();
				button.setUserData(path);

				button.setOnAction(pathLookupListener);

				// Setzt globales Feld
				pathLookupButton = button;
			} else if (content instanceof MultiPathContent) {
				MenuButton button = new MenuButton(PlayPadMain.getUiResourceBundle().getString("padSettings.button.path"));
				List<Path> paths = ((MultiPathContent) content).getPaths();

				for (Path path : paths) {
					MenuItem item = new MenuItem(path.getFileName().toString());
					button.getItems().add(item);

					// Referenz auf das Model
					item.setUserData(path);

					item.setOnAction(pathLookupListener);
				}

				// Setzt globales Feld
				pathLookupButton = button;
			}

			// Hüge Path Button zum Root Container hinzu.
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

		Profile.currentProfile().currentLayout().applyCss(stage);
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
