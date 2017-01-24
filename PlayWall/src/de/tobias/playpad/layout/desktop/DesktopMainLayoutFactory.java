package de.tobias.playpad.layout.desktop;

import java.util.Stack;

import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.desktop.pad.DesktopPadView;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.view.main.MainLayoutFactory;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import de.tobias.utils.ui.icon.FontIconType;
import de.tobias.utils.util.Localization;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Desktop Implmentierung des Main Layouts.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public class DesktopMainLayoutFactory extends MainLayoutFactory {

	private DesktopMenuToolbarViewController desktopMenuToolbarViewController;
	private ObjectProperty<DesktopEditMode> editMode = new SimpleObjectProperty<>(DesktopEditMode.PLAY);

	private Stack<IPadView> recyclingStack;

	public DesktopMainLayoutFactory(String type) {
		super(type);
		recyclingStack = new Stack<>();
	}

	@Override
	public MenuToolbarViewController createMenuToolbar(IMainViewController mainViewRef) {
		if (desktopMenuToolbarViewController == null) {
			desktopMenuToolbarViewController = new DesktopMenuToolbarViewController(mainViewRef, this);
		}
		return desktopMenuToolbarViewController;
	}

	@Override
	public IPadView createPadView() {
		if (!recyclingStack.isEmpty()) {
			return recyclingStack.pop();
		}
		return new DesktopPadView(this);
	}

	@Override
	public void recyclePadView(IPadView padView) {
		recyclingStack.push(padView);
	}

	@Override
	public String getStylesheet() {
		return null;
	}


	// Current Mode
	public DesktopEditMode getEditMode() {
		return editMode.get();
	}

	void setEditMode(DesktopEditMode editMode) {
		if (editMode != DesktopEditMode.PLAY && Profile.currentProfile().getProfileSettings().isLocked()) {
			return;
		}
		this.editMode.set(editMode);
	}

	ObjectProperty<DesktopEditMode> editModeProperty() {
		return editMode;
	}
}
