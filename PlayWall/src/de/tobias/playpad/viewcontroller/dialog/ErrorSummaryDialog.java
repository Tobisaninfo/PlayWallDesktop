package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.PadException;
import de.tobias.playpad.pad.PadException.PadExceptionType;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.cell.errordialog.ErrorCell;
import de.tobias.playpad.viewcontroller.cell.errordialog.FixCell;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ErrorSummaryDialog extends ViewController {

	private static ErrorSummaryDialog instance;

	@FXML private TableView<PadException> errorTable;

	@FXML private TableColumn<PadException, String> padColumn;
	@FXML private TableColumn<PadException, String> errorColumn;
	@FXML private TableColumn<PadException, PadException> fixColumn;

	@FXML private Button closeButton;

	public ErrorSummaryDialog(Window owner) {
		super("errorSummaryDialog", "de/tobias/playpad/assets/dialog/", null, PlayPadMain.getUiResourceBundle());
		instance = this;

		getStage().initOwner(owner);
		getStage().initModality(Modality.WINDOW_MODAL);
	}

	@Override
	public void init() {
		errorTable.setPlaceholder(new Label(Localization.getString(Strings.UI_Placeholder_ErrorSummary)));
		// Disable das etwas ausgewählt werden kann
		errorTable.setSelectionModel(null);

		padColumn.setCellValueFactory(param ->
		{
			StringProperty value = new SimpleStringProperty(param.getValue().getPad().toReadableString());
			return value;
		});

		errorColumn.setCellFactory(param -> new ErrorCell());
		errorColumn.setCellValueFactory(param ->

		{
			PadException padException = param.getValue();
			String string = "";
			try {
				if (padException.getType() == PadExceptionType.UNKOWN_CONTENT_TYPE)
					string = Localization.getString(Strings.Error_Pad_BaseName + padException.getType().name(),
							padException.getPad().getIndexReadable());
				else
					string = Localization.getString(Strings.Error_Pad_BaseName + padException.getType().name(),
							padException.getPath().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			StringProperty value = new SimpleStringProperty(string);
			return value;
		});

		fixColumn.setCellFactory(param -> new FixCell(getStage()));
		fixColumn.setCellValueFactory(param ->

		{
			ObjectProperty<PadException> type = new SimpleObjectProperty<>(param.getValue());
			return type;
		});

		// Close CMD+W
		getStage().getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
				() -> Platform.runLater(() -> getStage().close()));
	}

	public void setProject(Project project) {
		// errorTable.setItems(project.getExceptions()); TODO Error Handling User
		errorTable.getItems().addListener(new ListChangeListener<PadException>() {

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends PadException> c) {
				if (!errorTable.getItems().isEmpty()) {
					getStage().show();
				} else {
					getStage().close();
				}
			}
		});
	}

	@Override
	public void initStage(Stage stage) {
		stage.setMinWidth(900);
		stage.setMinHeight(300);

		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_ErrorSummary_Title));
		stage.setWidth(900);
		stage.setHeight(300);

		if (Profile.currentProfile() != null)
			Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	@FXML
	private void closeButtonHandler(ActionEvent event) {
		getStage().close();
	}

	public static ErrorSummaryDialog getInstance() {
		return instance;
	}
}
