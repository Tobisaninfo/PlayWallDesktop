package de.tobias.playpad.settings.keys;

import de.tobias.playpad.Displayable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCombination;

/**
 * Eine Tastenkombination für das Mapping zum Menü.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public class Key implements Displayable {

	private String id;

	private String key;
	private boolean ctrl;
	private boolean alt;
	private boolean meta;
	private boolean shift;

	/**
	 * Erstellt eine leere Tastenkombination.
	 * 
	 * @param id
	 *            ID für die Speicherung
	 */
	public Key(String id) {
		this.id = id;
	}

	/**
	 * Erstellt eine Vollständige Tastenkombination mit ID und Datenwerten.
	 * 
	 * @param id
	 *            ID f+r doe Speicherung
	 * @param key
	 *            Taste
	 * @param ctrl
	 *            ctrl
	 * @param alt
	 *            alt
	 * @param meta
	 *            meta (Mac: CMD)
	 * @param shift
	 *            shift
	 */
	public Key(String id, String key, boolean ctrl, boolean alt, boolean meta, boolean shift) {
		this.id = id;

		this.key = key;
		this.ctrl = ctrl;
		this.alt = alt;
		this.meta = meta;
		this.shift = shift;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;

		displayProperty.set(toString());
	}

	public boolean isCtrl() {
		return ctrl;
	}

	public void setCtrl(boolean ctrl) {
		this.ctrl = ctrl;

		displayProperty.set(toString());
	}

	public boolean isAlt() {
		return alt;
	}

	public void setAlt(boolean alt) {
		this.alt = alt;

		displayProperty.set(toString());
	}

	public boolean isMeta() {
		return meta;
	}

	public void setMeta(boolean meta) {
		this.meta = meta;

		displayProperty.set(toString());
	}

	public boolean isShift() {
		return shift;
	}

	public void setShift(boolean shift) {
		this.shift = shift;

		displayProperty.set(toString());
	}

	public String getId() {
		return id;
	}

	/**
	 * Gibt die Tastenkombination als String für das Menü in JavaFX zurück.
	 * 
	 * @return KeyCombination Readable
	 */
	public String getKeyCode() {
		StringBuilder builder = new StringBuilder();

		if (ctrl)
			builder.append("ctrl+");
		if (alt)
			builder.append("alt+");
		if (meta)
			builder.append("meta+");
		if (shift)
			builder.append("shift+");
		builder.append(key);

		return builder.toString();
	}

	/**
	 * Gibt die Tastenkombination von JavaFX geparsed zurück. Diese wird dann für das Menü und die Darstellung verwendet.
	 */
	@Override
	public String toString() {
		if (!getKeyCode().isEmpty())
			try {
				return KeyCombination.valueOf(getKeyCode()).getDisplayText();
			} catch (IllegalArgumentException e) {
				return "";
			}
		else
			return "";
	}

	private transient StringProperty displayProperty = new SimpleStringProperty();

	@Override
	public StringProperty displayProperty() {
		displayProperty.set(toString());
		return displayProperty;
	}

	public boolean isEmpty() {
		return key.isEmpty() && !ctrl && !shift & !meta && !alt;
	}
}
