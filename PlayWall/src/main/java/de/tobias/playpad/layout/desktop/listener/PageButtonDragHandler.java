package de.tobias.playpad.layout.desktop.listener;

import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;

/**
 * Drag and Drop eines Pads auf ein Page Button (umschalten auf neue Page), wird im DesktopMenuToolbarController verwaltet.
 *
 * @author tobias
 * @since 5.1.0
 */
public class PageButtonDragHandler implements EventHandler<DragEvent> {

	private IMainViewController controller;
	private int page;

	public PageButtonDragHandler(IMainViewController controller, int page) {
		this.controller = controller;
		this.page = page;
	}

	@Override
	public void handle(DragEvent event) {
		if (event.getEventType() == DragEvent.DRAG_OVER) {
			controller.showPage(page);
		}
	}

}
