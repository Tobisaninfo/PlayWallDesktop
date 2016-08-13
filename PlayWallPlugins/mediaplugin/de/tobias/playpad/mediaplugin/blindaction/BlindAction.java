package de.tobias.playpad.mediaplugin.blindaction;

import org.dom4j.Element;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.mediaplugin.main.impl.MediaPluginImpl;
import de.tobias.playpad.project.v2.ProjectV2;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class BlindAction extends Action implements Displayable {

	private ChangeListener<Boolean> blindFeedbackListener;

	public BlindAction() {
		blindFeedbackListener = (a, b, c) ->
		{
			if (c) {
				handleFeedback(FeedbackMessage.EVENT);
			} else {
				handleFeedback(FeedbackMessage.STANDARD);
			}
		};
	}

	@Override
	public void performAction(InputType type, ProjectV2 project, IMainViewController mainViewController) {
		if (type == InputType.PRESSED) {
			MediaPluginImpl.blindProperty().set(!MediaPluginImpl.blindProperty().get());
		}
	}

	@Override
	public void clearFeedback() {
		MediaPluginImpl.blindProperty().removeListener(blindFeedbackListener);
	}

	@Override
	public void initFeedback(ProjectV2 project, IMainViewController controller) {
		// Listener für Eingaben
		BooleanProperty blindProperty = MediaPluginImpl.blindProperty();
		blindProperty.removeListener(blindFeedbackListener);
		blindProperty.addListener(blindFeedbackListener);

		// Handle Current Feedback
		blindFeedbackListener.changed(blindProperty, null, blindProperty.getValue());
	}

	@Override
	public FeedbackType geFeedbackType() {
		return FeedbackType.DOUBLE;
	}

	@Override
	public String getType() {
		return BlindActionConnect.TYPE;
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

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(MediaPluginImpl.getInstance().getBundle().getString("blindaction.name"));
	}

	@Override
	public Node getGraphics() {
		return new Label("", new FontIcon(FontAwesomeType.DESKTOP));
	}

	@Override
	public Action cloneAction() throws CloneNotSupportedException {
		BlindAction actionClone = (BlindAction) super.clone();
		return actionClone;
	}
}
