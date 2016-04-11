package de.tobias.playpad.viewcontroller.option.pad;

import java.util.HashMap;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import de.tobias.playpad.trigger.TriggerWrapper;
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

public class TriggerPadTabViewController extends PadSettingsTabViewController implements ChangeListener<TreeItem<TriggerWrapper>> {

	@FXML private TreeView<TriggerWrapper> treeView;
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
		HashMap<TriggerPoint, Trigger> triggers = pad.getTriggers();
		TreeItem<TriggerWrapper> rootItem = new TreeItem<>();

		// Sort the tpyes for the treeview
		for (TriggerPoint point : TriggerPoint.values()) {
			Trigger trigger = triggers.get(point);

			TreeItem<TriggerWrapper> triggerItem = new TreeItem<>(new TriggerWrapper(trigger));
			rootItem.getChildren().add(triggerItem);
		}

		treeView.setRoot(rootItem);
	}

	public void changed(ObservableValue<? extends TreeItem<TriggerWrapper>> observable, TreeItem<TriggerWrapper> oldValue,
			TreeItem<TriggerWrapper> newValue) {
		contentView.getChildren().clear();

		if (newValue != null) {
			TriggerWrapper triggerWrapper = newValue.getValue();
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
