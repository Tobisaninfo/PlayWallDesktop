package de.tobias.playpad.viewcontroller.option.pad.trigger;

import java.util.Set;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.tigger.TriggerItemConnect;
import de.tobias.playpad.tigger.TriggerRegistry;
import de.tobias.playpad.trigger.TriggerUIWrapper;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TriggerPointViewController extends ContentViewController {

	@FXML private VBox itemView;
	@FXML private HBox buttonBox;

	private TriggerUIWrapper triggerWrapper;

	public TriggerPointViewController(TriggerUIWrapper triggerWrapper) {
		super("triggerPoint", "de/tobias/playpad/assets/view/option/pad/trigger/", PlayPadMain.getUiResourceBundle());
		this.triggerWrapper = triggerWrapper;

		for (TriggerItem item : triggerWrapper.getTrigger().getItems())
			showTriggerItem(item);
	}

	@Override
	public void init() {
		Set<String> types = TriggerRegistry.getTypes();
		types.stream().sorted().forEach(item ->
		{
			Button button = new Button(TriggerRegistry.getTriggerConnect(item).toString(), new FontIcon(FontAwesomeType.PLUS_CIRCLE));
			button.setContentDisplay(ContentDisplay.TOP);
			button.setPrefWidth(150);

			button.setOnAction(e ->
			{
				TriggerItemConnect connect = TriggerRegistry.getTriggerConnect(item);
				TriggerItem triggerItem = connect.newInstance(triggerWrapper.getTrigger());

				triggerWrapper.addItem(triggerItem);
				showTriggerItem(triggerItem);
			});
			buttonBox.getChildren().add(button);
		});
	}

	private void showTriggerItem(TriggerItem item) {
		TriggerItemConnect connect = TriggerRegistry.getTriggerConnect(item.getType());

		VBox itemBox = new VBox(14);
		ContentViewController contentViewController = connect.getSettingsController(item);
		if (contentViewController != null) {
			itemBox.getChildren().add(contentViewController.getParent());

			ContentViewController timeViewController = new TriggerTimeViewController(item);
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

//			HBox.setHgrow(itemBox, Priority.ALWAYS);
		}
	}
}
