package de.tobias.playpad.layout.desktop;

import de.tobias.utils.ui.Alertable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;

public class DesktopSearchController implements EventHandler<ActionEvent> {

	private TextField textField;
	private Alertable alertable;

	public DesktopSearchController(TextField textField, Alertable alertable) {
		this.textField = textField;
		this.alertable = alertable;
	}

	private int currentIndex = 0;

	@Override
	public void handle(ActionEvent event) {
		if (textField.getText().isEmpty()) {
			return;
		}

		// TODO Implement Search
//		ProjectV2 currentProject = PlayPadMain.getProgramInstance().getCurrentProject();
//		main: for (int i = currentIndex; i < currentProject.getPadCount(); i++) {
//			Pad pad = currentProject.getPad(i);
//			if (pad.getStatus() != PadStatus.EMPTY) {
//				if (pad.getName().startsWith(textField.getText())) {
//					while (pad.getController() == null) {
//						if (!PlayPadPlugin.getImplementation().getMainViewController()
//								.showPage(PlayPadPlugin.getImplementation().getMainViewController().getPage() + 1)) {
//							break main;
//						}
//					}
//					pad.getController().getView().highlightView(3);
//					currentIndex = i + 1;
//					return;
//				}
//			}
//		}
//		alertable.showInfoMessage("Keine weiteren Treffer gefunden.", PlayPadMain.stageIcon.orElse(null)); // TODO i18n
//		currentIndex = 0;
	}
}
