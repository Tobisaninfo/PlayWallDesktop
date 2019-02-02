package de.tobias.playpad.design;

import de.tobias.playpad.design.modern.ModernCartDesignHandler;
import de.tobias.playpad.design.modern.ModernGlobalDesignHandler;

public class ModernDesignHandlerImpl implements ModernDesign {

	private ModernCartDesignHandler cartDesignHandler;
	private ModernGlobalDesignHandler globalDesignHandler;

	@Override
	public ModernGlobalDesignHandler getModernGlobalDesignHandler() {
		if (globalDesignHandler == null) {
			globalDesignHandler = new ModernGlobalDesignHandlerImpl();
		}
		return globalDesignHandler;
	}

	@Override
	public ModernCartDesignHandler getModernCartDesignHandler() {
		if (cartDesignHandler == null) {
			cartDesignHandler = new ModernCartDesignHandlerImpl();
		}
		return cartDesignHandler;
	}
}
