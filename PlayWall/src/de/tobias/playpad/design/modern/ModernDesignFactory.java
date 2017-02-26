package de.tobias.playpad.design.modern;

import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.DesignFactory;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.viewcontroller.CartDesignViewController;
import de.tobias.playpad.viewcontroller.GlobalDesignViewController;
import de.tobias.playpad.viewcontroller.design.ModernCartDesignViewController;
import de.tobias.playpad.viewcontroller.design.ModernGlobalDesignViewController;

public class ModernDesignFactory extends DesignFactory {


	public ModernDesignFactory(String type) {
		super(type);
	}

	@Override
	public CartDesign newCartDesign(Pad pad) {
		return new ModernCartDesign(pad);
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
