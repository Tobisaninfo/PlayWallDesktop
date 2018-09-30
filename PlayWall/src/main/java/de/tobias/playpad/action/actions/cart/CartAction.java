package de.tobias.playpad.action.actions.cart;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.actions.cart.handler.CartActionHandler;
import de.tobias.playpad.action.actions.cart.handler.CartActionHandlerFactory;
import de.tobias.playpad.action.feedback.ColorAdjustable;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.actions.CartActionViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.ui.NVC;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.dom4j.Element;

public class CartAction extends Action implements ColorAdjustable {

	public enum CartActionMode {
		PLAY_PAUSE,
		PLAY_STOP,
		PLAY_HOLD,
		PLAY_PLAY
	}

	private final String type;

	private int x;
	private int y;

	private CartActionMode mode;
	private CartActionHandler handler;
	private boolean autoFeedbackColors;

	private transient Pad pad;
	private transient PadContentFeedbackListener padContentFeedbackListener = new PadContentFeedbackListener();
	private transient PadStatusFeedbackListener padStatusFeedbackListener = new PadStatusFeedbackListener();
	private transient PadPositionWarningListener padPositionListener = new PadPositionWarningListener(this);

	public CartAction(String type) {
		this(type, 0, 0, CartActionMode.PLAY_STOP);
		this.autoFeedbackColors = true;
	}

	public CartAction(String type, int x, int y, CartActionMode mode) {
		this.type = type;
		this.x = x;
		this.y = y;

		setMode(mode);
		this.autoFeedbackColors = true;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public CartActionMode getMode() {
		return mode;
	}

	public void setMode(CartActionMode mode) {
		this.mode = mode;
		this.handler = CartActionHandlerFactory.getInstance(mode);
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

		if (pad.hasVisibleContent()) {
			handler.performAction(type, this, pad, project);
		}
	}

	@Override
	public FeedbackType geFeedbackType() {
		return FeedbackType.DOUBLE;
	}

	void setPad(Pad newPad) {
		Pad oldPad = this.pad;
		if (newPad == null || !newPad.equals(oldPad)) {
			removeOldListener(oldPad);
			this.pad = newPad;
			addNewListener(newPad);
		}
	}

	private void removeOldListener(Pad oldPad) {
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
	}

	private void addNewListener(Pad newPad) {
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

	public PadPositionWarningListener getPadPositionListener() {
		return padPositionListener;
	}

	// Helper
	@Override
	public Action cloneAction() throws CloneNotSupportedException {
		CartAction action = (CartAction) super.clone();

		action.autoFeedbackColors = autoFeedbackColors;
		action.x = x;
		action.y = y;
		action.setMode(mode);

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

	// Serialization
	private static final String X_ATTR = "x";
	private static final String Y_ATTR = "y";
	private static final String CONTROL_MODE = "mode";
	private static final String AUTO_FEEDBACK_COLORS = "autoColor";

	@Override
	public void load(Element root) {
		if (root.attributeValue(X_ATTR) != null)
			x = Integer.valueOf(root.attributeValue(X_ATTR));
		if (root.attributeValue(Y_ATTR) != null)
			y = Integer.valueOf(root.attributeValue(Y_ATTR));
		setMode(CartActionMode.valueOf(root.attributeValue(CONTROL_MODE)));
		autoFeedbackColors = Boolean.valueOf(root.attributeValue(AUTO_FEEDBACK_COLORS));
	}

	@Override
	public void save(Element root) {
		root.addAttribute(X_ATTR, String.valueOf(x));
		root.addAttribute(Y_ATTR, String.valueOf(y));
		root.addAttribute(CONTROL_MODE, mode.name());
		root.addAttribute(AUTO_FEEDBACK_COLORS, String.valueOf(autoFeedbackColors));
	}

}
