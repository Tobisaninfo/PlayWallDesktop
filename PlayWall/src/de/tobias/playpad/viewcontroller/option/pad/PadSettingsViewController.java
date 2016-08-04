package de.tobias.playpad.viewcontroller.option.pad;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadContentRegistry;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.conntent.path.MultiPathContent;
import de.tobias.playpad.pad.conntent.path.SinglePathContent;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.IPadSettingsViewController;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.utils.ui.ViewController;
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

public class PadSettingsViewController extends ViewController implements IPadSettingsViewController {

	private Pad pad;

	@FXML private TabPane tabPane;
	protected List<PadSettingsTabViewController> tabs = new ArrayList<>();

	private Control pathLookupButton;
	private PathLookupListener pathLookupListener;

	@FXML private Button finishButton;

	public PadSettingsViewController(Pad pad, Window owner) {
		super("padSettingsView", "de/tobias/playpad/assets/view/option/pad/", null, PlayPadMain.getUiResourceBundle());
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

				PadContentConnect padContentConnect = registry.getComponent(type);
				PadSettingsTabViewController contentTab = padContentConnect.getSettingsViewController(pad);

				if (contentTab != null)
					addTab(contentTab);
			} catch (NoSuchComponentException e) {
				e.printStackTrace();
			}
		}

		setupPathLookupButton();

		getStage().initOwner(owner);

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
				Button button = new Button("Show Path");

				// Referenz auf das Model
				Path path = ((SinglePathContent) content).getPath();
				button.setUserData(path);

				button.setOnAction(pathLookupListener);

				// Setzt globales Feld
				pathLookupButton = button;
			} else if (content instanceof MultiPathContent) {
				MenuButton button = new MenuButton(PlayPadMain.getUiResourceBundle().getString(""));
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
		if (pad.getStatus() != PadStatus.EMPTY) {
			getStage().setTitle(Localization.getString(Strings.UI_Window_PadSettings_Title, pad.getIndexReadable(), pad.getName()));
		} else {
			getStage().setTitle(Localization.getString(Strings.UI_Window_PadSettings_Title_Empty, pad.getIndexReadable()));
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
		onFinish();
		return true;
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		onFinish();
	}

	/**
	 * Diese Methode wird aufgerufen, wenn das Fenster geschlossen wird (Per X oder Finish Button). Hier geschehen alle Aktionen zum
	 * manuellen Speichern.
	 */
	private void onFinish() {
		// Speichern der einzelen Tabs
		for (PadSettingsTabViewController controller : tabs) {
			controller.saveSettings(pad);
		}
		getStage().close();
	}
}
