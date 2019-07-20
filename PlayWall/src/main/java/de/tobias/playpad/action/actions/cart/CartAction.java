package de.tobias.playpad.action.actions.cart;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.event.KeyEvent;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.thecodelabs.midi.mapping.MidiKey;
import de.thecodelabs.midi.midi.Midi;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.actions.cart.handler.CartActionHandlerFactory;
import de.tobias.playpad.action.feedback.ActionFeedbackSuggester;
import de.tobias.playpad.action.feedback.ColorAdjuster;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PageCoordinate;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CartAction extends ActionHandler implements ActionFeedbackSuggester {

	public static final String TYPE = "CartAction";
	public static final String PAYLOAD_X = "x";
	public static final String PAYLOAD_Y = "y";
	public static final String PAYLOAD_MODE = "mode";
	public static final String PAYLOAD_AUTO_FEEDBACK = "autoFeedback";

	public enum CartActionMode {
		PLAY_PAUSE,
		PLAY_STOP,
		PLAY_HOLD,
		PLAY_PLAY;

		public static CartActionMode valueOf(Action action) {
			return valueOf(action.getPayload(PAYLOAD_MODE));
		}
	}

	// TODO: Add listener

	@Override
	public String actionType() {
		return TYPE;
	}

	@Override
	public FeedbackType handle(KeyEvent keyEvent, Action action) {
		Pad pad = getPad(action);
		CartActionMode mode = getMode(action);

		if (pad == null) {
			return FeedbackType.NONE;
		}

		if (pad.hasVisibleContent()) {
			CartActionHandlerFactory.getInstance(mode).performAction(keyEvent.getKeyEventType(), this, pad);
		}
		return null;
	}

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Override
	public FeedbackType getCurrentFeedbackType(Action action) {
		Project project = PlayPadPlugin.getInstance().getCurrentProject();
		IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();

		int x = getX(action);
		int y = getY(action);

		Pad pad = project.getPad(x, y, mainViewController.getPage());

		switch (pad.getStatus()) {
			case EMPTY:
			case ERROR:
				return FeedbackType.NONE;
			case PLAY:
				if (pad.getContent() instanceof Durationable) {
					Durationable durationable = (Durationable) pad.getContent();
					PadSettings padSettings = pad.getPadSettings();

					if (!padSettings.isLoop()) {
						Duration warning = padSettings.getWarning();
						Duration rest = durationable.getDuration().subtract(durationable.getPosition());
						double seconds = rest.toSeconds();

						if (warning.toSeconds() > seconds) {
							return FeedbackType.WARNING;
						}
					}
				}
				return FeedbackType.EVENT;
			case PAUSE:
			case READY:
			case STOP:
				return FeedbackType.DEFAULT;
			default:
				return FeedbackType.NONE;
		}
	}

	public static void refreshFeedback(Pad pad) {
		final Mapping mapping = Mapping.getCurrentMapping();
		final PageCoordinate coordinate = pad.getPageCoordinate();

		Map<String, String> payload = new HashMap<>();
		payload.put(CartAction.PAYLOAD_X, String.valueOf(coordinate.getX()));
		payload.put(CartAction.PAYLOAD_Y, String.valueOf(coordinate.getY()));

		final Optional<Action> action = mapping.getActionForTypeWithPayload(CartAction.TYPE, payload);
		action.ifPresent(value -> Midi.getInstance().showFeedback(value));
	}

	/*
	Color Adjustable
	 */

	@Override
	public boolean isAutoFeedbackColors(Action action) {
		return isAutoFeedback(action);
	}

	@Override
	public void suggestFeedback(Action action) {
		for (MidiKey midiKey : action.getKeysForType(MidiKey.class)) {
			ColorAdjuster.setSuggestedFeedbackColors(this, action, midiKey);
		}
	}

	@Override
	public byte suggestFeedbackChannel(FeedbackType type) {
		switch (type) {
			case DEFAULT:
				return 0;
			case EVENT:
				return 2;
			case WARNING:
				return 1;
		}
		return 0;
	}

	@Override
	public Pad getPad(Action action) {
		Project project = PlayPadPlugin.getInstance().getCurrentProject();
		IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();

		int x = getX(action);
		int y = getY(action);

		return project.getPad(x, y, mainViewController.getPage());
	}

	/*

	 */
	public static CartActionMode getMode(Action action) {
		return CartActionMode.valueOf(action);
	}

	public static int getY(Action action) {
		return Integer.parseInt(action.getPayload(PAYLOAD_Y));
	}

	public static int getX(Action action) {
		return Integer.parseInt(action.getPayload(PAYLOAD_X));
	}

	public static boolean isAutoFeedback(Action action) {
		return Boolean.parseBoolean(action.getPayload(PAYLOAD_AUTO_FEEDBACK));
	}

	public static void setMode(Action action, CartActionMode mode) {
		action.addPayloadEntry(CartAction.PAYLOAD_MODE, mode.name());
	}

	public static void getY(Action action, int y) {
		action.addPayloadEntry(CartAction.PAYLOAD_MODE, String.valueOf(y));
	}

	public static void getX(Action action, int x) {
		action.addPayloadEntry(CartAction.PAYLOAD_MODE, String.valueOf(x));
	}

	public static void setAutoFeedback(Action action, boolean enable) {
		action.addPayloadEntry(CartAction.PAYLOAD_MODE, String.valueOf(enable));
	}
}
