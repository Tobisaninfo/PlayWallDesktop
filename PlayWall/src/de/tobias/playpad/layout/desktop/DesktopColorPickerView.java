package de.tobias.playpad.layout.desktop;

import java.util.function.Consumer;

import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.design.IColorPickerView;
import de.tobias.playpad.settings.Profile;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DesktopColorPickerView implements Consumer<DisplayableColor> {

	private Stage stage;

	public DesktopColorPickerView(IColorPickerView baseView) {
		Node node = baseView.getColorInterface(this);
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
		
	}
}
