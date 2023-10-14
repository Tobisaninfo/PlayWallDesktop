package de.tobias.playpad.viewcontroller.option.pad.trigger;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.tigger.TriggerItemFactory;
import de.tobias.playpad.trigger.TriggerDisplayable;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
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

	private final IMainViewController mainViewController;
	private final TriggerDisplayable triggerWrapper;

	public TriggerPointViewController(IMainViewController mainViewController, TriggerDisplayable triggerWrapper) {
		this.mainViewController = mainViewController;
		load("view/option/pad/trigger", "TriggerPoint", Localization.getBundle());
		this.triggerWrapper = triggerWrapper;

		for (TriggerItem item : triggerWrapper.getTrigger().getItems()) {
			showTriggerItem(item, mainViewController);
		}
	}

	@Override
	public void init() {
		Set<String> types = PlayPadPlugin.getRegistries().getTriggerItems().getTypes();
		types.stream().sorted().forEach(item ->
		{
			try {
				TriggerItemFactory factory = PlayPadPlugin.getRegistries().getTriggerItems().getFactory(item);
				Button button = new Button(factory.toString(), new FontIcon(FontAwesomeType.PLUS_CIRCLE));
				button.setContentDisplay(ContentDisplay.TOP);
				button.setPrefWidth(150);

				button.setOnAction(e ->
				{
					TriggerItem triggerItem = factory.newInstance(triggerWrapper.getTrigger());

					triggerWrapper.addItem(triggerItem);
					showTriggerItem(triggerItem, mainViewController);
				});
				buttonBox.getChildren().add(button);
			} catch (NoSuchComponentException e) {
				Logger.error(e);
			}
		});
	}

	private void showTriggerItem(TriggerItem item, IMainViewController mainViewController) {
		try {
			TriggerItemFactory connect = PlayPadPlugin.getRegistries().getTriggerItems().getFactory(item.getType());

			final VBox itemBox = new VBox(14);
			final NVC controller = connect.getSettingsController(item, mainViewController);
			if (controller != null) {
				itemBox.getChildren().add(controller.getParent());

				if (triggerWrapper.getTrigger().getTriggerPoint().isTimeAppendable()) {
					final NVC timeViewController = new TriggerTimeViewController(item);
					itemBox.getChildren().add(timeViewController.getParent());
				}

				final Button deleteButton = new Button("", new FontIcon(FontAwesomeType.TRASH));
				final HBox hbox = new HBox(itemBox, deleteButton);
				hbox.setSpacing(14);

				final VBox rootBox = new VBox(14.0, hbox, new Separator());

				itemView.getChildren().addAll(rootBox);

				deleteButton.setOnAction(event ->
				{
					triggerWrapper.removeItem(item);
					itemView.getChildren().removeAll(rootBox);
				});
			}
		} catch (NoSuchComponentException e) {
			Logger.error(e);
		}
	}
}
