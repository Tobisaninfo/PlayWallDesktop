package de.tobias.playpad.settings;

import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.profile.ProfileSettings;
import javafx.util.Duration;
import org.dom4j.Element;

/**
 * Einstellungen zum Fade, zusammengefasst in dieser Klasse.
 *
 * @author tobias
 * @see ProfileSettings#getFade()
 * @see PadSettings#getFade()
 * @since 6.0.0
 */
public class Fade {

	private Duration fadeIn;
	private Duration fadeOut;

	private boolean fadeInStart;
	private boolean fadeInPause;
	private boolean fadeOutPause;
	private boolean fadeOutStop;

	/**
	 * Erstellt ein neues Fade mit den Default Werten. (Fade Dauer: 0 sec)
	 */
	public Fade() {
		this(Duration.ONE, Duration.ONE);
	}

	/**
	 * Erstellt einen neues Fade mit Custom Zeiten und Default Einstellungen für Play, Pause, Stop.
	 *
	 * @param fadeIn  Fade In Dauer
	 * @param fadeOut Fade Out Dauer
	 */
	public Fade(Duration fadeIn, Duration fadeOut) {
		this(fadeIn, fadeOut, false, true, true, true);
	}

	/**
	 * Erstellt ein Fade mit Custom Werten.
	 *
	 * @param fadeIn       Fade In Dauer
	 * @param fadeOut      Fade Out Dauer
	 * @param fadeInStart  Fade beim Start
	 * @param fadeInPause  Fade nach Pause
	 * @param fadeOutPause Fade vor Pause
	 * @param fadeOutStop  Fade vor Stop
	 */
	public Fade(Duration fadeIn, Duration fadeOut, boolean fadeInStart, boolean fadeInPause, boolean fadeOutPause, boolean fadeOutStop) {
		this.fadeIn = fadeIn;
		this.fadeOut = fadeOut;
		this.fadeInStart = fadeInStart;
		this.fadeInPause = fadeInPause;
		this.fadeOutPause = fadeOutPause;
		this.fadeOutStop = fadeOutStop;
	}

	public Duration getFadeIn() {
		return fadeIn;
	}

	public Duration getFadeOut() {
		return fadeOut;
	}

	public void setFadeIn(Duration fadeIn) {
		this.fadeIn = fadeIn;
	}

	public void setFadeOut(Duration fadeOut) {
		this.fadeOut = fadeOut;
	}

	public boolean isFadeInStart() {
		return fadeInStart;
	}

	public void setFadeInStart(boolean fadeInStart) {
		this.fadeInStart = fadeInStart;
	}

	public boolean isFadeInPause() {
		return fadeInPause;
	}

	public void setFadeInPause(boolean fadeInPause) {
		this.fadeInPause = fadeInPause;
	}

	public boolean isFadeOutPause() {
		return fadeOutPause;
	}

	public void setFadeOutPause(boolean fadeOutPause) {
		this.fadeOutPause = fadeOutPause;
	}

	public boolean isFadeOutStop() {
		return fadeOutStop;
	}

	public void setFadeOutStop(boolean fadeOutStop) {
		this.fadeOutStop = fadeOutStop;
	}

	/*
	 * Serialize
	 */

	private static final String FADE_OUT = "FadeOut";
	private static final String FADE_IN = "FadeIn";

	private static final String ON_STOP_ATTR = "onStop";
	private static final String ON_PAUSE_ATTR = "onPause";
	private static final String ON_START_ATTR = "onStart";

	public void save(Element container) {
		Element fadeInElement = container.addElement(FADE_IN);
		fadeInElement.addText(fadeIn.toString());
		fadeInElement.addAttribute(ON_START_ATTR, String.valueOf(fadeInStart));
		fadeInElement.addAttribute(ON_PAUSE_ATTR, String.valueOf(fadeInPause));

		Element fadeOutElement = container.addElement(FADE_OUT);
		fadeOutElement.addText(fadeOut.toString());
		fadeOutElement.addAttribute(ON_PAUSE_ATTR, String.valueOf(fadeOutPause));
		fadeOutElement.addAttribute(ON_STOP_ATTR, String.valueOf(fadeOutStop));
	}

	public static Fade load(Element container) {
		try {
			Fade fade = new Fade();

			Element fadeInElement = container.element(FADE_IN);
			if (fadeInElement.attributeValue(ON_PAUSE_ATTR) != null)
				fade.setFadeInStart(Boolean.valueOf(fadeInElement.attributeValue(ON_START_ATTR)));
			if (fadeInElement.attributeValue(ON_STOP_ATTR) != null)
				fade.setFadeInPause(Boolean.valueOf(fadeInElement.attributeValue(ON_PAUSE_ATTR)));
			fade.setFadeIn(Duration.valueOf(fadeInElement.getStringValue().replace(" ", "")));

			Element fadeOutElement = container.element(FADE_OUT);
			if (fadeOutElement.attributeValue(ON_PAUSE_ATTR) != null)
				fade.setFadeOutPause(Boolean.valueOf(fadeOutElement.attributeValue(ON_PAUSE_ATTR)));
			if (fadeOutElement.attributeValue(ON_STOP_ATTR) != null)
				fade.setFadeOutStop(Boolean.valueOf(fadeOutElement.attributeValue(ON_STOP_ATTR)));
			fade.setFadeOut(Duration.valueOf(fadeOutElement.getStringValue().replace(" ", "")));
			return fade;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
