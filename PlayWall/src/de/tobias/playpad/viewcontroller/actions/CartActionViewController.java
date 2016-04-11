package de.tobias.playpad.viewcontroller.actions;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.cartaction.CartAction;
import de.tobias.playpad.action.cartaction.CartAction.ControlMode;
import de.tobias.playpad.action.mapper.MapperRegistry;
import de.tobias.playpad.action.mapper.MapperViewController;
import de.tobias.playpad.viewcontroller.IMapperOverviewViewController;
import de.tobias.playpad.viewcontroller.cell.EnumCell;
import de.tobias.utils.ui.ContentViewController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CartActionViewController extends ContentViewController {

	@FXML private ComboBox<ControlMode> controlMode;
	@FXML private CheckBox autoColorCheckbox;

	@FXML private VBox rootContainer;
	private IMapperOverviewViewController mapperOverviewViewController;

	private CartAction action;

	public CartActionViewController() {
		super("cartAction", "de/tobias/playpad/assets/view/actions/", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public void init() {
		controlMode.getItems().setAll(ControlMode.values());
		controlMode.setCellFactory(list -> new EnumCell<>(Strings.CartAction_Mode_BaseName));
		controlMode.setButtonCell(new EnumCell<>(Strings.CartAction_Mode_BaseName));
		controlMode.valueProperty().addListener((a, b, c) ->
		{
			action.setMode(c);
		});

		autoColorCheckbox.selectedProperty().addListener((a, b, c) ->
		{
			action.setAutoFeedbackColors(c);
			// Disable Feedback Controls bei Automatischen Feedback für VORHANDENE MAPPER
			if (mapperOverviewViewController != null) {
				mapperOverviewViewController.getControllers().forEach(controller ->
				{
					toggleFeedbackVisablity(action, controller);
				});
			}
		});
		VBox.setVgrow(rootContainer, Priority.ALWAYS);

		mapperOverviewViewController = MapperRegistry.getOverviewViewControllerInstance();
		mapperOverviewViewController.addMapperAddListener((mapper, controller) ->
		{
			// Disable Feedback Controls bei Automatischen Feedback für NEUE MAPPER die erstellt werden
			toggleFeedbackVisablity(action, controller);
		});
	}

	public void setCartAction(CartAction action) {
		this.action = action;

		controlMode.setValue(action.getMode());
		autoColorCheckbox.setSelected(action.isAutoFeedbackColors());
	}

	private void toggleFeedbackVisablity(CartAction action, MapperViewController controller) {
		if (action.isAutoFeedbackColors()) {
			controller.hideFeedback();
		} else {
			controller.showFeedback();
		}
	}
}
