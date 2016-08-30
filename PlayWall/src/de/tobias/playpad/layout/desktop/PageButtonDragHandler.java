package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;

public class PageButtonDragHandler implements EventHandler<DragEvent> {

	private IMainViewController controller;
	private int page;

	public PageButtonDragHandler(IMainViewController controller, int page) {
		this.controller = controller;
		this.page = page;
	}

	public void handle(DragEvent event) {
		if (event.getEventType() == DragEvent.DRAG_OVER) {
			controller.showPage(page);
		}
	}

}
