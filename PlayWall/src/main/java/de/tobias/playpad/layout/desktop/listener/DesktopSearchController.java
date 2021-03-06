package de.tobias.playpad.layout.desktop.listener;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.util.List;

public class DesktopSearchController implements EventHandler<ActionEvent> {

	private static final int HIGHLIGHT_DURATION = 3;

	private Project currentProject;

	private TextField textField;
	private IMainViewController mainView;

	public DesktopSearchController(Project project, TextField textField, IMainViewController mainView) {
		this.currentProject = project;

		this.textField = textField;
		this.mainView = mainView;
	}

	// Current Search
	private String lastSearchTerm;
	private int currentIndex = 0;
	private List<Pad> searchResult;

	@Override
	public void handle(ActionEvent event) {
		String currentSearchTerm = textField.getText();
		if (currentSearchTerm.isEmpty()) {
			return;
		}

		// New Search
		if (!currentSearchTerm.equals(lastSearchTerm)) {
			this.lastSearchTerm = currentSearchTerm;
			searchResult = currentProject.findPads(currentSearchTerm);
			currentIndex = 0;
		}

		if (searchResult.isEmpty()) {
			Alerts.getInstance().createAlert(
					Alert.AlertType.INFORMATION, ApplicationUtils.getApplication().getInfo().getName(),
					Localization.getString(Strings.SEARCH_ALERT_NO_MATCHES_HEADER),
					Localization.getString(Strings.SEARCH_ALERT_NO_MATCHES_CONTENT, currentSearchTerm),
					mainView.getStage()
			).showAndWait();
			return;
		}

		if (currentIndex < searchResult.size()) {
			Pad result = searchResult.get(currentIndex++);
			mainView.showPage(result.getPage());
			if (result.getController() != null) {
				result.getController().getView().highlightView(HIGHLIGHT_DURATION);
			}
		} else {
			Alerts.getInstance().createAlert(
					Alert.AlertType.INFORMATION, ApplicationUtils.getApplication().getInfo().getName(),
					Localization.getString(Strings.SEARCH_ALERT_NO_MATCHES_HEADER),
					Localization.getString(Strings.SEARCH_ALERT_NO_MATCHES_CONTENT, currentSearchTerm),
					mainView.getStage()
			).showAndWait();
			currentIndex = 0;
		}
	}
}
