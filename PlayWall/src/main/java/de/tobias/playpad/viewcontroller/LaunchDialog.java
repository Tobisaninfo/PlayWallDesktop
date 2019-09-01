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
	private Label infoLabel;
	@FXML
	private ImageView imageView;

	@FXML
	private ListView<ProjectReference> projectListView;

	@FXML
	private Button newProjectButton;
	@FXML
	private Button importProjectButton;
	@FXML
	private Button convertProjectButton;

	@FXML
	private Button openButton;
	@FXML
	private Button deleteButton;

	@FXML
	private Label cloudLabel;
	private FontIcon cloudIcon;

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
		infoLabel.setText(getString(Strings.UI_DIALOG_LAUNCH_INFO, app.getInfo().getName(), app.getInfo().getVersion()));
		imageView.setImage(new Image(IMAGE));

		openButton.setDisable(true);
		deleteButton.setDisable(true);

		// Load project to list
		projectListView.setPlaceholder(new Label(getString(Strings.UI_PLACEHOLDER_PROJECT)));
		projectListView.setId("list");
		projectListView.setCellFactory(list -> new ProjectCell(true));

		// List selection listener
		projectListView.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
			openButton.setDisable(c == null);
			deleteButton.setDisable(c == null);
		});

		// Mouse Double Click on list
		projectListView.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY) &&
					mouseEvent.getClickCount() == 2 &&
					!projectListView.getSelectionModel().isEmpty()) {
				launchProject(getSelectedProject());
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

		stage.setTitle(getString(Strings.UI_DIALOG_LAUNCH_TITLE));
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
				cloudLabel.setText(Localization.getString(Strings.SERVER_CONNECTED));
				break;
			case CONNECTION_LOST:
				cloudIcon.setColor(Color.GRAY);
				cloudLabel.setText(Localization.getString(Strings.SERVER_CONNECTION_LOST));
				break;
			case DISCONNECTED:
				cloudIcon.setColor(Color.RED);
				cloudLabel.setText(Localization.getString(Strings.SERVER_DISCONNECTED));
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

		String extensionName = Localization.getString(Strings.FILE_FILTER_ZIP);
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
	void openButtonHandler(ActionEvent event) {
		launchProject(getSelectedProject());
	}

	@FXML
	void deleteButtonHandler(ActionEvent event) {
		if (getSelectedProject() != null) {
			ProjectReference ref = getSelectedProject();

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setContentText(getString(Strings.UI_DIALOG_PROJECT_MANAGER_DELETE_CONTENT, ref));

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
					showErrorMessage(getString(Strings.ERROR_PROJECT_DELETE, e.getLocalizedMessage()));
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
			} catch (SessionNotExistsException ignored) {
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
			showInfoMessage(Localization.getString(Strings.ERROR_PLUGINS_MISSING));

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
			showErrorMessage(getString(Strings.ERROR_PROFILE_NOT_FOUND, ref.getProfileReference(), e.getLocalizedMessage()));

			// Choose new profile
			ProfileReference profile = null;
			try {
				profile = delegate.getProfileReference();
			} catch (ProjectReader.ProjectReaderDelegate.ProfileAbortException ignored) {
			}
			ref.setProfileReference(profile);
		} catch (ProjectNotFoundException e) {
			Logger.error(e);
			showErrorMessage(getString(Strings.ERROR_PROJECT_NOT_FOUND, ref, e.getLocalizedMessage()));
		} catch (Exception e) {
			Logger.error(e);
			showErrorMessage(getString(Strings.ERROR_PROJECT_OPEN, ref, e.getLocalizedMessage()));
		}
	}
}
