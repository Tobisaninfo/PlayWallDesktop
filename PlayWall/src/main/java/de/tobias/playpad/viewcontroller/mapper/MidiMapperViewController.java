package de.tobias.playpad.viewcontroller.mapper;

import de.thecodelabs.midi.device.MidiDevice;
import de.thecodelabs.midi.mapping.Key;
import de.thecodelabs.midi.mapping.MidiKey;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.MidiCommand;
import de.thecodelabs.midi.midi.MidiCommandHandler;
import de.thecodelabs.midi.midi.MidiListener;
import de.thecodelabs.midi.midi.feedback.MidiFeedbackTranscript;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.mapper.MapperViewController;
import de.tobias.playpad.viewcontroller.option.feedback.SingleFeedbackViewController;
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

	@FXML
	private Label midiInputKeyLabel;
	@FXML
	private Button midiInputRecordButton;

	@FXML
	private VBox root;

	private MidiKey key;

	private NVC feedbackController;

	public MidiMapperViewController() {
		load("view/mapper", "Midi", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public Key getKey() {
		return key;
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
			root.getChildren().remove(feedbackController.getParent());
			root.getChildren().add(feedbackController.getParent());
			VBox.setVgrow(feedbackController.getParent(), Priority.ALWAYS);
		}
	}

	/**
	 * Current Alert for mapping.
	 */
	private Alert alert;

	// Hilfsvariable um zu speichern, ob der Input Dialog abgebrochen wurde
	private boolean canceled = false;

	@FXML
	private void midiInputRecordButtonHandler(ActionEvent event) {
		canceled = false;

		MidiCommandHandler.getInstance().addMidiListener(this);

		alert = new Alert(AlertType.NONE);
		alert.setTitle(Localization.getString(Strings.Mapper_Midi_Name));
		alert.setContentText(Localization.getString(Strings.Info_Mapper_PressKey));
		alert.getButtonTypes().add(ButtonType.CANCEL);
		alert.initOwner(getContainingWindow());
		alert.showAndWait().ifPresent(result ->
		{
			if (result == ButtonType.CANCEL) {
				alert = null;
				canceled = true;
				MidiCommandHandler.getInstance().removeMidiListener(this);
			}
		});
	}

	@Override
	public void onMidiMessage(MidiCommand midiCommand) {
		key.setValue(midiCommand.getPayload()[0]);
		midiCommand.consume();

		Platform.runLater(() ->
		{
			if (alert != null) {
				Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
				stage.close();
				alert = null;
			}
			midiInputKeyLabel.setText(String.valueOf(key.getValue()));
		});

		MidiCommandHandler.getInstance().removeMidiListener(this);
	}

	@Override
	public boolean showInputMapperUI() {
		midiInputRecordButtonHandler(null);
		return !canceled;
	}

	@Override
	public void setKey(Key midiKey) {
		this.key = (MidiKey) midiKey;

		midiInputKeyLabel.setText(String.valueOf(key.getValue()));

		final MidiDevice device = Midi.getInstance().getDevice();
		final MidiFeedbackTranscript transcript = Midi.getInstance().getFeedbackTranscript();

		if (device != null) {
			if (device.isModeSupported(Midi.Mode.OUTPUT)) { // TODO Change
				// remove old Elements
				if (feedbackController != null) {
					root.getChildren().remove(feedbackController.getParent());
				}
				feedbackController = new SingleFeedbackViewController(key.getDefaultFeedback(), transcript.getFeedbackValues());
			}
			showFeedback();
		}
	}
}
