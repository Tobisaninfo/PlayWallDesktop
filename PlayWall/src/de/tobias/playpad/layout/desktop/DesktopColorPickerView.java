package de.tobias.playpad.layout.desktop;

import java.util.function.Consumer;

import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.ColorModeHandler;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.settings.Profile;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DesktopColorPickerView implements Consumer<DisplayableColor>, EventHandler<MouseEvent> {

	private Stage stage;

	private ColorModeHandler colorModeHandler;
	private DisplayableColor selectedColor;

	public DesktopColorPickerView(ColorModeHandler colorModeHandler) {
		this.colorModeHandler = colorModeHandler;

		Node node = colorModeHandler.getColorInterface(this);
		VBox root = new VBox(node);

		stage = new Stage();
		stage.setScene(new Scene(root));

		// Init Stage
		Profile.currentProfile().currentLayout().applyCss(stage);
		stage.setResizable(false);
	}

	public void show() {
		stage.show();
	}

	public void hide() {
		stage.close();
	}

	// Handle Selected Color from View.
	@Override
	public void accept(DisplayableColor t) {
		selectedColor = t;
	}

	@Override
	public void handle(MouseEvent event) {
		// TODO Rewrite this
		if (event.getSource() instanceof StackPane) {
			StackPane view = (StackPane) event.getSource();
			if (view.getUserData() instanceof Pad) {
				Pad pad = (Pad) view.getUserData();
				PadSettings padSettings = pad.getPadSettings();
				padSettings.setCustomLayout(true);
				CartDesign design = padSettings.getDesign();
				colorModeHandler.setColor(design, selectedColor);
				PlayPadMain.getProgramInstance().getMainViewController().loadUserCss();
			}
		}
	}
}
