package de.tobias.playpad.viewcontroller;

import com.neovisionaries.ws.client.WebSocketException;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.playpad.project.importer.ConverterV6;
import de.tobias.playpad.project.loader.ProjectLoader;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.server.*;
import de.tobias.playpad.viewcontroller.cell.ProjectCell;
import de.tobias.playpad.viewcontroller.dialog.ModernPluginViewController;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectImportDialog;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectLoadDialog;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectNewDialog;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectReaderDelegateImpl;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static de.thecodelabs.utils.util.Localization.getString;

public class LaunchDialog extends NVC implements ChangeListener<ConnectionState> {

	static final String IMAGE = "gfx/Logo-large.png";

	@FXML
	Label infoLabel;
	@FXML
	ImageView imageView;

	@FXML
	ListView<ProjectReference> projectListView;

	@FXML
	Button newProjectButton;
	@FXML
	Button importProjectButton;
	@FXML
	Button convertProjectButton;

	@FXML
	Button openButton;
	@FXML
	Button deleteButton;

	@FXML
	Label cloudLabel;
	FontIcon cloudIcon;

	public LaunchDialog(Stage stage) {
		load("view/dialog", "LaunchDialog", Localization.getBundle());
		setProjectListValues();

		applyViewControllerToStage(stage);
	}

	private void setProjectListValues() {
		List<ProjectReference> projectsSorted = ProjectReferenceManager.getProjectsSorted();
		projectListView.getItems().setAll(projectsSorted);
	}

	@Override
	public void init() {
		App app = ApplicationUtils.getApplication();

		// Setup launchscreen labels and image
		infoLabel.setText(getString(Strings.UI_Dialog_Launch_Info, app.getInfo().getName(), app.getInfo().getVersion()));
		imageView.setImage(new Image(IMAGE));

		openButton.setDisable(true);
		deleteButton.setDisable(true);

		// Load project to list
		projectListView.setPlaceholder(new Label(getString(Strings.UI_Placeholder_Project)));
		projectListView.setId("list");
		projectListView.setCellFactory(list -> new ProjectCell(true));

		// List selection listener
		projectListView.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
			openButton.setDisable(c == null);
			deleteButton.setDisable(c == null);
		});

		// Mouse Double Click on list
		projectListView.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
				if (mouseEvent.getClickCount() == 2) {
					if (!projectListView.getSelectionModel().isEmpty()) {
						launchProject(getSelectedProject());
					}
				}
			}
		});

		// Cloud Info Label
		cloudIcon = new FontIcon(FontAwesomeType.CLOUD);
		cloudLabel.setGraphic(cloudIcon);
		setCloudState();

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.connectionStateProperty().addListener(this);
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadPlugin.styleable().applyStyle(stage);

		stage.setTitle(getString(Strings.UI_Dialog_Launch_Title));
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setResizable(false);
		stage.setWidth(650);
		stage.setHeight(400);
		stage.centerOnScreen();
		stage.show();
	}

	@Override
	public void changed(ObservableValue<? extends ConnectionState> observable, ConnectionState oldValue, ConnectionState newValue) {
		Platform.runLater(this::setCloudState);
	}

	private void setCloudState() {
		Server server = PlayPadPlugin.getServerHandler().getServer();
		switch (server.getConnectionState()) {
			case CONNECTED:
				cloudIcon.setColor(Color.BLACK);
				cloudLabel.setText(Localization.getString(Strings.Server_Connected));
				break;
			case CONNECTION_LOST:
				cloudIcon.setColor(Color.GRAY);
				cloudLabel.setText(Localization.getString(Strings.Server_Connection_Lost));
				break;
			case DISCONNECTED:
				cloudIcon.setColor(Color.RED);
				cloudLabel.setText(Localization.getString(Strings.Server_Disconnected));
				break;
		}
	}

	@FXML
	void newProjectButtonHandler(ActionEvent event) {
		ProjectNewDialog dialog = new ProjectNewDialog(getContainingWindow());
		dialog.showAndWait().ifPresent(this::launchProject);
	}

	@FXML
	void importProjectButtonHandler(ActionEvent event) {
		FileChooser chooser = new FileChooser();

		String extensionName = Localization.getString(Strings.File_Filter_ZIP);
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(extensionName, PlayPadMain.ZIP_TYPE);
		chooser.getExtensionFilters().add(extensionFilter);

		File file = chooser.showOpenDialog(getContainingWindow());

		if (file != null) {
			try {
				ProjectImportDialog dialog = new ProjectImportDialog(file.toPath(), getContainingWindow());
				Optional<ProjectReference> importedProject = dialog.showAndWait();
				importedProject.ifPresent(projectListView.getItems()::add);
			} catch (IOException | DocumentException e) {
				Logger.error(e);
			}
		}
	}

	@FXML
	void convertProjectButtonHandler(ActionEvent event) {
		try {
			List<ProjectReference> projects = ConverterV6.loadProjectReferences();
			ChoiceDialog<ProjectReference> dialog = new ChoiceDialog<>(null, projects);

			dialog.setHeaderText(Localization.getString(Strings.UI_Dialog_Project_Convert_Header));
			dialog.setContentText(Localization.getString(Strings.UI_Dialog_Project_Convert_Content));

			dialog.initOwner(getContainingWindow());
			dialog.initModality(Modality.WINDOW_MODAL);
			Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
			stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

			Optional<ProjectReference> result = dialog.showAndWait();
			result.ifPresent((ref) -> {
				try {
					ConverterV6.convert(ref.getUuid(), ref.getName());
					ProjectReferenceManager.addProjectReference(ref);
					setProjectListValues();
				} catch (IOException | DocumentException e) {
					e.printStackTrace();
					showErrorMessage(Localization.getString(Strings.Error_Project_Convert));
				}
			});
		} catch (IOException | DocumentException e) {
			e.printStackTrace();
			showErrorMessage(Localization.getString(Strings.Error_Standard_Gen));
		}
	}

	@FXML
	void openButtonHandler(ActionEvent event) {
		launchProject(getSelectedProject());
	}

	@FXML
	void deleteButtonHandler(ActionEvent event) {
		if (getSelectedProject() != null) {
			ProjectReference ref = getSelectedProject();

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setContentText(getString(Strings.UI_Dialog_ProjectManager_Delete_Content, ref));

			Stage dialog = (Stage) alert.getDialogPane().getScene().getWindow();
			dialog.getIcons().add(PlayPadPlugin.getInstance().getIcon());
			alert.initOwner(getContainingWindow());
			alert.initModality(Modality.WINDOW_MODAL);

			alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(item ->
			{
				try {
					ProjectReferenceManager.removeProject(ref);
					projectListView.getItems().remove(ref); // VIEW
				} catch (IOException e) {
					showErrorMessage(getString(Strings.Error_Project_Delete, e.getLocalizedMessage()));
				}
			});
		}
	}

	@FXML
	void cloudIconClicked(MouseEvent event) {
		Server server = PlayPadPlugin.getServerHandler().getServer();
		if (server.getConnectionState() == ConnectionState.DISCONNECTED) {
			SessionDelegate sessionDelegate = new LoginViewController();
			Session session = sessionDelegate.getSession();
			try {
				server.connect(session.getKey());
			} catch (IOException | WebSocketException e) {
				Logger.error(e);
			} catch (SessionNotExisitsException ignored) {
				Logger.warning("Session not exists");
			}
		}
	}

	/**
	 * Returns the selected project from the list view
	 *
	 * @return Project
	 */
	private ProjectReference getSelectedProject() {
		return projectListView.getSelectionModel().getSelectedItem();
	}

	/**
	 * Öffnet ein Project und zeigt es im MainView an. Zudem wird as entsprechende Profile geladen und geprüft ob Module (Plugins) fehlen.
	 *
	 * @param ref Project to launch
	 */
	private void launchProject(ProjectReference ref) {
		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.connectionStateProperty().removeListener(this);

		// Es fehlen Module
		if (!ref.getMissedModules().isEmpty()) {
			showInfoMessage(Localization.getString(Strings.Error_Plugins_Missing));

			ModernPluginViewController pluginViewController = new ModernPluginViewController(getContainingWindow(), ref.getMissedModules());
			pluginViewController.getStageContainer().ifPresent(NVCStage::showAndWait);
		}

		ProjectReader.ProjectReaderDelegate delegate = ProjectReaderDelegateImpl.getInstance(getContainingWindow());
		try {
			ProjectLoader loader = new ProjectLoader(ref);
			loader.setDelegate(delegate);
			loader.setListener(new ProjectLoadDialog());

			Project project = loader.load();
			PlayPadMain.getProgramInstance().openProject(project, e -> getStageContainer().ifPresent(NVCStage::close));
		} catch (ProfileNotFoundException e) {
			Logger.error(e);
			showErrorMessage(getString(Strings.Error_Profile_NotFound, ref.getProfileReference(), e.getLocalizedMessage()));

			// Choose new profile
			ProfileReference profile = null;
			try {
				profile = delegate.getProfileReference();
			} catch (ProjectReader.ProjectReaderDelegate.ProfileAbortException ignored) {
			}
			ref.setProfileReference(profile);
		} catch (ProjectReader.ProjectReaderDelegate.ProfileAbortException ignored) {
		} catch (ProjectNotFoundException e) {
			Logger.error(e);
			showErrorMessage(getString(Strings.Error_Project_NotFound, ref, e.getLocalizedMessage()));
		} catch (Exception e) {
			Logger.error(e);
			showErrorMessage(getString(Strings.Error_Project_Open, ref, e.getLocalizedMessage()));
		}
	}
}
