package de.tobias.playpad.action.actions.cart.handler;

import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.actions.cart.CartAction;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;

public abstract class CartActionHandler {

	public abstract void performAction(InputType type, CartAction cartAction, Pad pad, Project project);
}
