package de.tobias.playpad.action.mididevice;

import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import de.tobias.playpad.action.feedback.Feedback;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.event.Event;
import de.tobias.playpad.event.EventDispatcher;
import de.tobias.playpad.event.Listener;
import de.tobias.playpad.midi.device.DeviceRegistry;

import javax.sound.midi.MidiMessage;
import java.util.List;

/**
 * Abstraktes Midi Gerät. Jede Implementierung musss hier von erben. Es ist zugleich ein Listener und registriert die Listener beim
 * Erstellen automatisch auf Implementierung. Mögliche Interfaces: DeviceColorAssociatorConnector
 * 
 * @author tobias
 * 
 * @since 5.0.0
 */
public abstract class MidiDeviceImpl extends EventDispatcher implements Listener {

	public MidiDeviceImpl() {
		registerEventListener(this);
	}

	public abstract String getName();

	// Feedback
	public abstract boolean supportFeedback();

	public abstract DisplayableFeedbackColor[] getColors();

	public abstract DisplayableFeedbackColor getColor(int id);

	public abstract void initDevice();

	public abstract void handleFeedback(FeedbackMessage type, int key, Feedback feedback);

	public abstract void clearFeedback();

	// Custom Midi Actions
	public void onMidiMessage(MidiMessage message) {}

	@Override
	public void dispatchEvent(Event event) {
		List<ListenerHandler> listeners = this.listeners.get(event.getClass().getName());
		if (listeners != null) {
			for (ListenerHandler handler : listeners) {
				handler.execute(event);
				System.out.println("Dispatched MidiDeviceImpl Event: " + event);
			}
		}
		DeviceRegistry.getFactoryInstance().dispatchEvent(event);
	}
}
