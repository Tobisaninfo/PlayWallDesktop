package de.tobias.playpad.equalizerplugin.impl;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.NumberUtils;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.equalizerplugin.Equalizer;
import de.tobias.playpad.profile.Profile;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.EqualizerBand;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EqualizerViewController extends NVC {

	@FXML
	private HBox equalizerView;
	@FXML
	private Button resetButton;
	@FXML
	private Button finishButton;
	@FXML
	private CheckBox enableCheckBox;

	public EqualizerViewController(Window owner) {
		load("view", "equalizerView", EqualizerPluginImpl.getBundle());

		applyViewControllerToStage().initOwner(owner);

		Equalizer eq = Equalizer.getInstance();

		List<Integer> bands = new ArrayList<>(eq.getBands());
		Collections.sort(bands);

		for (int band : bands) {
			VBox bandBox = new VBox();

			Slider slider = new Slider(EqualizerBand.MIN_GAIN, EqualizerBand.MAX_GAIN, eq.getGain(band));
			slider.setOrientation(Orientation.VERTICAL);
			slider.setMajorTickUnit(6);
			slider.setShowTickLabels(true);
			slider.setShowTickMarks(true);
			slider.setMaxHeight(Double.MAX_VALUE);
			slider.getStyleClass().add("equalizer-slider");
			slider.setUserData(band);

			Label infoLabel = new Label(NumberUtils.convertBytesToAppropriateFormat(band, 0) + EqualizerPluginImpl.getBundle().getString("eq.slider"));
			infoLabel.getStyleClass().add("equalizer-label");

			bandBox.getChildren().addAll(slider, infoLabel);
			bandBox.prefWidthProperty().bind(equalizerView.widthProperty().divide(bands.size()));
			VBox.setVgrow(slider, Priority.ALWAYS);
			bandBox.setAlignment(Pos.CENTER);
			bandBox.getStyleClass().add("equalizer-bandbox");

			equalizerView.getChildren().add(bandBox);
			equalizerView.getStyleClass().add("equalizer-box");

			// Data Binding
			eq.gainProperty(band).bindBidirectional(slider.valueProperty());
		}

		// Setup
		enableCheckBox.setSelected(eq.isEnable());
		equalizerView.setDisable(!enableCheckBox.isSelected());
	}

	@Override
	public void init() {
		enableCheckBox.selectedProperty().addListener((a, b, c) -> equalizerView.setDisable(!c));

		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadPlugin.getImplementation().getIcon().ifPresent(stage.getIcons()::add);

		stage.setTitle(EqualizerPluginImpl.getBundle().getString("eq.title"));
		stage.setMinWidth(500);
		stage.setMinHeight(250);

		ModernGlobalDesign design = Profile.currentProfile().getProfileSettings().getDesign();
		PlayPadPlugin.getModernDesignHandler().getModernGlobalDesignHandler().applyStyleSheet(design, stage);
		stage.getScene().getStylesheets().add("style/style.css");
	}

	@FXML
	private void enableCheckBoxHandler(ActionEvent event) {
		Equalizer.getInstance().setEnable(enableCheckBox.isSelected());
	}

	@FXML
	private void resetButtonHandler(ActionEvent event) {
		for (int band : Equalizer.getInstance().getBands()) {
			Equalizer.getInstance().setGain(band, 0);
		}
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		try {
			Equalizer.save(ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "equalizer.xml"));
		} catch (IOException e) {
			showErrorMessage(Localization.getString("error.file.save", e.getLocalizedMessage()));
			e.printStackTrace();
		}
		getStageContainer().ifPresent(NVCStage::close);
	}
}
