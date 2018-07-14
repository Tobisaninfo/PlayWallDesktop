package de.tobias.playpad.midi;

import de.tobias.playpad.action.mididevice.MidiDeviceImpl;
import de.tobias.playpad.midi.device.DeviceRegistry;

import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;

public class Midi implements AutoCloseable {

	private MidiDevice inputDevice;
	private MidiDevice outputDevice;
	private MidiDeviceImpl midiMidiDeviceImplImpl;

	private MidiListener listener;

	private static Midi instance;

	public static Midi getInstance() {
		if (instance == null) {
			instance = new Midi();
		}
		return instance;
	}

	private Midi() {
	}

	public MidiListener getListener() {
		return listener;
	}

	public void setListener(MidiListener listener) {
		this.listener = listener;
	}

	public static Info[] getMidiDevices() {
		return MidiSystem.getMidiDeviceInfo();
	}

	public MidiDevice getInputDevice() {
		return inputDevice;
	}

	public MidiDevice getOutputDevice() {
		return outputDevice;
	}

	public MidiDeviceImpl getMidiDevice() {
		return midiMidiDeviceImplImpl;
	}

	public void lookupMidiDevice(String name) throws IllegalArgumentException, MidiUnavailableException, NullPointerException {
		boolean first = true;

		Info input = null;
		Info output = null;
		for (Info item : Midi.getMidiDevices()) {
			if (item.getName().equals(name)) {
				if (first) {
					input = item;
					first = false;
				} else {
					output = item;
				}
			}
		}
		if (input == null || output == null) {
			throw new NullPointerException();
		}
		setMidiDevice(input, output);
	}

	private void setMidiDevice(Info input, Info output) throws MidiUnavailableException, IllegalArgumentException {
		MidiDevice newInputDevice = MidiSystem.getMidiDevice(input);
		MidiDevice newOutputDevice = MidiSystem.getMidiDevice(output);

		if (newInputDevice == null && newOutputDevice == null) {
			return;
		}

		if (this.inputDevice == newInputDevice && this.outputDevice == newOutputDevice) {
			return;
		}

		// Close Old Devices
		close();

		this.inputDevice = newInputDevice;
		this.outputDevice = newOutputDevice;

		// Hier wird die DeviceImpl aufgerufen
		try {
			this.midiMidiDeviceImplImpl = DeviceRegistry.getFactoryInstance().getDevice(input.getName());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		setupMidiDevice();
	}


	private void setupMidiDevice() throws MidiUnavailableException {
		if (inputDevice != null) {
			Transmitter trans = inputDevice.getTransmitter();
			trans.setReceiver(new MidiInputReceiver());

			// Belegt das Midi Gerät und macht es nutzbar
			inputDevice.open();
			if (outputDevice != null) {
				outputDevice.open();
			}
		}
	}

	public void close() throws MidiUnavailableException {
		try {
			if (inputDevice != null) {
				inputDevice.getTransmitter().close();
				inputDevice.close();
			}
			if (outputDevice != null) {
				outputDevice.getReceiver().close();
				outputDevice.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(int midiCommand, int midiKey, int midiVelocity) throws MidiUnavailableException, InvalidMidiDataException {
		if (outputDevice != null) {
			if (midiCommand != 0) {
				ShortMessage message = new ShortMessage(midiCommand, midiKey, midiVelocity);
				// System.out.println("Send: " + Arrays.toString(message.getMessage()));
				outputDevice.getReceiver().send(message, -1);
			}
		}
	}

	private class MidiInputReceiver implements Receiver {

		@Override
		public void send(MidiMessage msg, long timeStamp) {
			try {
				listener.onMidiAction(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void close() {
		}

	}

	public boolean isOpen() {
		return inputDevice != null && outputDevice != null && inputDevice.isOpen() && outputDevice.isOpen();
	}

	public void sendClearCommand() throws InvalidMidiDataException, MidiUnavailableException {
		if (midiMidiDeviceImplImpl != null) {
			midiMidiDeviceImplImpl.clearFeedback();
		}
	}
}
