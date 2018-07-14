package de.tobias.playpad.view.main;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

public class ProjectPreviewView extends Pagination {

	private Project project;

	private ObservableList<Pad> selected;

	public ProjectPreviewView(Project project, List<Pad> preSelect) {
		super(project.getPages().size());
		this.project = project;
		this.selected = FXCollections.observableArrayList(preSelect);

		setPageFactory(index -> {
			GridPane gridPane = new GridPane();
			gridPane.setHgap(7);
			gridPane.setVgap(7);

			Page page = project.getPage(index);
			for (int x = 0; x < project.getSettings().getColumns(); x++) {
				for (int y = 0; y < project.getSettings().getRows(); y++) {
					final Pad pad = page.getPad(x, y);
					ToggleButton toggleButton = new ToggleButton(String.valueOf(pad.getPositionReadable()));
					if (pad.getStatus() != PadStatus.EMPTY) {
						toggleButton.setTooltip(new Tooltip(pad.getName()));
					}
					if (preSelect.contains(pad)) {
						toggleButton.setSelected(true);
					}
					toggleButton.setMinWidth(35);
					toggleButton.setPrefWidth(35);
					toggleButton.setMaxWidth(35);

					toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
						if (newValue) {
							selected.add(pad);
						} else {
							selected.remove(pad);
						}
					});

					gridPane.add(toggleButton, x, y);
				}
			}
			return gridPane;
		});
	}

	public List<Pad> getSelected() {
		return selected;
	}

	public ObservableList<Pad> selectedProperty() {
		return selected;
	}
}
