package de.tobias.playpad.server.listener;

import com.google.gson.JsonObject;
import de.tobias.playpad.server.ObjectHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by tobias on 18.02.17.
 */
public class SimplePropertyListener<T> implements ChangeListener<T> {

	private Object object;
	private String className;
	private String fieldName;

	public SimplePropertyListener(Object object, String className, String fieldName) {
		this.object = object;
		this.className = className;
		this.fieldName = fieldName;
	}

	@Override
	public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
		if (ObjectHandler.checkStackTrace()) {
			try {
				// Get Id
				Optional<Object> idOptional = ObjectHandler.getId(object);
				if (idOptional.isPresent()) {
					Object id = idOptional.get();

					JsonObject json = new JsonObject();
					json.addProperty("class", className);
					json.addProperty("field", fieldName);

					if (id instanceof Number) {
						json.addProperty("id", (Number) id);
					} else if (id instanceof UUID || id instanceof String) {
						json.addProperty("id", id.toString());
					}

					if (newValue instanceof Number) {
						json.addProperty("value", (Number) newValue);
					} else if (newValue instanceof String) {
						json.addProperty("value", (String) newValue);
					} else if (newValue instanceof Boolean) {
						json.addProperty("value", (Boolean) newValue);
					}

					json.addProperty("type", newValue.getClass().getName());
					json.addProperty("operation", "update");

					// Send finish Json to Listener
					ObjectHandler.getListener().accept(json.toString());
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
