package de.tobias.playpad.viewcontroller.option;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.update.Updatable;
import de.tobias.playpad.update.UpdateChannel;
import de.tobias.playpad.update.UpdateRegistery;
import de.tobias.playpad.viewcontroller.SettingsTabViewController;
import de.tobias.playpad.viewcontroller.cell.UpdateCell;
import de.tobias.playpad.viewcontroller.dialog.UpdaterDialog;
import de.tobias.utils.application.ApplicationInfo;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.NativeLauncher;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.OS;
import de.tobias.utils.util.OS.OSType;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class UpdateTabViewController extends SettingsTabViewController {

	private static final String DOWNLOAD_FOLDER = "Updates";
	private static final String UPDATER_JAR = "Updater.jar";
	private static final String UPDATER_EXE = "Updater.exe";

	@FXML private Label currentVersionLabel;

	@FXML private CheckBox automaticSearchCheckBox;
	@FXML private Button manualSearchButton;

	@FXML private ListView<Updatable> openUpdateList;
	@FXML private Button updateButton;

	@FXML private ComboBox<UpdateChannel> updateChannelComboBox;

	// Placeholder for List
	private ProgressIndicator progressIndecator;
	private Label placeholderLabel;

	public UpdateTabViewController() {
		super("updateTab", "de/tobias/playpad/assets/view/option/", PlayPadMain.getUiResourceBundle());

		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		updateChannelComboBox.setValue(profileSettings.getUpdateChannel());
		openUpdateList.getItems().setAll(UpdateRegistery.getAvailableUpdates());
		updateButton.setDisable(openUpdateList.getItems().isEmpty());

		ApplicationInfo info = ApplicationUtils.getApplication().getInfo();
		String currentVersionString = Localization.getString(Strings.UI_Window_Settings_Updates_CurrentVersion, info.getVersion(),
				info.getBuild());
		currentVersionLabel.setText(currentVersionString);
	}

	@Override
	public void init() {
		openUpdateList.setCellFactory(list -> new UpdateCell());
		updateChannelComboBox.getItems().setAll(UpdateChannel.values());

		updateChannelComboBox.valueProperty().addListener((a, b, c) ->
		{
			Profile.currentProfile().getProfileSettings().setUpdateChannel(c);
		});

		progressIndecator = new ProgressIndicator(-1);
		progressIndecator.setMinSize(75, 75);
		progressIndecator.setMaxSize(75, 75);

		placeholderLabel = new Label(Localization.getString(Strings.UI_Placeholder_Updates));
		openUpdateList.setPlaceholder(placeholderLabel);

		updateButton.setDisable(openUpdateList.getItems().isEmpty());
	}

	@FXML
	private void manualSearchHandler(ActionEvent event) {
		openUpdateList.getItems().clear();

		Profile profile = Profile.currentProfile();
		if (profile != null) {
			openUpdateList.setPlaceholder(progressIndecator);

			Worker.runLater(() ->
			{
				// Search for updates
				try {
					UpdateRegistery.lookupUpdates(profile.getProfileSettings().getUpdateChannel());
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
					showErrorMessage(Localization.getString(Strings.Error_Update_Download, e.getLocalizedMessage()));
				}

				Platform.runLater(() ->
				{
					openUpdateList.setPlaceholder(placeholderLabel);
					openUpdateList.getItems().setAll(UpdateRegistery.getAvailableUpdates());
					updateButton.setDisable(openUpdateList.getItems().isEmpty());
				});
			});

		}
	}

	@FXML
	private void updateHandler(ActionEvent event) {
		update(getStage());
	}

	public static void update(Window dialogOwner) {
		String parameter = UpdateRegistery
				.buildParamaterString(ApplicationUtils.getApplication().getPath(PathType.DOWNLOAD, DOWNLOAD_FOLDER).toString());
		if (OS.getType() == OSType.Windows) {
			windowsUpdate(dialogOwner, parameter);
		} else {
			macUpdate(dialogOwner, parameter);
		}
	}

	/**
	 * Perform Mac Update and if needed download the jar updater.
	 * 
	 * @param dialogOwner
	 *            Owner window
	 * @param parameter
	 */
	private static void macUpdate(Window dialogOwner, String parameter) {
		try {
			Path fileJar = Paths.get(UPDATER_JAR);
			Path fileJarFolder = ApplicationUtils.getApplication().getPath(PathType.DOWNLOAD, UPDATER_JAR);

			if (Files.exists(fileJar)) {
				startJarFile(parameter, fileJar);
			} else if (Files.exists(fileJarFolder)) {
				startJarFile(parameter, fileJarFolder);
			} else {
				UpdaterDialog dialog = new UpdaterDialog(dialogOwner);
				dialog.show();

				Worker.runLater(() ->
				{
					String updaterURL = ApplicationUtils.getApplication().getInfo().getUserInfo().get(AppUserInfoStrings.UPDATER_PROGRAM)
							+ UPDATER_JAR;
					Path path = ApplicationUtils.getApplication().getPath(PathType.DOWNLOAD, UPDATER_JAR);
					try {
						downloadUpdater(updaterURL, path);
						startJarFile(parameter, path);
					} catch (Exception e) {
						e.printStackTrace();
						String errorMessage = Localization.getString(Strings.Error_Update_Download, e.getMessage());
						showErrorMessage(errorMessage, PlayPadPlugin.getImplementation().getIcon(), dialogOwner);
					}
				});
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void windowsUpdate(Window dialogOwner, String parameter) {
		try {
			Path fileJar = Paths.get(UPDATER_JAR);
			Path fileExe = Paths.get(UPDATER_EXE);
			Path fileJarFolder = ApplicationUtils.getApplication().getPath(PathType.DOWNLOAD, UPDATER_JAR);
			Path fileExeFolder = ApplicationUtils.getApplication().getPath(PathType.DOWNLOAD, UPDATER_EXE);

			if (Files.exists(fileJar)) {
				startJarFile(parameter, fileJar);
			} else if (Files.exists(fileExe)) {
				startExeFile(parameter, fileExe);

			} else if (Files.exists(fileJarFolder)) {
				startJarFile(parameter, fileJarFolder);
			} else if (Files.exists(fileExeFolder)) {
				startExeFile(parameter, fileExeFolder);
			} else {
				UpdaterDialog dialog = new UpdaterDialog(dialogOwner);
				dialog.show();

				Worker.runLater(() ->
				{
					ApplicationInfo info = ApplicationUtils.getApplication().getInfo();
					String updaterURL = info.getUserInfo().get(AppUserInfoStrings.UPDATER_PROGRAM) + UPDATER_EXE;
					Path path = ApplicationUtils.getApplication().getPath(PathType.DOWNLOAD, UPDATER_EXE);
					try {
						downloadUpdater(updaterURL, path);
						startExeFile(parameter, path);
					} catch (Exception e) {
						e.printStackTrace();
						String errorMessage = Localization.getString(Strings.Error_Update_Download, e.getMessage());
						showErrorMessage(errorMessage, PlayPadPlugin.getImplementation().getIcon(), dialogOwner);
					}
				});
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void downloadUpdater(String updaterURL, Path path) throws IOException, MalformedURLException {
		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}

		URLConnection conn = new URL(updaterURL).openConnection();

		InputStream iStr = conn.getInputStream();
		OutputStream oStr = Files.newOutputStream(path);

		byte[] data = new byte[1024];
		int dataLength = 0;
		while ((dataLength = iStr.read(data, 0, data.length)) > 0) {
			oStr.write(data, 0, dataLength);
		}
		oStr.close();
	}

	private static void startExeFile(String parameter, Path fileExeFolder) {
		NativeLauncher.executeAsAdministrator(fileExeFolder.toAbsolutePath().toString(), parameter);
		System.exit(0);
	}

	private static void startJarFile(String parameter, Path fileJarFolder) throws IOException {
		ProcessBuilder builder = new ProcessBuilder("java", "-jar", fileJarFolder.toAbsolutePath().toString(), parameter);
		builder.start();
		System.exit(0);
	}

	@Override
	public void loadSettings(Profile profile) {
		automaticSearchCheckBox.setSelected(profile.getProfileSettings().isAutoUpdate());
	}

	@Override
	public void saveSettings(Profile profile) {
		profile.getProfileSettings().setAutoUpdate(automaticSearchCheckBox.isSelected());
	}

	@Override
	public boolean needReload() {
		return false;
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Updates_Title);
	}

	private static void showErrorMessage(String message, Optional<Image> icon, Window owner) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> showErrorMessage(message, icon, owner));
			return;
		}

		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(owner);
		alert.initModality(Modality.WINDOW_MODAL);
		alert.setContentText(message);
		if (icon.isPresent()) {
			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			stage.getIcons().add(icon.get());
		}
		alert.showAndWait();
	}
}
