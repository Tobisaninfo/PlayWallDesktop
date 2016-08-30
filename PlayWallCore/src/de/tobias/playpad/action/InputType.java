package de.tobias.playpad.action;

/**
 * Optionne für die verschiedenen Input Möglichkeiten.
 * 
 * @author tobias
 * 
 * @since 5.0.0
 */
public enum InputType {

	/**
	 * Mapper is pressed (e.g. key on the keyboard is pressed)
	 */
	PRESSED,
	/**
	 * Mapper is releaes (e.g. key on the keyboard is released)
	 */
	RELEASED,
	/**
	 * Mapper is typed (e.g. key on the keyboard is typed - somewhere in between of pressed and released)
	 */
	TYPED;
}
