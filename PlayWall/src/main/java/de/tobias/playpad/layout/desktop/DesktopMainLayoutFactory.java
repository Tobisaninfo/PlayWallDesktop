package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.layout.desktop.pad.DesktopPadView;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.view.main.MainLayoutFactory;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Desktop Implmentierung des Main Layouts.
 *
 * @author tobias
 * @since 5.1.0
 */
public class DesktopMainLayoutFactory extends MainLayoutFactory {

	private DesktopMenuToolbarViewController desktopMenuToolbarViewController;
	private final ObjectProperty<DesktopEditMode> editMode = new SimpleObjectProperty<>(DesktopEditMode.PLAY);

	private final Deque<IPadView> recyclingStack;

	public DesktopMainLayoutFactory(String type) {
		super(type);
		recyclingStack = new ArrayDeque<>();
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
