package de.tobias.playpad.action.cartaction;

import org.dom4j.Element;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.connect.CartActionConnect;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.Durationable;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.actions.CartActionViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CartAction extends Action {

	public enum ControlMode {
		PLAY_PAUSE,
		PLAY_STOP,
		PLAY_HOLD;
	}

	private int cart;
	private ControlMode mode;
	private boolean autoFeedbackColors;

	private transient Pad pad;
	private transient PadStatusFeedbackListener padStatusFeedbackListener = new PadStatusFeedbackListener();
	private transient PadPositionWarningListener padPositionListener = new PadPositionWarningListener(this);

	public CartAction() {
		this.cart = 0;
		this.mode = ControlMode.PLAY_STOP;
		this.autoFeedbackColors = true;
	}

	public CartAction(int cart, ControlMode mode) {
		this.cart = cart;
		this.mode = mode;
		this.autoFeedbackColors = true;
	}

	public int getCart() {
		return cart;
	}

	public void setCart(int cart) {
		this.cart = cart;
	}

	public ControlMode getMode() {
		return mode;
	}

	public void setMode(ControlMode mode) {
		this.mode = mode;
	}

	public boolean isAutoFeedbackColors() {
		return autoFeedbackColors;
	}

	public void setAutoFeedbackColors(boolean autoFeedbackColors) {
		this.autoFeedbackColors = autoFeedbackColors;
	}

	// Helper
	public Pad getPad() {
		return pad;
	}

	@Override
	public void initFeedback(Project project, IMainViewController controller) {
		Pad pad = project.getPad(cart);
		setPad(pad);
		// init first feedback
		padStatusFeedbackListener.changed(null, null, pad.getStatus());
	}

	@Override
	public void clearFeedback() {
		setPad(null);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CartAction) {
			CartAction action2 = (CartAction) obj;
			if (action2.cart == cart) {
				return true;
			}
		}
		return super.equals(obj);
	}

	@Override
	public String getType() {
		return CartActionConnect.TYPE;
	}

	@Override
	public void performAction(InputType type, Project project, IMainViewController mainViewController) {
		setPad(project.getPad(cart));

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

	private static final String CART_ID = "id";
	private static final String CONTROL_MDOE = "mode";
	private static final String AUTO_FEEDBACK_COLORS = "autoColor";

	@Override
	public void load(Element root) {
		cart = Integer.valueOf(root.attributeValue(CART_ID));
		mode = ControlMode.valueOf(root.attributeValue(CONTROL_MDOE));
		autoFeedbackColors = Boolean.valueOf(root.attributeValue(AUTO_FEEDBACK_COLORS));
	}

	@Override
	public void save(Element root) {
		root.addAttribute(CART_ID, String.valueOf(cart));
		root.addAttribute(CONTROL_MDOE, mode.name());
		root.addAttribute(AUTO_FEEDBACK_COLORS, String.valueOf(autoFeedbackColors));
	}

	public void setPad(Pad newPad) {
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
				oldPad.statusProperty().removeListener(padStatusFeedbackListener);
			}

			this.pad = newPad;

			if (newPad != null) {
				// Add new listener
				if (newPad.getContent() != null) {
					if (newPad.getContent() instanceof Durationable) {
						padPositionListener.setPad(newPad);
						Durationable durationable = (Durationable) newPad.getContent();
						durationable.positionProperty().addListener(padPositionListener);
					}
				}
				padStatusFeedbackListener.setAction(this);
				newPad.statusProperty().addListener(padStatusFeedbackListener);
			}
		}
	}

	// Listener
	public PadPositionWarningListener getPadPositionListener() {
		return padPositionListener;
	}

	public PadStatusFeedbackListener getPadStatusFeedbackListener() {
		return padStatusFeedbackListener;
	}

	// Helper
	@Override
	public Action cloneAction() throws CloneNotSupportedException {
		CartAction action = (CartAction) super.clone();

		action.autoFeedbackColors = autoFeedbackColors;
		action.cart = cart;
		action.mode = mode;

		return action;
	}

	// UI Helper
	@Override
	public String toString() {
		return Localization.getString(Strings.Action_Cart_toString, String.valueOf(cart + 1));
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(toString());
	}

	private static CartActionViewController cartActionViewController;

	@Override
	public ContentViewController getSettingsViewController() {
		if (cartActionViewController == null) {
			cartActionViewController = new CartActionViewController();
		}
		cartActionViewController.setCartAction(this);
		return cartActionViewController;
	}
}
