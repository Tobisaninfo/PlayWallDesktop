package de.tobias.playpad.viewcontroller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.mididevice.Device;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.midi.device.DisplayableDevice;
import de.tobias.playpad.model.layout.CartLayout;
import de.tobias.playpad.model.layout.LayoutRegistry;
import de.tobias.playpad.pad.Fade;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.Pad.TimeMode;
import de.tobias.playpad.pad.Warning;
import de.tobias.playpad.pad.view.PadViewController;
import de.tobias.playpad.plugin.PlayPadPlugin;
import de.tobias.playpad.plugin.viewcontroller.CartLayoutViewController;
import de.tobias.playpad.plugin.viewcontroller.IPadSettingsViewController;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.cell.TimeModeCell;
import de.tobias.playpad.viewcontroller.settings.FadeViewController;
import de.tobias.playpad.viewcontroller.settings.WarningFeedbackViewController;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;

// TODO Renew this class
public class PadSettingsViewController extends ViewController implements IPadSettingsViewController {

	@FXML private TabPane tabPane;

	@FXML private TextField titleTextField;
	@FXML private Slider volumeSlider;
	@FXML private CheckBox repeatCheckBox;

	@FXML private CheckBox customTimeDisplayCheckBox;
	@FXML private ComboBox<TimeMode> timeDisplayComboBox;

	@FXML private CheckBox customFadeCheckBox;
	@FXML private AnchorPane fadeContainer;
	private FadeViewController fadeViewController;

	@FXML private AnchorPane layoutAnchorPane;
	@FXML private CheckBox enableLayoutCheckBox;
	private CartLayoutViewController layoutViewController;

	@FXML private AnchorPane warningFeedbackContainer;
	@FXML private CheckBox warningEnableCheckBox;

	@FXML private Button deleteButton;
	@FXML private Button folderButton;
	@FXML private Button finishButton;

	private Pad pad;
	private PadViewController controller;

	public PadSettingsViewController(Pad pad, PadViewController controller, Window owner) {
		super("padSettingsView", "de/tobias/playpad/assets/settings/", null, PlayPadMain.getUiResourceBundle());
		this.pad = pad;
		this.controller = controller;

		// Listener
		PlayPadPlugin.getImplementation().getPadSettingsViewListener().forEach(l -> l.onInit(this));

		getStage().initOwner(owner);

		titleTextField.setText(pad.getTitle());
		volumeSlider.setValue(pad.getVolume() * 100);
		if (pad.isCustomFade())
			fadeViewController.setFade(pad.getFade().get());

		repeatCheckBox.setSelected(pad.isLoop());

		customTimeDisplayCheckBox.setSelected(pad.isCustomTimeMode());
		if (!pad.isCustomTimeMode()) {
			timeDisplayComboBox.setDisable(true);
		}
		timeDisplayComboBox.setValue(pad.getTimeMode().orElse(TimeMode.REST));

		customFadeCheckBox.setSelected(pad.isCustomFade());
		if (!pad.isCustomFade()) {
			fadeContainer.setDisable(true);
		}

		enableLayoutCheckBox.setSelected(pad.isCustomLayout());
		if (pad.isCustomLayout()) {
			try {
				String layoutType = Profile.currentProfile().getProfileSettings().getLayoutType();
				Optional<CartLayout> layoutOpt = pad.getLayout(layoutType);
				if (layoutOpt.isPresent()) {
					setLayoutController(LayoutRegistry.cartViewControllerInstance(layoutType, layoutOpt.get()));
				}
			} catch (Exception e) {
				e.printStackTrace();
				// TODO
			}
		}

		warningEnableCheckBox.setSelected(pad.isCustomWarning());
		warningEnableCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			if (c && !pad.isCustomWarning()) {
				pad.setWarningFeedback(new Warning());
				addWarningController();
			} else if (b && pad.isCustomWarning()) {
				pad.setWarningFeedback(null);
				warningFeedbackContainer.getChildren().clear();
			}
		});

		if (pad.isCustomWarning()) {
			addWarningController();
		}

		if (!pad.isPadLoaded())
			folderButton.setDisable(true);

		getStage().getScene().setOnKeyReleased(ke ->
		{
			if (ke.isShortcutDown() && ke.getCode() == KeyCode.W) {
				Platform.runLater(() -> finishButtonHandler(null));
			}
		});
	}

	private void addWarningController() {
		Midi midi = Midi.getInstance();

		ContentViewController controller = null;
		if (midi.getMidiDevice().isPresent()) {
			Device device = Midi.getInstance().getMidiDevice().get();
			if (device instanceof DisplayableDevice) {
				controller = ((DisplayableDevice) device).getWarnViewController(pad);
			}
		}

		if (controller == null) {
			controller = new WarningFeedbackViewController(pad);
		}

		if (controller != null) {
			warningFeedbackContainer.getChildren().add(controller.getParent());
			setAnchor(controller.getParent(), 0, 0, 0, 0);
		}
	}

	@Override
	public void init() {
		// Listener
		titleTextField.textProperty().addListener((a, b, c) -> pad.setTitle(c));

		// Embed ViewController
		fadeViewController = new FadeViewController();
		fadeContainer.getChildren().add(fadeViewController.getParent());
		setAnchor(fadeViewController.getParent(), 0.0, 0.0, 0.0, 0.0);

		volumeSlider.valueProperty().addListener((a, b, c) -> pad.setVolume(c.doubleValue() / 100.0));
		repeatCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			pad.setLoop(c);
			if (controller != null)
				controller.getView().setLoopLabelActive(c);
		});

		customTimeDisplayCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			timeDisplayComboBox.setDisable(!c);
			if (c && !pad.isCustomTimeMode())
				pad.setTimeMode(TimeMode.REST);
			else if (b && pad.isCustomTimeMode())
				pad.setTimeMode(null);

		});
		timeDisplayComboBox.getItems().addAll(TimeMode.values());
		timeDisplayComboBox.valueProperty().addListener((a, b, c) ->
		{
			pad.setTimeMode(c);
		});
		timeDisplayComboBox.setButtonCell(new TimeModeCell());
		timeDisplayComboBox.setCellFactory(list -> new TimeModeCell());

		customFadeCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			fadeContainer.setDisable(!c);
			if (c && !pad.isCustomFade())
				pad.setFade(new Fade());
			else if (!c && pad.isCustomFade())
				pad.setFade(null);

			if (c)
				fadeViewController.setFade(pad.getFade().get());
		});

		enableLayoutCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			if (c && !pad.isCustomLayout()) {
				try {
					pad.setCustomLayout(true);
					try {
						String layoutType = Profile.currentProfile().getProfileSettings().getLayoutType();
						Optional<CartLayout> layoutOpt = pad.getLayout(layoutType);
						if (layoutOpt.isPresent()) {
							setLayoutController(LayoutRegistry.cartViewControllerInstance(layoutType, layoutOpt.get()));
						}
					} catch (Exception e) {
						e.printStackTrace();
						// TODO
					}
				} catch (Exception e) {
					showErrorMessage(Localization.getString(Strings.Error_Standard_Gen, e.getLocalizedMessage()));
					e.printStackTrace();
				}
			} else if (!c && pad.isCustomLayout()) {
				pad.setCustomLayout(false);
				setLayoutController(null);
			}
		});
	}

	private void setLayoutController(CartLayoutViewController cartLayoutViewController) {
		if (layoutViewController != null)
			layoutAnchorPane.getChildren().remove(layoutViewController.getParent());

		if (cartLayoutViewController != null) {
			layoutViewController = cartLayoutViewController;
			layoutAnchorPane.getChildren().add(layoutViewController.getParent());
			setAnchor(layoutViewController.getParent(), 0, 0, 0, 0);
		}
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(650);
		stage.setMinHeight(530);
		stage.setTitle(Localization.getString(Strings.UI_Window_PadSettings_Title));

		Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	@FXML
	private void deleteButtonHandler(ActionEvent event) {
		pad.clearPad();
		getStage().close();
	}

	@Override
	public boolean closeRequest() {
		if (layoutViewController != null) {
			layoutViewController.save();
		}
		// Listener
		PlayPadPlugin.getImplementation().getPadSettingsViewListener().forEach(l -> l.onClose(this));
		return true;
	}

	@FXML
	private void folderButtonHandler(ActionEvent event) {
		try {
			Desktop.getDesktop().open(pad.getPath().toFile().getParentFile());
		} catch (IOException | URISyntaxException e) {
			showErrorMessage(Localization.getString(Strings.Error_Standard_Gen, e.getMessage()), PlayPadMain.stageIcon);
			e.printStackTrace();
		}
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		if (layoutViewController != null) {
			layoutViewController.save();
		}
		// Listener
		PlayPadPlugin.getImplementation().getPadSettingsViewListener().forEach(l -> l.onClose(this));
		getStage().close();
	}

	public Pad getPad() {
		return pad;
	}

	public void addTab(String name, AnchorPane content) {
		content.setMinSize(0, 0);
		content.setMaxSize(AnchorPane.USE_COMPUTED_SIZE, AnchorPane.USE_COMPUTED_SIZE);
		tabPane.getTabs().add(new Tab(name, content));
	}
}
