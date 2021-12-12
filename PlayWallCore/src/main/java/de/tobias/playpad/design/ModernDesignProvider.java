package de.tobias.playpad.design;

import de.tobias.playpad.design.modern.ModernCartDesignHandler;
import de.tobias.playpad.design.modern.ModernGlobalDesignHandler;
import de.tobias.playpad.design.modern.ModernWarningDesignHandler;

public interface ModernDesignProvider
{
	ModernWarningDesignHandler warning();

	ModernGlobalDesignHandler global();

	ModernCartDesignHandler cart();
}
