package de.tobias.playpad.view;

import java.util.List;

import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperRegistry;
import de.tobias.playpad.action.mapper.MapperViewController;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MapperQuickSettingsView {

	private VBox optionPane;
	private Pane root;

	public MapperQuickSettingsView(Pane pane) {
		root = pane;

		optionPane = new VBox();
		optionPane.prefWidthProperty().bind(root.widthProperty());
		optionPane.prefHeightProperty().bind(root.heightProperty());
		optionPane.setBackground(new Background(new BackgroundFill(new Color(0.2, 0.2, 0.2, 0.8), new CornerRadii(10), new Insets(0))));
		optionPane.setAlignment(Pos.CENTER);
		optionPane.setPadding(new Insets(5));
		optionPane.setSpacing(5);
	}

	private PadContentConnect selectedConnect;

	public void showDropOptions(List<Mapper> mappers) {
		if (!root.getChildren().contains(optionPane)) {
			selectedConnect = null;

			root.getChildren().add(optionPane);
			optionPane.getChildren().clear();

			for (Mapper mapper : mappers) {
				MapperViewController controller = MapperRegistry.getMapperConnect(mapper.getType()).getQuickSettingsViewController(mapper);
				optionPane.getChildren().add(controller.getParent());
			}
		}
	}

	public PadContentConnect getSelectedConnect() {
		return selectedConnect;
	}

	public void hide() {
		root.getChildren().remove(optionPane);
	}
}
