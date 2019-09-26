package de.tobias.playpad.design;

import de.tobias.playpad.design.modern.ModernCartDesignHandler;
import de.tobias.playpad.design.modern.ModernGlobalDesignHandler;

public interface ModernDesignHandler {
	ModernGlobalDesignHandler global();

	ModernCartDesignHandler cart();
}
