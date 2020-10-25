package de.tobias.playpad;

import de.thecodelabs.utils.util.Localization;

import java.util.Locale;

public class PlayPadLocalizationDelegate implements Localization.LocalizationDelegate {
	@Override
	public String[] getBaseResources() {
		return new String[]{
				"lang/",
				"lang/ui"
		};
	}

	@Override
	public boolean useMultipleResourceBundles() {
		return true;
	}

	@Override
	public Locale getLocale() {
		// TODO Locale GERMAN
		return Locale.GERMAN;
	}
}
