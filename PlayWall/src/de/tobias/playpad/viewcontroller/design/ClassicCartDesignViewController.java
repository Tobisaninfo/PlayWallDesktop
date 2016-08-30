package de.tobias.playpad.viewcontroller.design;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.classic.ClassicCartDesign;
import de.tobias.playpad.viewcontroller.CartDesignViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

public class ClassicCartDesignViewController extends CartDesignViewController {

	private ClassicCartDesign layout;

	@FXML private ColorPicker playgroundColorPicker;
	@FXML private ColorPicker backgroundColorPicker;
	@FXML private ColorPicker warnColorPicker;
	@FXML private ColorPicker fadeColorPicker;

	@FXML private ColorPicker infoLabelColorPicker;
	@FXML private ColorPicker titleLabelColorPicker;

	@FXML private Button resetButton;

	public ClassicCartDesignViewController(CartDesign layout) {
		super("classicLayoutCart", "de/tobias/playpad/assets/view/option/layout/", PlayPadMain.getUiResourceBundle(), layout);
		setLayout((ClassicCartDesign) layout);
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
		titleLabelColorPicker.valueProperty().addListener((a, b, c) -> layout.setTitleLabelColor(c));
	}

	public void setLayout(ClassicCartDesign defaultLayout) {
		this.layout = defaultLayout;

		backgroundColorPicker.setValue(layout.getBackgroundColor());
		playgroundColorPicker.setValue(layout.getPlaybackColor());
		warnColorPicker.setValue(layout.getWarnColor());
		fadeColorPicker.setValue(layout.getFadeColor());

		infoLabelColorPicker.setValue(layout.getInfoLabelColor());
		titleLabelColorPicker.setValue(layout.getTitleLabelColor());
	}

	@FXML
	private void resetButtonHandler(ActionEvent event) {
		layout.reset();
		setLayout(layout);
	}

	@Override
	public void updateData() {
		backgroundColorPicker.setValue(layout.getBackgroundColor());
	}
}
