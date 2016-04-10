package de.tobias.playpad.midi.device;

import java.util.HashMap;
import java.util.List;

import de.tobias.playpad.action.mididevice.Device;
import de.tobias.playpad.event.Event;
import de.tobias.playpad.event.EventDispatcher;

/**
 * Verwaltet die Midi Geräte Implementierung. Hier werden Geräte registiert und bei Verwendung instanziert für das Programm.
 * 
 * @author tobias
 * @since 3.0.0
 *
 */
public class DeviceRegistry extends EventDispatcher {

	private static DeviceRegistry instance;

	static {
		instance = new DeviceRegistry();
	}

	/**
	 * Speicher für alle Geräte Implementierungen [Produktname, Implementierung]
	 */
	private HashMap<String, Class<? extends Device>> devices = new HashMap<>();

	/**
	 * Registriert eine neue Implementierung für ein Midi Gerät
	 * 
	 * @param id
	 *            Names des Gerätes, muss exakt der Produktname sein
	 * @param device
	 *            Implementierung als Klasse, Instanz erstellt das Programm bei bei bedarf
	 */
	public void registerDevice(String id, Class<? extends Device> device) {
		devices.put(id, device);
		System.out.println("Register MIDI Device Impl: " + id);
	}

	/**
	 * Instanz einer Implementierung für ein Midi Gerät.
	 * 
	 * @param id
	 *            Name des Geräts
	 * @return Implementierung, DefaultDevice wenn keine vorhanden oder registriert über register
	 * @throws InstantiationException
	 *             Fehler beim instanzieren
	 * @throws IllegalAccessException
	 *             Fehler beim instanzieren
	 * @see DeviceRegistry#registerDevice(String, Class)
	 * @see DefaultDevice
	 */
	public Device getDevice(String id) throws InstantiationException, IllegalAccessException {
		if (devices.containsKey(id)) {
			System.out.println("Recognize known MIDI device: " + id);
			return devices.get(id).newInstance();
		} else {
			System.out.println("Use Default MIDI device");
			return new DefaultDevice();
		}
	}

	/**
	 * Instanz der Device Factory
	 * 
	 * @return instance
	 */
	public static DeviceRegistry getFactoryInstance() {
		return instance;
	}

	/**
	 * Handel global Events
	 */
	@Override
	public void dispatchEvent(Event event) {
		List<ListenerHandler> listeners = this.listeners.get(event.getClass().getName());
		if (listeners != null) {
			for (ListenerHandler handler : listeners) {
				handler.execute(event);
			}
		}
	}
}
