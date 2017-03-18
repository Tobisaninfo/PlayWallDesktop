package de.tobias.playpad.viewcontroller.option.profile;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.ActionFactory;
import de.tobias.playpad.action.ActionDisplayable;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.BaseMapperOverviewViewController;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.playpad.viewcontroller.cell.DisplayableCell;
import de.tobias.playpad.viewcontroller.cell.DisplayableTreeCell;
import de.tobias.playpad.viewcontroller.dialog.MappingListViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileReloadTask;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.util.Localization;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

public class MappingTabViewController extends ProfileSettingsTabViewController implements IMappingTabViewController, IProfileReloadTask {

	@FXML private ComboBox<Mapping> mappingComboBox;
	@FXML private Button editMappingsButton;

	@FXML private TreeView<ActionDisplayable> treeView;

	@FXML private VBox detailView;
	private BaseMapperOverviewViewController baseMapperOverviewViewController;

	private Mapping oldMapping;
	private Mapping mapping;

	MappingTabViewController() {
		load("de/tobias/playpad/assets/view/option/profile/", "mapping", PlayPadMain.getUiResourceBundle());
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
				NVC controller = c.getValue().getSettingsViewController();
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
		Collection<ActionFactory> types = PlayPadPlugin.getRegistryCollection().getActions().getComponents();
		List<ActionFactory> sortedTypes = types.stream().sorted((a, b) -> a.getType().compareTo(b.getType())).collect(Collectors.toList());

		// Sort the tpyes for the treeview
		for (ActionType actionType : ActionType.values()) {
			createTreeViewForActionType(mapping, rootItem, sortedTypes, actionType);
		}

		return rootItem;
	}

	private void createTreeViewForActionType(Mapping mapping, TreeItem<ActionDisplayable> rootItem, List<ActionFactory> sortedTypes, ActionType type) {
		for (ActionFactory actionFactory : sortedTypes) {
			List<Action> actions = mapping.getActionsOfType(actionFactory);
			if (actionFactory.geActionType() == type) {
				TreeItem<ActionDisplayable> item = actionFactory.getTreeViewForActions(actions, mapping);
				rootItem.getChildren().add(item);
			}
		}
	}

	@FXML
	private void editMappingsHandler(ActionEvent event) {
		MappingListViewController controller = new MappingListViewController(Profile.currentProfile().getMappings(), getContainingWindow());
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
				baseMapperOverviewViewController = BaseMapperOverviewViewController.getInstance();
				baseMapperOverviewViewController.showAction(action, detailView);
			} else {
				detailView.getChildren().remove(baseMapperOverviewViewController.getParent());
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
	public Task<Void> getTask(ProfileSettings settings, Project project, IMainViewController controller) {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateTitle(name());
				updateProgress(-1, -1);

				Profile.currentProfile().getMappings().getActiveMapping().adjustPadColorToMapper();

				Mapping activeMapping = Profile.currentProfile().getMappings().getActiveMapping();

				oldMapping.clearFeedback();
				activeMapping.showFeedback(project);
				activeMapping.initFeedback();
				return null;
			}
		};
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
