package de.tobias.playpad.viewcontroller.layout;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.layout.CartLayout;
import de.tobias.playpad.layout.classic.ClassicCartLayout;
import de.tobias.playpad.viewcontroller.CartLayoutViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

public class ClassicLayoutCartViewController extends CartLayoutViewController {

	private ClassicCartLayout layout;

	@FXML private ColorPicker playgroundColorPicker;
	@FXML private ColorPicker backgroundColorPicker;
	@FXML private ColorPicker warnColorPicker;
	@FXML private ColorPicker fadeColorPicker;

	@FXML private ColorPicker infoLabelColorPicker;
	@FXML private ColorPicker titleLabelColorPicker;

	@FXML private Button resetButton;

	public ClassicLayoutCartViewController(CartLayout layout) {
		super("classicLayoutCart", "de/tobias/playpad/assets/view/option/layout/", PlayPadMain.getUiResourceBundle(), layout);
		setLayout((ClassicCartLayout) layout);
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

	public void setLayout(ClassicCartLayout defaultLayout) {
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
