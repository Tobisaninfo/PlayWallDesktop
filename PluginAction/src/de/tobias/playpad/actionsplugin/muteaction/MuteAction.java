package de.tobias.playpad.actionsplugin.muteaction;

import org.dom4j.Element;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.actionsplugin.impl.ActionsPluginImpl;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.ui.icon.MaterialDesignIcon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class MuteAction extends Action implements Displayable {

	private ChangeListener<Boolean> muteListener;

	private String type;

	MuteAction(String type) {
		this.type = type;
		muteListener = (a, b, c) ->
		{
			if (c) {
				handleFeedback(FeedbackMessage.EVENT);
			} else {
				handleFeedback(FeedbackMessage.STANDARD);
			}
		};
	}

	@Override
	public void performAction(InputType type, Project project, IMainViewController mainViewController) {
		if (type == InputType.PRESSED) {
			ActionsPluginImpl.muteProperty().set(!ActionsPluginImpl.muteProperty().get());
		}
	}

	@Override
	public void clearFeedback() {
		ActionsPluginImpl.muteProperty().removeListener(muteListener);
	}

	@Override
	public void init(Project project, IMainViewController controller) {
		// Listener für Eingaben
		BooleanProperty muteProperty = ActionsPluginImpl.muteProperty();
		muteProperty.removeListener(muteListener);
		muteProperty.addListener(muteListener);
	}

	@Override
	public void showFeedback(Project project, IMainViewController controller) {
		BooleanProperty muteProperty = ActionsPluginImpl.muteProperty();

		// Handle Current Feedback
		muteListener.changed(muteProperty, null, muteProperty.getValue());
	}

	@Override
	public FeedbackType geFeedbackType() {
		return FeedbackType.DOUBLE;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void load(Element root) {}

	@Override
	public void save(Element root) {}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass().equals(getClass())) {
			return true;
		}
		return super.equals(obj);
	}

	// TODO Remove
	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(ActionsPluginImpl.getBundle().getString("muteaction.name"));
	}

	@Override
	public Node getGraphics() {
		return new Label("", new FontIcon(MaterialDesignIcon.FONT_FILE, MaterialDesignIcon.VOLUME_OFF));
	}

	@Override
	public Action cloneAction() throws CloneNotSupportedException {
		MuteAction actionClone = (MuteAction) super.clone();
		return actionClone;
	}
}
