package de.tobias.playpad.viewcontroller.mapper;

import javax.sound.midi.MidiMessage;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperViewController;
import de.tobias.playpad.action.mapper.MidiMapper;
import de.tobias.playpad.action.mapper.feedback.DoubleMidiFeedback;
import de.tobias.playpad.action.mapper.feedback.SingleMidiFeedback;
import de.tobias.playpad.action.mididevice.Device;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.midi.MidiListener;
import de.tobias.playpad.viewcontroller.option.feedback.DoubleFeedbackViewController;
import de.tobias.playpad.viewcontroller.option.feedback.SingleFeedbackViewController;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MidiMapperViewController extends MapperViewController implements MidiListener {

	@FXML private Label midiInputKeyLabel;
	@FXML private Button midiInputRecordButton;

	@FXML private VBox root;

	private MidiMapper mapper;

	private ContentViewController feedbackController;

	public MidiMapperViewController() {
		super("midi", "de/tobias/playpad/assets/view/mapper/", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public Mapper getMapper() {
		return mapper;
	}

	@Override
	public void hideFeedback() {
		if (feedbackController != null) {
			root.getChildren().remove(feedbackController.getParent());
		}
	}

	@Override
	public void showFeedback() {
		if (feedbackController != null) {
			if (root.getChildren().contains(feedbackController.getParent()))
				root.getChildren().remove(feedbackController.getParent());
			root.getChildren().add(feedbackController.getParent());
			VBox.setVgrow(feedbackController.getParent(), Priority.ALWAYS);
		}
	}

	/**
	 * Midi Listener von SettingsViewController, damit dieser wiederhergestellt werden kann am Ende.
	 */
	private MidiListener currentListener;

	/**
	 * Current Alert for mapping.
	 */
	private Alert alert;
	// Hilfsvariable um zu speichern, ob der Input Dialog abgebrochen wurde
	private boolean canceled = false;

	@FXML
	private void midiInputRecordButtonHandler(ActionEvent event) {
		canceled = false;

		currentListener = Midi.getInstance().getListener();
		Midi.getInstance().setListener(this);

		alert = new Alert(AlertType.NONE);
		alert.setTitle(Localization.getString(Strings.Mapper_Midi_Name));
		alert.setContentText(Localization.getString(Strings.Info_Mapper_PressKey));
		alert.getButtonTypes().add(ButtonType.CANCEL);
		alert.initOwner(getWindow());
		alert.showAndWait().ifPresent(result ->
		{
			if (result == ButtonType.CANCEL) {
				Midi.getInstance().setListener(currentListener);
				currentListener = null;
				alert = null;
				canceled = true;
			}
		});
	}

	/**
	 * Record new Midi Key
	 */
	@Override
	public void onMidiAction(MidiMessage message) {
		Platform.runLater(() ->
		{
			if (alert != null) {
				Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
				stage.close();
				alert = null;
			}
			mapper.setCommand(message.getMessage()[0]);
			mapper.setKey(message.getMessage()[1]);

			midiInputKeyLabel.setText(String.valueOf(mapper.getKey()));
		});
		Midi.getInstance().setListener(currentListener);
		currentListener = null;
	}

	@Override
	public boolean showInputMapperUI() {
		midiInputRecordButtonHandler(null);
		return !canceled;
	}

	public void setMapper(MidiMapper midiMapper) {
		this.mapper = midiMapper;

		midiInputKeyLabel.setText(String.valueOf(mapper.getKey()));

		Device device = Midi.getInstance().getMidiDevice();
		if (device != null) {
			if (device.supportFeedback()) {
				// remove old Elements
				if (feedbackController != null) {
					root.getChildren().remove(feedbackController.getParent());
				}
				// add new Elements
				if (mapper.getFeedbackType() == FeedbackType.SINGLE) {
					feedbackController = new SingleFeedbackViewController((SingleMidiFeedback) mapper.getFeedback(), device.getColors());
				} else if (mapper.getFeedbackType() == FeedbackType.DOUBLE) {
					feedbackController = new DoubleFeedbackViewController((DoubleMidiFeedback) mapper.getFeedback(), device.getColors());
				}
				showFeedback();
			}
		}
	}
}
