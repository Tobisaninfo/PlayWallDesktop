package de.tobias.playpad.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class EventDispatcher {

	public class ListenerHandler {

		private Method method;
		private Listener listener;

		public ListenerHandler(Method method, Listener listener) {
			this.method = method;
			this.listener = listener;
		}

		public Listener getListener() {
			return listener;
		}

		public Method getMethod() {
			return method;
		}

		public void execute(Event event) {
			try {
				if (!event.isConsume()) {
					method.setAccessible(true);
					method.invoke(listener, event);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public abstract void dispatchEvent(Event event);

	protected HashMap<String, List<ListenerHandler>> listeners = new HashMap<>();

	public void registerEventListener(Listener listener) {
		for (Method method : listener.getClass().getDeclaredMethods()) {
			if (method.isAnnotationPresent(EventHandler.class)) {
				Class<?>[] parameters = method.getParameterTypes();
				if (parameters.length == 1) {
					Class<?> eventType = parameters[0];

					if (eventType.getSuperclass().equals(Event.class)) {
						if (!listeners.containsKey(eventType.getName())) {
							listeners.put(eventType.getName(), new ArrayList<>());
						}
						listeners.get(eventType.getName()).add(new ListenerHandler(method, listener));
					} else {
						System.err.println("Method: " + method.getName() + " has wrong arguments");
					}
				} else {
					System.err.println("Method: " + method.getName() + " has wrong arguments");
				}
			}
		}
	}
}
