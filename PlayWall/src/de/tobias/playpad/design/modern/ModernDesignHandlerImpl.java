package de.tobias.playpad.design.modern;

import de.tobias.playpad.design.ModernDesignHandler;

public class ModernDesignHandlerImpl implements ModernDesignHandler {

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
