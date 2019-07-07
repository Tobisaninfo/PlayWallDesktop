package de.tobias.playpad.viewcontroller.option.pad.trigger;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.tigger.TriggerItemFactory;
import de.tobias.playpad.trigger.TriggerDisplayable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Set;

public class TriggerPointViewController extends NVC {

	@FXML
	private VBox itemView;
	@FXML
	private HBox buttonBox;

	private TriggerDisplayable triggerWrapper;

	public TriggerPointViewController(TriggerDisplayable triggerWrapper) {
		load("view/option/pad/trigger", "TriggerPoint", PlayPadMain.getUiResourceBundle());
		this.triggerWrapper = triggerWrapper;

		for (TriggerItem item : triggerWrapper.getTrigger().getItems())
			showTriggerItem(item);
	}

	@Override
	public void init() {
		Set<String> types = PlayPadPlugin.getRegistries().getTriggerItems().getTypes();
		types.stream().sorted().forEach(item ->
		{
			try {
				TriggerItemFactory conntect = PlayPadPlugin.getRegistries().getTriggerItems().getFactory(item);
				Button button = new Button(conntect.toString(), new FontIcon(FontAwesomeType.PLUS_CIRCLE));
				button.setContentDisplay(ContentDisplay.TOP);
				button.setPrefWidth(150);

				button.setOnAction(e ->
				{
					TriggerItem triggerItem = conntect.newInstance(triggerWrapper.getTrigger());

					triggerWrapper.addItem(triggerItem);
					showTriggerItem(triggerItem);
				});
				buttonBox.getChildren().add(button);
			} catch (NoSuchComponentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	private void showTriggerItem(TriggerItem item) {
		try {
			TriggerItemFactory connect = PlayPadPlugin.getRegistries().getTriggerItems().getFactory(item.getType());

			VBox itemBox = new VBox(14);
			NVC controller = connect.getSettingsController(item);
			if (controller != null) {
				itemBox.getChildren().add(controller.getParent());

				NVC timeViewController = new TriggerTimeViewController(item);
				itemBox.getChildren().add(timeViewController.getParent());

				Button deleteButton = new Button("", new FontIcon(FontAwesomeType.TRASH));
				HBox hbox = new HBox(itemBox, deleteButton);
				hbox.setSpacing(14);

				VBox rootBox = new VBox(14.0, hbox, new Separator());

				itemView.getChildren().addAll(rootBox);

				deleteButton.setOnAction((e) ->
				{
					triggerWrapper.removeItem(item);
					itemView.getChildren().removeAll(rootBox);
				});
			}
		} catch (NoSuchComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
