package de.tobias.playpad.design.classic;

import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.DesignFactory;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.viewcontroller.CartDesignViewController;
import de.tobias.playpad.viewcontroller.GlobalDesignViewController;
import de.tobias.playpad.viewcontroller.design.ClassicCartDesignViewController;
import de.tobias.playpad.viewcontroller.design.ClassicGlobalDesignViewController;
import de.tobias.utils.ui.icon.FontIconType;

public class ClassicDesignFactory extends DesignFactory {


	public ClassicDesignFactory(String type) {
		super(type);
	}

	@Override
	public CartDesign newCartDesign(Pad pad) {
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
