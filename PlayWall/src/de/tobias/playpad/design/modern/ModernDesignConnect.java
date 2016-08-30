package de.tobias.playpad.design.modern;

import de.tobias.playpad.Strings;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.design.DesignConnect;
import de.tobias.playpad.viewcontroller.CartDesignViewController;
import de.tobias.playpad.viewcontroller.GlobalDesignViewController;
import de.tobias.playpad.viewcontroller.design.ModernCartDesignViewController;
import de.tobias.playpad.viewcontroller.design.ModernGlobalDesignViewController;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ModernDesignConnect extends DesignConnect {

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
	public CartDesign newCartDesign() {
		return new ModernCartDesign();
	}

	@Override
	public GlobalDesign newGlobalDesign() {
		return new ModernGlobalDesign();
	}

	@Override
	public CartDesignViewController getCartDesignViewController(CartDesign cartLayout) {
		return new ModernCartDesignViewController(cartLayout);
	}

	@Override
	public GlobalDesignViewController getGlobalDesignViewController(GlobalDesign globalLayout) {
		return new ModernGlobalDesignViewController(globalLayout);
	}

}
