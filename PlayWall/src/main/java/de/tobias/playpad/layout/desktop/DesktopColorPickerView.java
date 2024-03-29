package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.design.ColorModeHandler;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import java.util.function.Consumer;

public class DesktopColorPickerView implements Consumer<DisplayableColor>, EventHandler<MouseEvent> {

	private ColorModeHandler colorModeHandler;
	private DisplayableColor selectedColor;

	private PopOver colorChooser;

	DesktopColorPickerView(ColorModeHandler colorModeHandler) {
		this.colorModeHandler = colorModeHandler;

		Node node = colorModeHandler.getColorInterface(this);
		VBox root = new VBox(node);

		// Init Stage
		colorChooser = new PopOver();
		colorChooser.setContentNode(root);
		colorChooser.setDetachable(false);
		colorChooser.setCornerRadius(5);
		colorChooser.setArrowLocation(ArrowLocation.TOP_CENTER);
	}

	public void show(Node anchorNode) {
		colorChooser.show(anchorNode);
	}

	public void hide() {
		if (colorChooser != null) {
			colorChooser.hide();
		}
	}

	// Handle Selected Color from View.
	@Override
	public void accept(DisplayableColor t) {
		selectedColor = t;
		colorChooser.hide();
	}

	// Listener, wenn auf ein Pad Geclicked wurde, zum färben
	@Override
	public void handle(MouseEvent event) {
		// TODO Rewrite this
		if (event.getSource() instanceof StackPane) {
			StackPane view = (StackPane) event.getSource();
			if (view.getUserData() instanceof Pad) {
				Pad pad = (Pad) view.getUserData();
				PadSettings padSettings = pad.getPadSettings();

				if (event.getButton() == MouseButton.PRIMARY) {
					ModernCartDesign design = padSettings.getDesign();
					design.setEnableCustomBackgroundColor(true);
					colorModeHandler.setColor(design, selectedColor);
				} else if (event.getButton() == MouseButton.SECONDARY) {
					padSettings.getDesign().setEnableCustomBackgroundColor(false);
				}
				PlayPadMain.getProgramInstance().getMainViewController().loadUserCss();
			}
		}
	}
}
