package de.tobias.playpad.viewcontroller.dialog;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.playpad.project.importer.Importable;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

@Deprecated
public class ImportDialog implements Importable, ProjectReader.ProjectReaderDelegate {

	private static ImportDialog instance;
	private static Window owner;

	static {
		instance = new ImportDialog();
	}

	public static ImportDialog getInstance(Window stage) {
		ImportDialog.owner = stage;
		return instance;
	}

	private Window getStage() {
		return owner;
	}

	@Override
	public String replaceProfile(String name) {
		Alert replaceAlert = new Alert(AlertType.CONFIRMATION);
		replaceAlert.initOwner(getStage());
		replaceAlert.initModality(Modality.WINDOW_MODAL);
		replaceAlert.setContentText(Localization.getString(Strings.UI_Dialog_Import_ReplaceProfile_Content, name));

		ButtonType skipButton = new ButtonType(Localization.getString(Strings.UI_Dialog_Import_ReplaceProfile_Skip));
		ButtonType renameButton = new ButtonType(Localization.getString(Strings.UI_Dialog_Import_ReplaceProfile_Rename));
		replaceAlert.getButtonTypes().setAll(skipButton, renameButton);

		Stage replaceStage = (Stage) replaceAlert.getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(replaceStage.getIcons()::add);

		Optional<ButtonType> resultButton = replaceAlert.showAndWait();
		if (resultButton.isPresent()) {
			if (resultButton.get() == renameButton) {
				TextInputDialog alert = new TextInputDialog(name);
				alert.initOwner(getStage());
				alert.initModality(Modality.WINDOW_MODAL);
				alert.setContentText(Localization.getString(Strings.UI_Dialog_Import_ReplaceProfile_ReplaceContent, name));

				Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
				PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

				if (alert.showAndWait().filter(item -> item != null).isPresent()) {
					return alert.getResult();
				}
			} else {
				return null;
			}
		}
		return null;
	}

	@Override
	public String replaceProject(String name) {
		TextInputDialog alert = new TextInputDialog(name.replace(PlayPadMain.projectType.substring(1), ""));
		alert.initOwner(getStage());
		alert.initModality(Modality.WINDOW_MODAL);
		alert.setContentText(Localization.getString(Strings.UI_Dialog_Import_ReplaceProject_ReplaceContent, name));

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		if (alert.showAndWait().filter(item -> item != null).isPresent()) {
			return alert.getResult();
		}

		return null;
	}

	@Override
	public Path mediaFolder() {
		Alert copyMediaAlert = new Alert(AlertType.CONFIRMATION);
		copyMediaAlert.initOwner(getStage());
		copyMediaAlert.initModality(Modality.WINDOW_MODAL);
		copyMediaAlert.setContentText(Localization.getString(Strings.UI_Dialog_Import_ReplaceMedia_Content));

		ButtonType copyButton = new ButtonType(Localization.getString(Strings.UI_Dialog_Import_ReplaceMedia_Copy));
		ButtonType skipButton = new ButtonType(Localization.getString(Strings.UI_Dialog_Import_ReplaceMedia_Skip));

		copyMediaAlert.getButtonTypes().setAll(copyButton, skipButton);

		Stage replaceStage = (Stage) copyMediaAlert.getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(replaceStage.getIcons()::add);

		Optional<ButtonType> resultButton = copyMediaAlert.showAndWait();
		if (resultButton.isPresent()) {
			if (resultButton.get() == copyButton) {
				DirectoryChooser chooser = new DirectoryChooser();
				File file = chooser.showDialog(owner);
				if (file != null) {
					return file.toPath();
				}
			}
		}
		return null;
	}

	@Override
	public ProfileReference getProfileReference() {
		ProfileChooseDialog dialog = new ProfileChooseDialog(getStage());

		dialog.getStageContainer().ifPresent(NVCStage::showAndWait);
		Profile profile = dialog.getProfile();
		if (profile != null) {
			return profile.getRef();
		}
		return null;
	}

}
