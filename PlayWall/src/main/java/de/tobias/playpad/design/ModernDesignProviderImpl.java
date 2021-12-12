package de.tobias.playpad.design;

import de.tobias.playpad.design.modern.ModernCartDesignHandler;
import de.tobias.playpad.design.modern.ModernGlobalDesignHandler;
import de.tobias.playpad.design.modern.ModernWarningDesignHandler;

public class ModernDesignProviderImpl implements ModernDesignProvider
{
	private ModernWarningDesignHandler warningHandler;
	private ModernCartDesignHandler cartDesignHandler;
	private ModernGlobalDesignHandler globalDesignHandler;

	@Override
	public ModernWarningDesignHandler warning() {
		if (warningHandler == null) {
			warningHandler = new ModernWarningDesignHandlerImpl();
		}
		return warningHandler;
	}

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
