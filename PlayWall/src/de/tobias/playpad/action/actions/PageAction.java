package de.tobias.playpad.action.actions;

import org.dom4j.Element;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PageAction extends Action {

	private final String type;

	private int page;

	public PageAction(String type) {
		this(type, 0);
	}

	public PageAction(String type, int page) {
		this.type = type;
		this.page = page;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void init(Project project, IMainViewController controller) {

	}

	@Override
	public void showFeedback(Project project, IMainViewController controller) {
		int page = controller.getPage();
		if (page == this.page) {
			handleFeedback(FeedbackMessage.EVENT);
		} else {
			handleFeedback(FeedbackMessage.STANDARD);
		}
	}

	@Override
	public void clearFeedback() {
		handleFeedback(FeedbackMessage.OFF);
	}

	@Override
	public void performAction(InputType type, Project project, IMainViewController mainViewController) {
		if (type == InputType.PRESSED) {
			Platform.runLater(() -> mainViewController.showPage(page));
		}
	}

	@Override
	public FeedbackType geFeedbackType() {
		return FeedbackType.DOUBLE;
	}

	private static final String PAGE = "page";

	@Override
	public void load(Element root) {
		page = Integer.valueOf(root.attributeValue(PAGE));
	}

	@Override
	public void save(Element root) {
		root.addAttribute(PAGE, String.valueOf(page));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PageAction) {
			return ((PageAction) obj).getPage() == page;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.Action_Page_toString, String.valueOf(page + 1));
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(toString());
	}

	@Override
	public Action cloneAction() throws CloneNotSupportedException {
		PageAction action = (PageAction) super.clone();

		action.page = page;

		return action;
	}
}
