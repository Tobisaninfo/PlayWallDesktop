package de.tobias.playpad.midi;

import java.util.Optional;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import de.tobias.playpad.action.mididevice.Device;
import de.tobias.playpad.midi.device.DeviceRegistry;

public class Midi {

	private Optional<MidiDevice> inputDevice = Optional.empty();
	private Optional<MidiDevice> outputDevice = Optional.empty();
	private Optional<Device> midiDeviceImpl = Optional.empty();

	private MidiListener listener;

	private static Midi instance;

	public static Midi getInstance() {
		if (instance == null) {
			instance = new Midi();
		}
		return instance;
	}

	private Midi() {}

	public MidiListener getListener() {
		return listener;
	}

	public void setListener(MidiListener listener) {
		this.listener = listener;
	}

	public static Info[] getMidiDevices() {
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		return infos;
	}

	public Optional<MidiDevice> getInputDevice() {
		return inputDevice;
	}

	public Optional<MidiDevice> getOutputDevice() {
		return outputDevice;
	}

	public Optional<Device> getMidiDevice() {
		return midiDeviceImpl;
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

	public void setMidiDevice(MidiDevice.Info input, MidiDevice.Info output) throws MidiUnavailableException, IllegalArgumentException {
		MidiDevice inputDevice = MidiSystem.getMidiDevice(input);
		MidiDevice outputDevice = MidiSystem.getMidiDevice(output);

		if (this.inputDevice.isPresent() && this.outputDevice.isPresent())
			if (this.inputDevice.get() == inputDevice && this.outputDevice.get() == outputDevice)
				return;

		this.inputDevice.ifPresent((device) ->
		{
			if (device.isOpen()) {
				device.close();
			}
		});
		this.outputDevice.ifPresent((device) ->
		{
			if (device.isOpen()) {
				device.close();
			}
		});

		if (inputDevice != null && outputDevice != null) {
			this.inputDevice = Optional.of(inputDevice);
			this.outputDevice = Optional.of(outputDevice);

			// Hier wird die DeviceImpl aufgerufen
			try {
				this.midiDeviceImpl = Optional.of(DeviceRegistry.getFactoryInstance().getDevice(input.getName()));
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				this.midiDeviceImpl = Optional.empty();
			}

			setupMidiDevice();
		} else {
			this.inputDevice = Optional.empty();
			this.outputDevice = Optional.empty();
			this.midiDeviceImpl = Optional.empty();
		}
	}

	private void setupMidiDevice() throws MidiUnavailableException {
		if (inputDevice.isPresent()) {
			Transmitter trans = inputDevice.get().getTransmitter();
			trans.setReceiver(new MidiInputReceiver());

			// Belegt das Midi Gerät und macht es nutzbar
			inputDevice.get().open();
			if (outputDevice.isPresent()) {
				outputDevice.get().open();
			}
		}
	}

	public void close() throws MidiUnavailableException {
		try {
			if (inputDevice.isPresent()) {
				inputDevice.get().getTransmitter().close();
				inputDevice.get().close();
			}
			if (outputDevice.isPresent()) {
				outputDevice.get().getReceiver().close();
				outputDevice.get().close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(int midiCommand, int midiKey, int midiVelocity) throws MidiUnavailableException, InvalidMidiDataException {
		if (outputDevice.isPresent()) {
			if (midiCommand != 0) {
				ShortMessage message = new ShortMessage(midiCommand, midiKey, midiVelocity);
				// System.out.println("Send: " + Arrays.toString(message.getMessage()));
				outputDevice.get().getReceiver().send(message, -1);
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
		public void close() {}
	}

	public boolean isOpen() {
		if (inputDevice.isPresent() && outputDevice.isPresent()) {
			return inputDevice.get().isOpen() && outputDevice.get().isOpen();
		} else {
			return false;
		}
	}

	public void sendClearCommand() throws InvalidMidiDataException, MidiUnavailableException {
		if (midiDeviceImpl.isPresent()) {
			midiDeviceImpl.get().clearFeedback();
		}
	}
}
