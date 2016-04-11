package de.tobias.playpad.layout.classic;

import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.CartLayout;
import de.tobias.playpad.layout.GlobalLayout;
import de.tobias.playpad.layout.LayoutConnect;
import de.tobias.playpad.viewcontroller.CartLayoutViewController;
import de.tobias.playpad.viewcontroller.GlobalLayoutViewController;
import de.tobias.playpad.viewcontroller.layout.ClassicLayoutCartViewController;
import de.tobias.playpad.viewcontroller.layout.ClassicLayoutGlobalViewController;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClassicLayoutConnect extends LayoutConnect {

	private static final String TYPE = "classic";

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(Localization.getString(Strings.Layout_Classic_Name));
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public CartLayout newCartLayout() {
		return new ClassicCartLayout();
	}

	@Override
	public GlobalLayout newGlobalLayout() {
		return new ClassicGlobalLayout();
	}

	@Override
	public CartLayoutViewController getCartLayoutViewController(CartLayout cartLayout) {
		return new ClassicLayoutCartViewController(cartLayout);
	}

	@Override
	public GlobalLayoutViewController getGlobalLayoutViewController(GlobalLayout globalLayout) {
		return new ClassicLayoutGlobalViewController(globalLayout);
	}

}
