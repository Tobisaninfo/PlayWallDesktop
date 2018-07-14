package de.tobias.playpad.action.actions;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.nui.icon.FontAwesomeType;
import de.tobias.utils.nui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.dom4j.Element;

public class StopAction extends Action {

	private String type;

	public StopAction(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void performAction(InputType type, Project project, IMainViewController mainViewController) {
		for (Pad pad : project.getPads()) {
			if (pad.getStatus() == PadStatus.PLAY || pad.getStatus() == PadStatus.PAUSE)
				pad.setStatus(PadStatus.STOP, true);
		}
	}

	@Override
	public void init(Project project, IMainViewController controller) {

	}

	@Override
	public void showFeedback(Project project, IMainViewController controller) {
		handleFeedback(FeedbackMessage.STANDARD);
	}

	@Override
	public void clearFeedback() {
		handleFeedback(FeedbackMessage.OFF);
	}

	@Override
	public FeedbackType geFeedbackType() {
		return FeedbackType.SINGLE;
	}

	@Override
	public void load(Element root) {
	}

	@Override
	public void save(Element root) {
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(Localization.getString(Strings.Action_Stop_Name));
	}

	@Override
	public Node getGraphics() {
		return new Label("", new FontIcon(FontAwesomeType.STOP));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass().equals(getClass())) {
			return true;
		}
		return super.equals(obj);
	}

	@Override
	public Action cloneAction() throws CloneNotSupportedException {
		return (Action) super.clone();
	}

}
