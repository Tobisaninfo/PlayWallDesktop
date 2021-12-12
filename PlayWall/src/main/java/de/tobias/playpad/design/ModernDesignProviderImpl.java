package de.tobias.playpad.design;

import de.tobias.playpad.design.modern.ModernCartDesignHandler;
import de.tobias.playpad.design.modern.ModernGlobalDesignHandler;

public class ModernDesignProviderImpl implements ModernDesignProvider
{

	private ModernCartDesignHandler cartDesignHandler;
	private ModernGlobalDesignHandler globalDesignHandler;

	@Override
	public ModernGlobalDesignHandler global() {
		if (globalDesignHandler == null) {
			globalDesignHandler = new ModernGlobalDesignHandlerImpl();
		}
		return globalDesignHandler;
	}

	@Override
	public ModernCartDesignHandler cart() {
		if (cartDesignHandler == null) {
			cartDesignHandler = new ModernCartDesignHandlerImpl();
		}
		return cartDesignHandler;
	}
}
