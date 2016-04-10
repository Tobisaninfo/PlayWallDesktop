package de.tobias.playpad.layout;

import de.tobias.playpad.viewcontroller.CartLayoutViewController;
import de.tobias.playpad.viewcontroller.GlobalLayoutViewController;

public class LayoutContainer {

	private String id;
	String name;
	Class<? extends GlobalLayout> globalClass;
	Class<? extends CartLayout> cartClass;

	Class<? extends GlobalLayoutViewController> globalVC;
	Class<? extends CartLayoutViewController> cartVC;

	public LayoutContainer(String id, String name, Class<? extends GlobalLayout> globalClass, Class<? extends CartLayout> cartClass,
			Class<? extends GlobalLayoutViewController> globalVC, Class<? extends CartLayoutViewController> cartCV) {
		this.id = id;
		this.name = name;
		this.globalClass = globalClass;
		this.cartClass = cartClass;
		this.globalVC = globalVC;
		this.cartVC = cartCV;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Class<? extends GlobalLayout> getGlobalClass() {
		return globalClass;
	}

	public Class<? extends CartLayout> getCartClass() {
		return cartClass;
	}

	public Class<? extends GlobalLayoutViewController> getGlobalVC() {
		return globalVC;
	}

	public Class<? extends CartLayoutViewController> getCartVC() {
		return cartVC;
	}
}