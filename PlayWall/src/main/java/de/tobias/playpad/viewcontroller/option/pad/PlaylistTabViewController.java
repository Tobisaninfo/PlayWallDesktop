package de.tobias.playpad.viewcontroller.option.pad;

import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.desktop.listener.PadNewContentListener;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class PlaylistTabViewController extends PadSettingsTabViewController {

	@FXML
	private ListView<MediaPath> mediaPathListView;

	@FXML
	private Button addButton;
	@FXML
	private Button upButton;
	@FXML
	private Button downButton;

	@FXML
	private Label pathLabel;
	@FXML
	private Button deleteButton;
	@FXML
	private Button showFileButton;

	private final Pad pad;

	public PlaylistTabViewController(Pad pad) {
		this.pad = pad;
		load("view/option/pad", "PlaylistTab", Localization.getBundle());

		mediaPathListView.setItems(pad.getPaths());
		initButtons();
	}

	@Override
	public void init() {
		addButton.setGraphic(new FontIcon(FontAwesomeType.PLUS));
		upButton.setGraphic(new FontIcon(FontAwesomeType.ARROW_UP));
		downButton.setGraphic(new FontIcon(FontAwesomeType.ARROW_DOWN));

		mediaPathListView.setCellFactory(param -> new ListCell<MediaPath>() {
			@Override
			protected void updateItem(MediaPath item, boolean empty) {
				super.updateItem(item, empty);
				if (!empty) {
					setText(item.getFileName());
				} else {
					setText("");
				}
			}
		});

		mediaPathListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				pathLabel.setText(newValue.getPath().toString());
			} else {
				pathLabel.setText("");
			}
		});
	}

	private void initButtons() {
		upButton.disableProperty()
				.bind(mediaPathListView.getSelectionModel().selectedItemProperty().isNull()
						.or(mediaPathListView.getSelectionModel().selectedIndexProperty().isEqualTo(0)));
		downButton.disableProperty()
				.bind(mediaPathListView.getSelectionModel().selectedItemProperty().isNull()
						.or(mediaPathListView.getSelectionModel().selectedIndexProperty().isEqualTo(Bindings.size(mediaPathListView.getItems()).subtract(1))));

		deleteButton.disableProperty().bind(mediaPathListView.getSelectionModel().selectedItemProperty().isNull());
		showFileButton.disableProperty().bind(mediaPathListView.getSelectionModel().selectedItemProperty().isNull());
	}

	@Override
	public String getName() {
		return Localization.getString(Strings.UI_WINDOW_PAD_SETTINGS_PLAYLIST_TITLE);
	}

	@Override
	public void loadSettings(Pad pad) {
		// Not implemented
	}

	@Override
	public void saveSettings(Pad pad) {
		// Not implemented
	}

	@FXML
	public void onAddHandler(ActionEvent event) {
		final PadContentRegistry padContentRegistry = PlayPadPlugin.getRegistries().getPadContents();
		final PadContentFactory contentFactory = padContentRegistry.getFactory(pad.getContent().getType());

		final PadNewContentListener padNewContentListener = new PadNewContentListener(pad);
		final List<File> files = padNewContentListener.showMediaOpenFileChooser(event, contentFactory.getSupportedTypes(), true);

		if (files != null) {
			for (File file : files) {
				pad.addPath(file.toPath());
			}
		}
	}

	@FXML
	public void onUpHandler(ActionEvent event) {
		final int selectedIndex = mediaPathListView.getSelectionModel().getSelectedIndex();
		Collections.swap(pad.getPaths(), selectedIndex, selectedIndex - 1);
		mediaPathListView.getSelectionModel().select(selectedIndex - 1);
		pad.getContent().reorderMedia();
	}

	@FXML
	public void onDownAction(ActionEvent event) {
		final int selectedIndex = mediaPathListView.getSelectionModel().getSelectedIndex();
		Collections.swap(pad.getPaths(), selectedIndex, selectedIndex + 1);
		mediaPathListView.getSelectionModel().select(selectedIndex + 1);
		pad.getContent().reorderMedia();
	}

	@FXML
	public void onShowFileHandler(ActionEvent event) {
		MediaPath mediaPath = mediaPathListView.getSelectionModel().getSelectedItem();
		NativeApplication.sharedInstance().showFileInFileViewer(mediaPath.getPath());
	}

	@FXML
	public void onDeleteHandler(ActionEvent event) {
		MediaPath mediaPath = mediaPathListView.getSelectionModel().getSelectedItem();
		pad.getContent().unloadMedia(mediaPath);
		pad.removePath(mediaPath);

		if (pad.getPaths().isEmpty()) {
			pad.clear();
		}
	}
}
