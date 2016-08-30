package de.tobias.playpad.design.classic;

import de.tobias.playpad.Strings;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.design.DesignConnect;
import de.tobias.playpad.viewcontroller.CartDesignViewController;
import de.tobias.playpad.viewcontroller.GlobalDesignViewController;
import de.tobias.playpad.viewcontroller.design.ClassicCartDesignViewController;
import de.tobias.playpad.viewcontroller.design.ClassicGlobalDesignViewController;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClassicDesignConnect extends DesignConnect {

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
	public CartDesign newCartDesign() {
		return new ClassicCartDesign();
	}

	@Override
	public GlobalDesign newGlobalDesign() {
		return new ClassicGlobalDesign();
	}

	@Override
	public CartDesignViewController getCartDesignViewController(CartDesign cartLayout) {
		return new ClassicCartDesignViewController(cartLayout);
	}

	@Override
	public GlobalDesignViewController getGlobalDesignViewController(GlobalDesign globalLayout) {
		return new ClassicGlobalDesignViewController(globalLayout);
	}

}
