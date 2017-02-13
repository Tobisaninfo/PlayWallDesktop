package de.tobias.playpad.server;

import com.google.gson.JsonObject;
import javafx.beans.property.*;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by tobias on 13.02.17.
 */
public class ObjectHandler {

	private static Consumer<String> listener;

	public static void setListener(Consumer<String> listener) {
		ObjectHandler.listener = listener;
	}

	public static void initializeObjectHandler(Object object) throws IllegalAccessException {
		// Get Table Name
		if (object.getClass().isAnnotationPresent(Name.class)) {
			String name = object.getClass().getAnnotation(Name.class).value();

			Stream.of(object.getClass().getDeclaredFields())
					.filter(field -> field.isAnnotationPresent(Sync.class))
					.filter(field -> isClassValid(field.getType()))
					.forEach(field -> {
						try {
							field.setAccessible(true);
							String fieldName = field.getAnnotation(Sync.class).value();

							Property<?> property = (Property<?>) field.get(object);
							property.addListener((observable, oldValue, newValue) -> {
								// Get Id
								try {
									Optional<Number> idOptional = getId(object);
									if (idOptional.isPresent()) {
										Number id = idOptional.get();

										JsonObject json = new JsonObject();
										json.addProperty("class", name);
										json.addProperty("field", fieldName);
										json.addProperty("id", id);

										if (newValue instanceof Number) {
											json.addProperty("value", (Number) newValue);
										} else if (newValue instanceof String) {
											json.addProperty("value", (String) newValue);
										} else if (newValue instanceof Boolean) {
											json.addProperty("value", (Boolean) newValue);
										}
										json.addProperty("type", newValue.getClass().getName());
										json.addProperty("operation", "update");

										listener.accept(json.toString());
									}
								} catch (IllegalAccessException e) {
									e.printStackTrace();
								}
							});
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					});

		}
	}

	private static Optional<Number> getId(Object object) throws IllegalAccessException {
		Optional<Field> f = Stream.of(object.getClass().getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(Sync.class))
				.filter(field -> field.isAnnotationPresent(Id.class))
				.filter(field -> isClassValid(field.getType()))
				.findFirst();

		if (f.isPresent()) {
			Field field = f.get();
			field.setAccessible(true);
			Property<?> property = (Property<?>) field.get(object);
			Object value = property.getValue();
			if (value instanceof Number) {
				return Optional.of((Number) value);
			}
		}
		return Optional.empty();
	}

	// TODO Enum Support
	private static boolean isClassValid(Class<?> clazz) {
		if (clazz == IntegerProperty.class) {
			return true;
		} else if (clazz == DoubleProperty.class) {
			return true;
		} else if (clazz == FloatProperty.class) {
			return true;
		} else if (clazz == LongProperty.class) {
			return true;
		} else if (clazz == StringProperty.class) {
			return true;
		} else if (clazz == BooleanProperty.class) {
			return true;
		}
		return false;
	}
}
