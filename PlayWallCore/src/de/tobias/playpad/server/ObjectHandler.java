package de.tobias.playpad.server;

import de.tobias.playpad.server.listener.SimplePropertyListener;
import de.tobias.playpad.server.listener.SimpleSetListener;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by tobias on 13.02.17.
 */
public class ObjectHandler {

	private static Set<Integer> listenered = new HashSet<>();

	private static Consumer<String> listener;

	public static Consumer<String> getListener() {
		return listener;
	}

	public static void setListener(Consumer<String> listener) {
		ObjectHandler.listener = listener;
	}

	public static void initializeObjectHandler(Object object) throws IllegalAccessException {
		if (listenered.contains(object.hashCode())) {
			return;
		}
		listenered.add(object.hashCode());

		// Get Table Name
		if (object.getClass().isAnnotationPresent(Name.class)) {
			String className = object.getClass().getAnnotation(Name.class).value();

			// Handle Simple Properties Listener
			Stream.of(object.getClass().getDeclaredFields())
					.filter(field -> field.isAnnotationPresent(Sync.class))
					.filter(field -> isPropertyClass(field.getType()))
					.forEach(field -> {
						try {
							field.setAccessible(true);
							String fieldName = field.getAnnotation(Sync.class).value();

							Property<?> property = (Property<?>) field.get(object);
							property.addListener(new SimplePropertyListener<>(object, className, fieldName));
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					});

			// Add Set Listener
			Stream.of(object.getClass().getDeclaredFields())
					.filter(field -> field.isAnnotationPresent(Sync.class))
					.filter(field -> isSetClass(field.getType()))
					.forEach(field -> {
						try {
							field.setAccessible(true);

							ObservableSet<?> set = (ObservableSet<?>) field.get(object);
							set.addListener(new SimpleSetListener<>(object, className));
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					});

		}
	}

	public static boolean checkStackTrace() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stackTrace) {
			try {
				Class<?> clazz = Class.forName(element.getClassName());
				if (clazz.isAnnotationPresent(ServerListener.class)) {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public static Optional<Object> getId(Object object) throws IllegalAccessException {
		Optional<Field> f = Stream.of(object.getClass().getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(Sync.class))
				.filter(field -> field.isAnnotationPresent(Id.class))
				.filter(field -> isClassValid(field.getType()))
				.findFirst();

		if (f.isPresent()) {
			Field field = f.get();
			field.setAccessible(true);
			return Optional.of(getValue(field, object));
		}
		return Optional.empty();
	}

	// TODO Enum Support
	public static boolean isClassValid(Class<?> clazz) {
		if (isPropertyClass(clazz)) {
			return true;
		} else if (clazz == UUID.class) {
			return true;
		}
		return false;
	}

	public static boolean isPropertyClass(Class<?> clazz) {
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

	public static boolean isSetClass(Class<?> clazz) {
		return clazz == ObservableSet.class;
	}

	public static boolean isListClass(Class<?> clazz) {
		return clazz == ObservableList.class;
	}

	public static boolean isMapClass(Class<?> clazz) {
		return clazz == ObservableMap.class;
	}

	public static Object getValue(Field field, Object obj) throws IllegalAccessException {
		if (isPropertyClass(field.getType())) {
			Property<?> property = (Property<?>) field.get(obj);
			return property.getValue();
		} else {
			return field.get(obj);
		}
	}
}
