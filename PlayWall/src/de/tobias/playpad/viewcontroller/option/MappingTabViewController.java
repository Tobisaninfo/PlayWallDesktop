package de.tobias.playpad.viewcontroller.option;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.ActionConnect;
import de.tobias.playpad.action.ActionDisplayable;
import de.tobias.playpad.action.ActionRegistery;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.mapper.MapperRegistry;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.IMapperOverviewViewController;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.playpad.viewcontroller.SettingsTabViewController;
import de.tobias.playpad.viewcontroller.cell.DisplayableCell;
import de.tobias.playpad.viewcontroller.cell.DisplayableTreeCell;
import de.tobias.playpad.viewcontroller.dialog.MappingListViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

public class MappingTabViewController extends SettingsTabViewController implements IMappingTabViewController {

	@FXML private ComboBox<Mapping> mappingComboBox;
	@FXML private Button editMappingsButton;

	@FXML private TreeView<ActionDisplayable> treeView;

	@FXML private VBox detailView;
	private IMapperOverviewViewController mapperOverviewViewController;

	private Mapping mapping;

	public MappingTabViewController() {
		super("mapping", "de/tobias/playpad/assets/view/option/", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public void init() {
		mappingComboBox.setCellFactory(list -> new DisplayableCell<>());
		mappingComboBox.setButtonCell(new DisplayableCell<>());

		mappingComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			mapping = c;
			Profile.currentProfile().getMappings().setActiveMapping(c);
			createTreeViewContent();
		});

		treeView.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			detailView.getChildren().clear();

			if (c != null) {
				ContentViewController controller = c.getValue().getSettingsViewController();
				if (controller == null) {
					controller = c.getValue().getActionSettingsViewController(mapping, this);
				}
				if (controller != null) {
					detailView.getChildren().add(controller.getParent());
				}
				if (c.getValue() instanceof Action) {
					showMapperFor((Action) c.getValue());
				}
			}
		});
		treeView.setCellFactory(list -> new DisplayableTreeCell<>());
	}

	private TreeItem<ActionDisplayable> createTreeView(Mapping mapping) {
		TreeItem<ActionDisplayable> rootItem = new TreeItem<>();
		Set<String> types = ActionRegistery.getTypes();
		List<String> sortedTypes = types.stream().sorted().collect(Collectors.toList());

		// Sort the tpyes for the treeview
		for (ActionType actionType : ActionType.values()) {
			createTreeViewForActionType(mapping, rootItem, sortedTypes, actionType);
		}

		return rootItem;
	}

	private void createTreeViewForActionType(Mapping mapping, TreeItem<ActionDisplayable> rootItem, List<String> sortedTypes, ActionType type) {
		for (String tpye : sortedTypes) {
			List<Action> actions = mapping.getActionsOfType(tpye);
			ActionConnect actionConnect = ActionRegistery.getActionConnect(tpye);
			if (actionConnect.geActionType() == type) {
				TreeItem<ActionDisplayable> item = actionConnect.getTreeViewForActions(actions, mapping);
				rootItem.getChildren().add(item);
			}
		}
	}

	@FXML
	private void editMappingsHandler(ActionEvent event) {
		MappingListViewController controller = new MappingListViewController(Profile.currentProfile().getMappings(), getWindow());
		controller.getStage().showAndWait();
		setMappingItemsToList();
	}

	private void createTreeViewContent() {
		TreeItem<ActionDisplayable> rootItem = createTreeView(mapping);
		treeView.setRoot(rootItem);
	}

	private void setMappingItemsToList() {
		mappingComboBox.getItems().setAll(Profile.currentProfile().getMappings());
		mappingComboBox.setValue(Profile.currentProfile().getMappings().getActiveMapping());

		mapping = mappingComboBox.getValue();
	}

	@Override
	public void showMapperFor(Action action) {
		try {
			if (action != null) {
				mapperOverviewViewController = MapperRegistry.getOverviewViewControllerInstance();
				mapperOverviewViewController.showAction(action, detailView);
			} else {
				detailView.getChildren().remove(mapperOverviewViewController.getParent());
			}
		} catch (Exception e) {}
	}

	// Tab Utils
	@Override
	public void loadSettings(Profile profile) {
		setMappingItemsToList();
		createTreeViewContent();
	}

	@Override
	public void saveSettings(Profile profile) {}

	@Override
	public boolean needReload() {
		return true;
	}

	@Override
	public void reload(Profile profile, Project project, IMainViewController controller) {
		Profile.currentProfile().getMappings().getActiveMapping().clearFeedback();
		Profile.currentProfile().getMappings().getActiveMapping().showFeedback(project, controller);
		Profile.currentProfile().getMappings().getActiveMapping().initFeedback();
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Mapping_Title);
	}
}
