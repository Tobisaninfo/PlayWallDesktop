package de.tobias.playpad.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * Hilfe Menü.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public class HelpMenuItem extends CustomMenuItem implements ChangeListener<String> {

	private Menu helpMenu;

	public HelpMenuItem(Menu parentMenu) {
		this.helpMenu = parentMenu;

		setHideOnClick(false);

		Label label = new Label("Suchen nach (Beta): "); // TODO BETA i18n
		TextField textfield = new TextField();
		textfield.textProperty().addListener(this);

		HBox hbox = new HBox(14, label, textfield);
		hbox.setAlignment(Pos.CENTER_LEFT);
		setContent(hbox);
	}

	private void clearMenu() {
		helpMenu.getItems().removeIf(i -> i != this);
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		clearMenu();

		for (char c : newValue.toCharArray()) {
			MenuItem item = new MenuItem(String.valueOf(c));
			helpMenu.getItems().add(item);
		}
	}
}
