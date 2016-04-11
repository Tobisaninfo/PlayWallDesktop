package de.tobias.playpad.layout.modern;

import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.CartLayout;
import de.tobias.playpad.layout.GlobalLayout;
import de.tobias.playpad.layout.LayoutConnect;
import de.tobias.playpad.viewcontroller.CartLayoutViewController;
import de.tobias.playpad.viewcontroller.GlobalLayoutViewController;
import de.tobias.playpad.viewcontroller.layout.ModernLayoutCartViewController;
import de.tobias.playpad.viewcontroller.layout.ModernLayoutGlobalViewController;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ModernLayoutConnect extends LayoutConnect {

	private static final String TYPE = "modern";

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(Localization.getString(Strings.Layout_Modern_Name));
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public CartLayout newCartLayout() {
		return new ModernLayoutCart();
	}

	@Override
	public GlobalLayout newGlobalLayout() {
		return new ModernLayoutGlobal();
	}

	@Override
	public CartLayoutViewController getCartLayoutViewController(CartLayout cartLayout) {
		return new ModernLayoutCartViewController(cartLayout);
	}

	@Override
	public GlobalLayoutViewController getGlobalLayoutViewController(GlobalLayout globalLayout) {
		return new ModernLayoutGlobalViewController(globalLayout);
	}

}
