package de.tobias.playpad.design.modern;

import de.tobias.playpad.design.modern.model.ModernCartDesign;

public interface ModernCartDesignHandler extends ModernDesignHandlerParent {

	String generateCss(ModernCartDesign design, String classSuffix, boolean flat);
}
