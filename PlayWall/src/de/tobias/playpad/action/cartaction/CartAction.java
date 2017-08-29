package de.tobias.playpad.action.cartaction;

import de.tobias.utils.nui.NVC;
import org.dom4j.Element;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.feedback.ColorAdjustable;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.actions.CartActionViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CartAction extends Action implements ColorAdjustable {

	public enum ControlMode {
		PLAY_PAUSE, PLAY_STOP, PLAY_HOLD
	}

	private final String type;

	private int x;
	private int y;

	private ControlMode mode;
	private boolean autoFeedbackColors;

	private transient Pad pad;
	private transient PadContentFeedbackListener padContentFeedbackListener = new PadContentFeedbackListener();
	private transient PadStatusFeedbackListener padStatusFeedbackListener = new PadStatusFeedbackListener();
	private transient PadPositionWarningListener padPositionListener = new PadPositionWarningListener(this);

	public CartAction(String type) {
		this(type, 0, 0, ControlMode.PLAY_STOP);
		this.autoFeedbackColors = true;
	}

	public CartAction(String type, int x, int y, ControlMode mode) {
		this.type = type;
		this.x = x;
		this.y = y;

		this.mode = mode;
		this.autoFeedbackColors = true;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public ControlMode getMode() {
		return mode;
	}

	public void setMode(ControlMode mode) {
		this.mode = mode;
	}

	@Override
	public boolean isAutoFeedbackColors() {
		return autoFeedbackColors;
	}

	public void setAutoFeedbackColors(boolean autoFeedbackColors) {
		this.autoFeedbackColors = autoFeedbackColors;
	}

	// Helper
	@Override
	public Pad getPad() {
		return pad;
	}

	@Override
	public void init(Project project, IMainViewController controller) {
		Pad pad = project.getPad(x, y, controller.getPage());
		if (pad != null) {
			setPad(pad);
		}
	}

	@Override
	public void showFeedback(Project project, IMainViewController controller) {
		if (pad != null) {
			// init first feedback
			padStatusFeedbackListener.changed(null, null, pad.getStatus());
		}
	}

	@Override
	public void clearFeedback() {
		setPad(null);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CartAction) {
			CartAction action2 = (CartAction) obj;
			if (action2.x == x && action2.y == y) {
				return true;
			}
		}
		return super.equals(obj);
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void performAction(InputType type, Project project, IMainViewController mainViewController) {
		setPad(project.getPad(x, y, mainViewController.getPage()));

		if (pad == null) {
			return;
		}

		// wird nur ausgeführt, wenn das Pad ein Content hat und sichtbar in der GUI (Gilt für MIDI und Keyboard)
		if (pad.getContent() != null && pad.getContent().isPadLoaded() && pad.isPadVisible()) {
			switch (mode) {
			case PLAY_STOP:
				if (type == InputType.PRESSED) {
					if (pad.getStatus() == PadStatus.PLAY) {
						pad.setStatus(PadStatus.STOP);
					} else {
						// Allow the listener to send the feedback
						padPositionListener.setSend(false);

						pad.setStatus(PadStatus.PLAY);
					}
				}
				break;
			case PLAY_HOLD:
				if (type == InputType.PRESSED) {
					if (pad.getStatus() == PadStatus.READY) {
						// Allow the listener to send the feedback
						padPositionListener.setSend(false);

						pad.setStatus(PadStatus.PLAY);
					}
				} else if (type == InputType.RELEASED) {
					if (pad.getStatus() == PadStatus.PLAY) {
						pad.setStatus(PadStatus.STOP);
					}
				}
				break;
			case PLAY_PAUSE:
				if (type == InputType.PRESSED) {
					if (pad.getStatus() == PadStatus.PLAY) {
						pad.setStatus(PadStatus.PAUSE);
					} else {
						// Allow the listener to send the feedback
						padPositionListener.setSend(false);

						pad.setStatus(PadStatus.PLAY);
					}
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public FeedbackType geFeedbackType() {
		return FeedbackType.DOUBLE;
	}

	private static final String X_ATTR = "x";
	private static final String Y_ATTR = "y";
	private static final String CONTROL_MDOE = "mode";
	private static final String AUTO_FEEDBACK_COLORS = "autoColor";

	@Override
	public void load(Element root) {
		if (root.attributeValue(X_ATTR) != null)
			x = Integer.valueOf(root.attributeValue(X_ATTR));
		if (root.attributeValue(Y_ATTR) != null)
			y = Integer.valueOf(root.attributeValue(Y_ATTR));
		mode = ControlMode.valueOf(root.attributeValue(CONTROL_MDOE));
		autoFeedbackColors = Boolean.valueOf(root.attributeValue(AUTO_FEEDBACK_COLORS));
	}

	@Override
	public void save(Element root) {
		root.addAttribute(X_ATTR, String.valueOf(x));
		root.addAttribute(Y_ATTR, String.valueOf(y));
		root.addAttribute(CONTROL_MDOE, mode.name());
		root.addAttribute(AUTO_FEEDBACK_COLORS, String.valueOf(autoFeedbackColors));
	}

	void setPad(Pad newPad) {
		Pad oldPad = this.pad;
		if (newPad == null || !newPad.equals(oldPad) || oldPad == null) {
			// Remove old Listener
			if (oldPad != null) {
				if (oldPad.getContent() != null) {
					if (oldPad.getContent() instanceof Durationable) {
						Durationable durationable = (Durationable) oldPad.getContent();
						durationable.positionProperty().removeListener(padPositionListener);
					}
				}
				oldPad.contentProperty().removeListener(padContentFeedbackListener);
				oldPad.statusProperty().removeListener(padStatusFeedbackListener);
			}

			this.pad = newPad;

			padPositionListener.setPad(newPad);
			padStatusFeedbackListener.setAction(this);
			padContentFeedbackListener.setAction(this);

			if (newPad != null) {
				// Add new listener
				if (newPad.getContent() != null) {
					if (newPad.getContent() instanceof Durationable) {
						Durationable durationable = (Durationable) newPad.getContent();
						durationable.positionProperty().addListener(padPositionListener);
					}
				}

				newPad.statusProperty().addListener(padStatusFeedbackListener);
				newPad.contentProperty().addListener(padContentFeedbackListener);
			}
		}
	}

	PadPositionWarningListener getPadPositionListener() {
		return padPositionListener;
	}

	// Helper
	@Override
	public Action cloneAction() throws CloneNotSupportedException {
		CartAction action = (CartAction) super.clone();

		action.autoFeedbackColors = autoFeedbackColors;
		action.x = x;
		action.y = y;
		action.mode = mode;

		return action;
	}

	// UI Helper
	@Override
	public String toString() {
		return Localization.getString(Strings.Action_Cart_toString, String.valueOf(x) + ", " + String.valueOf(y));
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(toString());
	}

	private static CartActionViewController cartActionViewController;

	@Override
	public NVC getSettingsViewController() {
		if (cartActionViewController == null) {
			cartActionViewController = new CartActionViewController();
		}
		cartActionViewController.setCartAction(this);
		return cartActionViewController;
	}
}
