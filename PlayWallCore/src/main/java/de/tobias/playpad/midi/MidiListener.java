package de.tobias.playpad.midi;

import javax.sound.midi.MidiMessage;

public interface MidiListener {

	void onMidiAction(MidiMessage message);

}
