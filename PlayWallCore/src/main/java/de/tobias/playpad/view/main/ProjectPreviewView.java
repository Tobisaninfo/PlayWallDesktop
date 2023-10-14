package de.tobias.playpad.view.main;

import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.api.IPad;
import de.tobias.playpad.project.api.IPage;
import de.tobias.playpad.project.api.IProject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

import java.util.List;

public class ProjectPreviewView extends Pagination {

	private final IProject project;
	private final ObservableList<IPad> selected;

	public ProjectPreviewView(IProject project, List<? extends IPad> preSelect, int initialPage) {
		super(project.getPages().size());
		this.project = project;
		this.selected = FXCollections.observableArrayList(preSelect);

		setCurrentPageIndex(initialPage);
		setPageFactory(this::getPageNode);
	}

	private Node getPageNode(int pageIndex) {
		GridPane gridPane = new GridPane();
		gridPane.setHgap(7);
		gridPane.setVgap(7);
		gridPane.setAlignment(Pos.CENTER);

		gridPane.setPadding(new Insets(0, 0, 7, 0));

		final IPage page = project.getPage(pageIndex);
		for (int x = 0; x < project.getSettings().getColumns(); x++) {
			for (int y = 0; y < project.getSettings().getRows(); y++) {
				final IPad pad = page.getPad(x, y);
				ToggleButton toggleButton = getToggleButton(selected, pad);

				gridPane.add(toggleButton, x, y);
			}
		}
		return gridPane;
	}

	private ToggleButton getToggleButton(List<IPad> preSelect, IPad pad) {
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
		return toggleButton;
	}

	public List<? extends IPad> getSelected() {
		return selected;
	}

	public ObservableList<? extends IPad> selectedProperty() {
		return selected;
	}
}
