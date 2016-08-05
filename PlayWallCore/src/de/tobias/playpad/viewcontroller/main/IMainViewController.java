package de.tobias.playpad.viewcontroller.main;

import java.util.List;

import de.tobias.playpad.midi.MidiListener;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.view.main.MainLayoutHandler;
import de.tobias.utils.ui.NotificationHandler;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

public interface IMainViewController extends NotificationHandler {

	public void setGridColor(Color color);

	public Stage getStage();

	public int getPage();

	public Parent getParent();

	public void registerKeyboardListener(EventType<KeyEvent> eventType, EventHandler<KeyEvent> listener);

	public void createPadViews();

	/**
	 * Zeigt eine Seite. Sollte die Seite bereits offen sien, passiert nichts.
	 * 
	 * @param page
	 *            Page Number
	 */
	public void showPage(int page);

	public void loadUserCss();

	public void applyColorsToMappers();

	public void showLiveInfo();

	public void setTitle();

	List<IPadViewV2> getPadViews();

	public Screen getScreen();

	public MidiListener getMidiHandler();

	public MenuToolbarViewController getMenuToolbarController();

	public void setPadVolume(double doubleValue);

	public void setMainLayout(MainLayoutConnect mainLayoutConnect);

	public void performLayoutDependendAction(MainLayoutHandler runnable);
}
