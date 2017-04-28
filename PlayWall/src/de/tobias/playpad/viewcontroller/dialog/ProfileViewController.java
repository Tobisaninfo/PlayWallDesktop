package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.viewcontroller.cell.DisplayableCell;
import de.tobias.playpad.viewcontroller.dialog.profile.DuplicateProfileDialog;
import de.tobias.playpad.viewcontroller.dialog.profile.NewProfileDialog;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.dom4j.DocumentException;

import java.io.IOException;

public class ProfileViewController extends NVC implements ChangeListener<ProfileReference> {

	@FXML
	private ListView<ProfileReference> profileList;
	@FXML
	private TextField nameTextField;

	@FXML
	private Button newButton;
	@FXML
	private Button duplicateButton;
	@FXML
	private Button deleteButton;
	@FXML
	private Button renameButton;

	@FXML
	private Button chooseButton;

	private Project project;

	public ProfileViewController(Window owner, Project project) {
		load("de/tobias/playpad/assets/dialog/", "profileSettingsView", PlayPadMain.getUiResourceBundle());
		profileList.getSelectionModel().select(Profile.currentProfile().getRef());
		this.project = project;

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));

		if (ProfileReferenceManager.getProfiles().size() == 1
				|| profileList.getSelectionModel().getSelectedItem().equals(Profile.currentProfile().getRef())) {
			deleteButton.setDisable(true);
		}
	}

	@Override
	public void init() {
		profileList.getItems().setAll(ProfileReferenceManager.getProfiles());
		profileList.setCellFactory(list -> new DisplayableCell<>());

		nameTextField.textProperty().addListener((a, b, c) ->
		{
			if (c != null) {
				try {
					if ((ProfileReferenceManager.getProfiles().contains(c) && !profileList.getSelectionModel().getSelectedItem().equals(c))
							&& !c.equals(profileList.getSelectionModel().getSelectedItem().getName())) {
						nameTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
					} else {
						nameTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
					}
				} catch (Exception e) {
					nameTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
				}
			} else {
				nameTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
			}
		});

		profileList.getSelectionModel().selectedItemProperty().addListener(this);
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_Profile_Title));
		stage.setMinWidth(375);
		stage.setMinHeight(500);

		stage.initModality(Modality.WINDOW_MODAL);
		Profile.currentProfile().currentLayout().applyCss(stage);
	}

	@FXML
	private void chooseButtonHandler(ActionEvent event) {
		ProfileReference ref = profileList.getSelectionModel().getSelectedItem();
		project.getProjectReference().setProfileReference(ref);

		try {
			Profile.load(ref);
		} catch (ProfileNotFoundException | DocumentException | IOException e) {
			e.printStackTrace();
		}
		getStageContainer().ifPresent(NVCStage::close);
	}

	@FXML
	private void newButtonHandler(ActionEvent event) {
		NewProfileDialog dialog = new NewProfileDialog(getContainingWindow());
		dialog.showAndWait().ifPresent(profile -> {
			ProfileReference ref = profile.getRef();
			profileList.getItems().add(ref);
			selectProfile(ref);
		});
	}

	@FXML
	private void duplicateButtonHandler(ActionEvent event) {
		if (profileList.getSelectionModel().getSelectedItem() != null) {
			DuplicateProfileDialog dialog = new DuplicateProfileDialog(this, profileList.getSelectionModel().getSelectedItem());

			dialog.getName().ifPresent(newRef ->
			{
				profileList.getItems().add(newRef);
				selectProfile(newRef);
			});
		}
	}

	@FXML
	private void deleteButtonHandler() {
		Alert alert = new Alert(AlertType.CONFIRMATION);

		alert.initOwner(getContainingWindow());
		alert.initModality(Modality.WINDOW_MODAL);
		Stage dialog = (Stage) alert.getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);

		ProfileReference ref = profileList.getSelectionModel().getSelectedItem();

		alert.setContentText(Localization.getString(Strings.UI_Dialog_Profile_Delete_Content, ref));
		alert.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button ->
		{
			try {
				ProfileReferenceManager.removeProfile(ref);
				profileList.getItems().remove(ref);
			} catch (Exception e) {
				e.printStackTrace();
				showErrorMessage(Localization.getString(Strings.Error_Profile_Delete, e.getMessage()));
			}
		});
	}

	@FXML
	private void renameButtonHandler(ActionEvent event) {
		ProfileReference ref = profileList.getSelectionModel().getSelectedItem();
		try {
			String newProfileName = nameTextField.getText();
			if (ProfileReferenceManager.getProfiles().contains(newProfileName)) {
				showErrorMessage(Localization.getString(Strings.Error_Standard_NameInUse, newProfileName));
				return;
			}
			ref.setName(newProfileName);
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage(Localization.getString(Strings.Error_Standard_Gen, ref.getName(), e.getMessage()));
		}
	}

	private void selectProfile(ProfileReference ref) {
		profileList.getSelectionModel().select(ref);
	}

	@Override
	public void changed(ObservableValue<? extends ProfileReference> observable, ProfileReference oldValue, ProfileReference newValue) {
		if (newValue != null) {
			nameTextField.setText(newValue.getName());
		} else {
			nameTextField.clear();
		}
		renameButton.setDisable(newValue == null);
		chooseButton.setDisable(newValue == null);
		duplicateButton.setDisable(newValue == null);

		if (ProfileReferenceManager.getProfiles().size() == 1 || profileList.getSelectionModel().getSelectedItem() == null
				|| profileList.getSelectionModel().getSelectedItem().equals(Profile.currentProfile().getRef())) {
			deleteButton.setDisable(true);
		} else {
			deleteButton.setDisable(false);
		}
	}
}
