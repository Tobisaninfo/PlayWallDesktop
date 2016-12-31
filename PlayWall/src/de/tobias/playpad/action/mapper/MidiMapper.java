package de.tobias.playpad.action.mapper;

import java.util.Optional;

import org.dom4j.Element;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.feedback.ColorAssociator;
import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import de.tobias.playpad.action.feedback.Feedback;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.action.mapper.feedback.DoubleMidiFeedback;
import de.tobias.playpad.action.mapper.feedback.SingleMidiFeedback;
import de.tobias.playpad.action.mididevice.Device;
import de.tobias.playpad.action.mididevice.DeviceColorAssociatorConnector;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.viewcontroller.mapper.MidiMapperViewController;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class MidiMapper extends Mapper implements ColorAssociator, MapperFeedbackable {

	private String type;

	private int command;
	private int key;

	private Feedback feedback;
	private FeedbackType feedbackType;

	public MidiMapper(String type) {
		this(type, 0, 0);
	}

	public MidiMapper(String type, int command, int key) {
		this.type = type;
		this.command = command;
		this.key = key;
		updateDisplayProperty();
	}

	public int getCommand() {
		return command;
	}

	public void setCommand(int command) {
		this.command = command;
		updateDisplayProperty();
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
		updateDisplayProperty();
	}

	@Override
	public void initFeedback() {
		if (feedback == null || this.feedbackType != super.feedbackType) {
			if (super.feedbackType == FeedbackType.SINGLE) {
				feedback = new SingleMidiFeedback();
			} else if (super.feedbackType == FeedbackType.DOUBLE) {
				feedback = new DoubleMidiFeedback();
			}
		}
		this.feedbackType = super.feedbackType;
	}

	public Feedback getFeedback() {
		return feedback;
	}
	
	@Deprecated
	public void setFeedback(Feedback feedback) {
		this.feedback = feedback;
	}

	@Override
	public void handleFeedback(FeedbackMessage type) {
		Midi.getInstance().getMidiDevice().ifPresent(device -> device.handleFeedback(type, key, feedback));
	}

	@Override
	public String getType() {
		return type;
	}

	// Feedback, abhängig vom Device
	@Override
	public boolean supportFeedback() {
		Optional<Device> midiDevice = Midi.getInstance().getMidiDevice();
		if (midiDevice.isPresent()) {
			return midiDevice.get().supportFeedback();
		}
		return false;
	}

	@Override
	public void setColor(FeedbackMessage feedbackMessage, DisplayableFeedbackColor color) {
		if (feedbackMessage == FeedbackMessage.STANDARD || feedbackMessage == FeedbackMessage.EVENT) {
			feedback.setFeedback(feedbackMessage, color.mapperFeedbackValue());
		} else {
			throw new IllegalArgumentException("Unexpected Message Type.");
		}
	}

	@Override
	public DisplayableFeedbackColor[] getColors() {
		Optional<Device> midiDevice = Midi.getInstance().getMidiDevice();
		if (midiDevice.isPresent()) {
			Device device = midiDevice.get();
			if (device instanceof DeviceColorAssociatorConnector) {
				return ((DeviceColorAssociatorConnector) device).getColors();
			}
		}
		return null;
	}

	@Override
	public DisplayableFeedbackColor getDefaultEventColor() {
		Optional<Device> midiDevice = Midi.getInstance().getMidiDevice();
		if (midiDevice.isPresent()) {
			Device device = midiDevice.get();
			if (device instanceof DeviceColorAssociatorConnector) {
				return ((DeviceColorAssociatorConnector) device).getDefaultEventColor();
			}
		}
		return null;
	}

	@Override
	public DisplayableFeedbackColor getDefaultStandardColor() {
		Optional<Device> midiDevice = Midi.getInstance().getMidiDevice();
		if (midiDevice.isPresent()) {
			Device device = midiDevice.get();
			if (device instanceof DeviceColorAssociatorConnector) {
				return ((DeviceColorAssociatorConnector) device).getDefaultStandardColor();
			}
		}
		return null;
	}
	
	@Override
	public DisplayableFeedbackColor map(Color color) {
		Optional<Device> midiDevice = Midi.getInstance().getMidiDevice();
		if (midiDevice.isPresent()) {
			Device device = midiDevice.get();
			if (device instanceof DeviceColorAssociatorConnector) {
				return ((DeviceColorAssociatorConnector) device).map(color);
			}
		}
		return null;
	}

	private static final String MIDI_COMMAND = "command";
	private static final String MIDI_KEY = "key";
	private static final String FEEDBACK = "Feedback";

	@Override
	public void load(Element element, Action action) {
		String commandValue = element.attributeValue(MIDI_COMMAND);
		String keyValue = element.attributeValue(MIDI_KEY);

		if (commandValue != null) {
			command = Integer.valueOf(commandValue);
		}
		if (keyValue != null) {
			key = Integer.valueOf(keyValue);
		}

		Element feedbackElement = element.element(FEEDBACK);
		setFeedbackType(action.geFeedbackType());
		if (feedback != null) {
			feedback.load(feedbackElement);
		} else {
			initFeedback();
		}
	}

	@Override
	public void save(Element element) {
		element.addAttribute(MIDI_COMMAND, String.valueOf(command));
		element.addAttribute(MIDI_KEY, String.valueOf(key));

		Element feedbackElement = element.addElement(FEEDBACK);
		if (feedback != null) {
			feedback.save(feedbackElement);
		}
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.Mapper_Midi_toString, key);
	}

	private StringProperty displayProperty = new SimpleStringProperty();

	@Override
	public StringProperty displayProperty() {
		updateDisplayProperty();
		return displayProperty;
	}

	private MidiMapperViewController settingsViewController;

	@Override
	public ContentViewController getSettingsViewController() {
		if (settingsViewController == null) {
			settingsViewController = new MidiMapperViewController();
		}
		settingsViewController.setMapper(this);
		return settingsViewController;
	}

	private void updateDisplayProperty() {
		displayProperty.set(toString());
	}

	@Override
	public Mapper cloneMapper() throws CloneNotSupportedException {
		MidiMapper mapper = (MidiMapper) super.clone();

		mapper.command = command;
		mapper.feedback = feedback.cloneFeedback();
		mapper.key = key;

		mapper.displayProperty = new SimpleStringProperty();

		return mapper;
	}
}
