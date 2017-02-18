package de.tobias.playpad.server.listener;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.tobias.playpad.server.Id;
import de.tobias.playpad.server.Name;
import de.tobias.playpad.server.ObjectHandler;
import de.tobias.playpad.server.Sync;
import javafx.beans.property.Property;
import javafx.collections.SetChangeListener;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Created by tobias on 18.02.17.
 */
public class SimpleSetListener<T> implements SetChangeListener<T> {

	private Object object;
	private String className;

	public SimpleSetListener(Object object, String className) {
		this.object = object;
		this.className = className;
	}

	@Override
	public void onChanged(Change<? extends T> change) {
		if (change.wasAdded()) {
			T t = change.getElementAdded();

			// Add Listener for the new object
			try {
				ObjectHandler.initializeObjectHandler(t);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			// Get Id
			try {
				Optional<Object> idOptional = ObjectHandler.getId(object);
				Optional<Object> idChildOptional = ObjectHandler.getId(t);
				if (idOptional.isPresent() && idChildOptional.isPresent()) {
					Object id = idOptional.get();
					Object idChild = idChildOptional.get();

					JsonObject json = new JsonObject();
					json.addProperty("parent_class", className);
					json.addProperty("child_class", t.getClass().getAnnotation(Name.class).value());

					if (id instanceof Integer) {
						json.addProperty("parent_id", (Integer) id);
						json.addProperty("parent_type", Integer.class.getName());
					} else if (id instanceof UUID || id instanceof String) {
						json.addProperty("parent_id", id.toString());
						json.addProperty("parent_type", UUID.class.getName());
					}


					if (idChild instanceof Integer) {
						json.addProperty("child_id", (Integer) idChild);
					} else if (idChild instanceof UUID || id instanceof String) {
						json.addProperty("child_id", idChild.toString());
					}

					JsonArray fields = new JsonArray();
					Stream.of(t.getClass().getDeclaredFields())
							.filter(field -> field.isAnnotationPresent(Sync.class))
							.filter(field -> !field.isAnnotationPresent(Id.class))
							.filter(field -> ObjectHandler.isPropertyClass(field.getType()))
							.forEach(field -> {
								try {
									field.setAccessible(true);
									String fieldName = field.getAnnotation(Sync.class).value();
									Property<?> property = (Property<?>) field.get(t);

									Object value = property.getValue();
									if (value != null) {
										JsonObject jsonField = new JsonObject();
										jsonField.addProperty("field", fieldName);

										if (value instanceof Number) {
											jsonField.addProperty("value", (Number) value);
										} else if (value instanceof String) {
											jsonField.addProperty("value", (String) value);
										} else if (value instanceof Boolean) {
											jsonField.addProperty("value", (Boolean) value);
										}
										jsonField.addProperty("type", value.getClass().getName());

										fields.add(jsonField);
									}
								} catch (IllegalAccessException e) {
									e.printStackTrace();
								}
							});
					json.add("fields", fields);
					json.addProperty("operation", "col-add");

					ObjectHandler.getListener().accept(json.toString());
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		if (change.wasRemoved()) {
			T t = change.getElementRemoved();

			try {
				Optional<Object> idOptional = ObjectHandler.getId(object);
				Optional<Object> idChildOptional = ObjectHandler.getId(t);
				if (idOptional.isPresent() && idChildOptional.isPresent()) {
					Object id = idOptional.get();
					Object idChild = idChildOptional.get();

					JsonObject json = new JsonObject();
					json.addProperty("parent_class", className);
					json.addProperty("child_class", t.getClass().getAnnotation(Name.class).value());

					if (id instanceof Integer) {
						json.addProperty("parent_id", (Integer) id);
					} else if (id instanceof UUID || id instanceof String) {
						json.addProperty("parent_id", id.toString());
					}

					if (idChild instanceof Number) {
						json.addProperty("child_id", (Number) idChild);
					} else if (idChild instanceof UUID || id instanceof String) {
						json.addProperty("child_id", idChild.toString());
					}

					json.addProperty("operation", "col-remove");
					ObjectHandler.getListener().accept(json.toString());
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

}
