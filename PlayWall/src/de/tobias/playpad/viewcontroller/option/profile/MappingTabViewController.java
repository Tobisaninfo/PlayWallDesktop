package de.tobias.playpad.viewcontroller.option.profile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.ActionConnect;
import de.tobias.playpad.action.ActionDisplayable;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.mapper.MapperRegistry;
import de.tobias.playpad.project.v2.ProjectV2;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.IMapperOverviewViewController;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.playpad.viewcontroller.cell.DisplayableCell;
import de.tobias.playpad.viewcontroller.cell.DisplayableTreeCell;
import de.tobias.playpad.viewcontroller.dialog.MappingListViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

public class MappingTabViewController extends ProfileSettingsTabViewController implements IMappingTabViewController {

	@FXML private ComboBox<Mapping> mappingComboBox;
	@FXML private Button editMappingsButton;

	@FXML private TreeView<ActionDisplayable> treeView;

	@FXML private VBox detailView;
	private IMapperOverviewViewController mapperOverviewViewController;

	private Mapping oldMapping;
	private Mapping mapping;

	public MappingTabViewController() {
		super("mapping", "de/tobias/playpad/assets/view/option/profile/", PlayPadMain.getUiResourceBundle());
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
		Set<String> types = PlayPadPlugin.getRegistryCollection().getActions().getTypes();
		List<String> sortedTypes = types.stream().sorted().collect(Collectors.toList());

		// Sort the tpyes for the treeview
		for (ActionType actionType : ActionType.values()) {
			createTreeViewForActionType(mapping, rootItem, sortedTypes, actionType);
		}

		return rootItem;
	}

	private void createTreeViewForActionType(Mapping mapping, TreeItem<ActionDisplayable> rootItem, List<String> sortedTypes, ActionType type) {
		for (String id : sortedTypes) {
			List<Action> actions = mapping.getActionsOfType(id);
			try {
				ActionConnect actionConnect = PlayPadPlugin.getRegistryCollection().getActions().getComponent(id);
				if (actionConnect.geActionType() == type) {
					TreeItem<ActionDisplayable> item = actionConnect.getTreeViewForActions(actions, mapping);
					rootItem.getChildren().add(item);
				}
			} catch (NoSuchComponentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Tab Utils
	@Override
	public void loadSettings(Profile profile) {
		oldMapping = profile.getMappings().getActiveMapping();
		setMappingItemsToList();
		createTreeViewContent();
	}

	@Override
	public void saveSettings(Profile profile) {
	}

	@Override
	public boolean needReload() {
		return true;
	}

	@Override
	public void reload(Profile profile, ProjectV2 project, IMainViewController controller) {
		Profile.currentProfile().getMappings().getActiveMapping().adjustPadColorToMapper();

		Mapping activeMapping = Profile.currentProfile().getMappings().getActiveMapping();

		oldMapping.clearFeedback();
		activeMapping.showFeedback(project);
		activeMapping.initFeedback();
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
