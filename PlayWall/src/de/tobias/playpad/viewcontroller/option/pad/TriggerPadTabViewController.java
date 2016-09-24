package de.tobias.playpad.viewcontroller.option.pad;

import java.util.HashMap;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import de.tobias.playpad.trigger.TriggerUIWrapper;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.cell.DisplayableTreeCell;
import de.tobias.playpad.viewcontroller.option.pad.trigger.TriggerPointViewController;
import de.tobias.utils.util.Localization;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TriggerPadTabViewController extends PadSettingsTabViewController implements ChangeListener<TreeItem<TriggerUIWrapper>> {

	@FXML private TreeView<TriggerUIWrapper> treeView;
	@FXML private VBox contentView;

	private Pad pad;

	public TriggerPadTabViewController(Pad pad) {
		super("triggerTab", "de/tobias/playpad/assets/view/option/pad/", PlayPadMain.getUiResourceBundle());
		this.pad = pad;
	}

	@Override
	public void init() {
		treeView.setCellFactory(list -> new DisplayableTreeCell<>());
		treeView.getSelectionModel().selectedItemProperty().addListener(this);
	}

	private void createTreeView() {
		HashMap<TriggerPoint, Trigger> triggers = pad.getPadSettings().getTriggers();
		TreeItem<TriggerUIWrapper> rootItem = new TreeItem<>();

		// Sort the tpyes for the treeview
		for (TriggerPoint point : TriggerPoint.values()) {
			Trigger trigger = triggers.get(point);

			TreeItem<TriggerUIWrapper> triggerItem = new TreeItem<>(new TriggerUIWrapper(trigger));
			rootItem.getChildren().add(triggerItem);
		}

		treeView.setRoot(rootItem);
	}

	@Override
	public void changed(ObservableValue<? extends TreeItem<TriggerUIWrapper>> observable, TreeItem<TriggerUIWrapper> oldValue,
			TreeItem<TriggerUIWrapper> newValue) {
		contentView.getChildren().clear();

		if (newValue != null) {
			TriggerUIWrapper triggerWrapper = newValue.getValue();
			TriggerPointViewController controller = new TriggerPointViewController(triggerWrapper);
			contentView.getChildren().setAll(controller.getParent());
			VBox.setVgrow(controller.getParent(), Priority.ALWAYS);
		}
	}

	@Override
	public String getName() {
		return Localization.getString(Strings.UI_Window_PadSettings_Trigger_Title);
	}

	@Override
	public void loadSettings(Pad pad) {
		createTreeView();
	}

	@Override
	public void saveSettings(Pad pad) {}
}
