package de.tobias.playpad.viewcontroller.design;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.design.DesignFactory;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.design.classic.ClassicGlobalDesign;
import de.tobias.playpad.design.classic.Theme;
import de.tobias.playpad.viewcontroller.GlobalDesignViewController;
import de.tobias.playpad.viewcontroller.cell.ThemeCell;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.converter.IntegerStringConverter;

public class ClassicGlobalDesignViewController extends GlobalDesignViewController {

	private ClassicGlobalDesign layout;

	@FXML private ComboBox<DesignFactory> layoutTypeComboBox;

	@FXML private ComboBox<Theme> programLayoutComboBox;
	@FXML private ColorPicker accentColorChooser;
	@FXML private CheckBox customLayoutCheckBox;

	@FXML private VBox customizeView;

	@FXML private ColorPicker playgroundColorPicker;
	@FXML private ColorPicker backgroundColorPicker;
	@FXML private ColorPicker warnColorPicker;
	@FXML private ColorPicker fadeColorPicker;

	@FXML private ColorPicker infoLabelColorPicker;
	@FXML private ComboBox<Integer> infoLabelFontSizeComboBox;

	@FXML private ColorPicker titleLabelColorPicker;
	@FXML private ComboBox<Integer> titleLabelFontSizeComboBox;

	@FXML private Button resetButton;

	public ClassicGlobalDesignViewController(GlobalDesign layout) {
		super(layout);
		load("de/tobias/playpad/assets/view/option/layout/", "classicLayoutGlobal", PlayPadMain.getUiResourceBundle());
		setLayout((ClassicGlobalDesign) layout);

		ClassicGlobalDesign cl = (ClassicGlobalDesign) layout;
		programLayoutComboBox.valueProperty().bindBidirectional(cl.themeProperty());
		accentColorChooser.setValue(cl.getAccentColor());
		customLayoutCheckBox.setSelected(cl.isCustomLayout());

		customizeView.setDisable(!cl.isCustomLayout());
	}

	@Override
	public void init() {
		backgroundColorPicker.valueProperty().addListener((a, b, c) ->
		{
			if (c.getOpacity() > 0.5) {
				backgroundColorPicker.setValue(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.5));
				return;
			}
			layout.setBackgroundColor(c);
		});
		playgroundColorPicker.valueProperty().addListener((a, b, c) ->
		{
			if (c.getOpacity() > 0.75) {
				playgroundColorPicker.setValue(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.75));
				return;
			}
			layout.setPlaybackColor(c);
		});

		warnColorPicker.valueProperty().addListener((a, b, c) ->
		{
			if (c.getOpacity() > 0.75) {
				warnColorPicker.setValue(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.75));
				return;
			}
			layout.setWarnColor(c);
		});

		fadeColorPicker.valueProperty().addListener((a, b, c) ->
		{
			if (c.getOpacity() > 0.75) {
				fadeColorPicker.setValue(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.75));
				return;
			}
			layout.setFadeColor(c);
		});

		infoLabelColorPicker.valueProperty().addListener((a, b, c) -> layout.setInfoLabelColor(c));
		infoLabelFontSizeComboBox.getItems().addAll(9, 10, 12, 13, 14, 16, 18, 20, 24, 28);
		infoLabelFontSizeComboBox.valueProperty().addListener((a, b, c) ->
		{
			try {
				layout.setInfoLabelFontSize(c);
				infoLabelFontSizeComboBox.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
			} catch (NumberFormatException e) {
				infoLabelFontSizeComboBox.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
			}
		});
		infoLabelFontSizeComboBox.setConverter(new IntegerStringConverter());

		titleLabelColorPicker.valueProperty().addListener((a, b, c) -> layout.setTitleLabelColor(c));
		titleLabelFontSizeComboBox.getItems().addAll(9, 10, 12, 13, 14, 16, 18, 20, 24, 28);
		titleLabelFontSizeComboBox.valueProperty().addListener((a, b, c) ->
		{
			try {
				layout.setTitleLabelFontSize(c);
				titleLabelFontSizeComboBox.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
			} catch (NumberFormatException e) {
				titleLabelFontSizeComboBox.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
			}
		});
		titleLabelFontSizeComboBox.setConverter(new IntegerStringConverter());

		programLayoutComboBox.getItems().addAll(Theme.values());
		programLayoutComboBox.setButtonCell(new ThemeCell());
		programLayoutComboBox.setCellFactory(list -> new ThemeCell());
		programLayoutComboBox.valueProperty().addListener((a, b, c) ->
		{
			if (b != null) {
				layout.setBackgroundColor(c.getBackground());
				layout.setAccentColor(c.getGridColor());
				backgroundColorPicker.setValue(c.getBackground());
				accentColorChooser.setValue(c.getGridColor());
			}
		});
		accentColorChooser.valueProperty().addListener((a, b, c) ->
		{
			layout.setAccentColor(c);
		});
		customLayoutCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			layout.setCustomLayout(c);
			customizeView.setDisable(!c);
		});
	}

	public void setLayout(ClassicGlobalDesign defaultLayout) {
		this.layout = defaultLayout;

		backgroundColorPicker.setValue(layout.getBackgroundColor());
		playgroundColorPicker.setValue(layout.getPlaybackColor());
		warnColorPicker.setValue(layout.getWarnColor());
		fadeColorPicker.setValue(layout.getFadeColor());

		infoLabelColorPicker.setValue(layout.getInfoLabelColor());
		infoLabelFontSizeComboBox.setValue(layout.getInfoLabelFontSize());

		titleLabelColorPicker.setValue(layout.getTitleLabelColor());
		titleLabelFontSizeComboBox.setValue(layout.getTitleLabelFontSize());
	}

	@FXML
	private void resetButtonHandler(ActionEvent event) {
		layout.reset();
		setLayout(layout);
	}

	public void updateData() {
		backgroundColorPicker.setValue(layout.getBackgroundColor());
	}
}
