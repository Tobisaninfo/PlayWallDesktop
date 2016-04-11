package de.tobias.playpad.action.actions;

import org.dom4j.Element;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.connect.NavigateActionConnect;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NavigateAction extends Action {

	public enum NavigationType {
		PREVIOUS,
		NEXT;

		public String toString() {
			return Localization.getString(Strings.NavigationType_BaseName + name());
		};
	}

	private NavigationType action;

	public NavigateAction() {
		action = NavigationType.NEXT;
	}

	public NavigateAction(NavigationType type) {
		this.action = type;
	}

	public NavigationType getAction() {
		return action;
	}

	public void setAction(NavigationType action) {
		this.action = action;
	}

	@Override
	public String getType() {
		return NavigateActionConnect.TYPE;
	}

	@Override
	public void initFeedback(Project project, IMainViewController controller) {
		handleFeedback(FeedbackMessage.STANDARD);
	}

	@Override
	public void clearFeedback() {
		handleFeedback(FeedbackMessage.OFF);
	}

	@Override
	public void performAction(InputType type, Project project, IMainViewController mainViewController) {
		if (type == InputType.PRESSED) {
			switch (this.action) {
			case PREVIOUS:
				Platform.runLater(() -> mainViewController.showPage(mainViewController.getPage() - 1));
				break;
			case NEXT:
				Platform.runLater(() -> mainViewController.showPage(mainViewController.getPage() + 1));
				break;
			default:
				break;
			}
		}
	}

	@Override
	public FeedbackType geFeedbackType() {
		return FeedbackType.SINGLE;
	}

	private static final String TYPE = "action";

	@Override
	public void load(Element root) {
		action = NavigationType.valueOf(root.attributeValue(TYPE));
	}

	@Override
	public void save(Element root) {
		root.addAttribute(TYPE, action.name());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NavigateAction) {
			return ((NavigateAction) obj).getAction() == action;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.Action_Navigate_toString, action.toString());
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(toString());
	}

	@Override
	public Action cloneAction() throws CloneNotSupportedException {
		NavigateAction actionClone = (NavigateAction) super.clone();

		actionClone.action = this.action;

		return actionClone;
	}
}
