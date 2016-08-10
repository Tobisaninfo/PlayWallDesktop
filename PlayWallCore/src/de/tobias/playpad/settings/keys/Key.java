package de.tobias.playpad.settings.keys;

import de.tobias.playpad.Displayable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCombination;

public class Key implements Displayable {

	private String id;

	private String key;
	private boolean ctrl;
	private boolean alt;
	private boolean meta;
	private boolean shift;

	public Key(String id) {
		this.id = id;
	}

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

	private StringProperty displayProperty = new SimpleStringProperty();

	@Override
	public StringProperty displayProperty() {
		displayProperty.set(toString());
		return displayProperty;
	}
}
