package de.tobias.playpad.plugin.media.action;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.Displayable;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.plugin.media.main.impl.MediaPluginImpl;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.dom4j.Element;

public class BlackAction extends Action implements Displayable {

	private ChangeListener<Boolean> blindFeedbackListener;

	BlackAction() {
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
	public void performAction(InputType type, Project project, IMainViewController mainViewController) {
		if (type == InputType.PRESSED) {
			MediaPluginImpl.blindProperty().set(!MediaPluginImpl.blindProperty().get());
		}
	}

	@Override
	public void clearFeedback() {
		MediaPluginImpl.blindProperty().removeListener(blindFeedbackListener);
	}

	@Override
	public void init(Project project, IMainViewController controller) {
		// Listener für Eingaben
		BooleanProperty blindProperty = MediaPluginImpl.blindProperty();
		blindProperty.removeListener(blindFeedbackListener);
		blindProperty.addListener(blindFeedbackListener);
	}

	@Override
	public void showFeedback(Project project, IMainViewController controller) {
		BooleanProperty blindProperty = MediaPluginImpl.blindProperty();

		// Handle Current Feedback
		blindFeedbackListener.changed(blindProperty, null, blindProperty.getValue());
	}

	@Override
	public FeedbackType geFeedbackType() {
		return FeedbackType.DOUBLE;
	}

	@Override
	public String getType() {
		return BlackActionFactory.TYPE;
	}

	@Override
	public void load(Element root) {
	}

	@Override
	public void save(Element root) {
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass().equals(getClass()) || super.equals(obj);
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(MediaPluginImpl.getInstance().getBundle().getString("black_action.name"));
	}

	@Override
	public Node getGraphics() {
		return new Label("", new FontIcon(FontAwesomeType.DESKTOP));
	}

	@Override
	public Action cloneAction() throws CloneNotSupportedException {
		return (BlackAction) super.clone();
	}
}
