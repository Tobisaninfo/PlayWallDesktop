package de.tobias.playpad.action.mapper;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.viewcontroller.mapper.KeyboardMapperViewController;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.StringUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;
import org.dom4j.Element;

public class KeyboardMapper extends Mapper {

	private final String type;

	private KeyCode code;
	private String key;

	public KeyboardMapper(String type) {
		this(type, KeyCode.A, "A");
	}

	public KeyboardMapper(String type, KeyCode code, String key) {
		this.type = type;
		this.code = code;
		this.key = key;
		updateDisplayProperty();
	}

	public KeyCode getCode() {
		return code;
	}

	public void setCode(KeyCode code) {
		this.code = code;
		updateDisplayProperty();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
		updateDisplayProperty();
	}

	@Override
	public String getType() {
		return type;
	}

	public String getReadableName() {
		if (!StringUtils.isStringNotVisable(getKey())) {
			return getKey();
		} else {
			return getCode().getName();
		}
	}

	private static final String KEY = "key";
	private static final String CODE = "code";

	@Override
	public void load(Element element, Action action) {
		key = element.attributeValue(KEY);
		code = KeyCode.valueOf(element.attributeValue(CODE));
	}

	@Override
	public void save(Element element) {
		element.addAttribute(KEY, key);
		element.addAttribute(CODE, code.name());
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.Mapper_Keyboard_toString, getReadableName());
	}

	private StringProperty displayProperty = new SimpleStringProperty();

	@Override
	public StringProperty displayProperty() {
		updateDisplayProperty();
		return displayProperty;
	}

	private void updateDisplayProperty() {
		displayProperty.set(toString());
	}

	private KeyboardMapperViewController settingsViewController;

	@Override
	public NVC getSettingsViewController() {
		if (settingsViewController == null) {
			settingsViewController = new KeyboardMapperViewController();
		}
		settingsViewController.setMapper(this);
		return settingsViewController;
	}

	@Override
	public Mapper cloneMapper() throws CloneNotSupportedException {
		KeyboardMapper mapper = (KeyboardMapper) super.clone();

		mapper.code = code;
		mapper.key = key;
		mapper.displayProperty = new SimpleStringProperty();

		return mapper;
	}
}
