package de.tobias.playpad.layout.desktop.listener;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.desktop.pad.DesktopPadViewController;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.GlobalSettings;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class PadNewContentListener {

	private final Pad pad;

	public PadNewContentListener(Pad pad) {
		this.pad = pad;
	}

	public List<File> showMediaOpenFileChooser(ActionEvent event, String[] supportedFileTypes, boolean multiSelect) {
		GlobalSettings settings = PlayPadPlugin.getInstance().getGlobalSettings();
		if (pad.getProject() != null && settings.isLiveMode() && settings.isLiveModeFile() && pad.getProject().getActivePlayers() > 0) {
			return Collections.emptyList();
		}

		final FileChooser chooser = new FileChooser();
		final FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(Localization.getString(Strings.FILE_FILTER_MEDIA), supportedFileTypes);
		chooser.getExtensionFilters().add(extensionFilter);

		// Last Folder
		final Object openFolder = ApplicationUtils.getApplication().getUserDefaults().getData(DesktopPadViewController.OPEN_FOLDER);
		if (openFolder != null) {
			File folder = new File(openFolder.toString());
			if (folder.exists()) {
				chooser.setInitialDirectory(folder);
			}
		}

		final List<File> selectedFiles;
		final Window window = ((Node) event.getTarget()).getScene().getWindow();
		if (multiSelect) {
			selectedFiles = chooser.showOpenMultipleDialog(window);
		} else {
			selectedFiles = Collections.singletonList(chooser.showOpenDialog(window));
		}

		if (selectedFiles != null && !selectedFiles.isEmpty()) {
			ApplicationUtils.getApplication().getUserDefaults().setData(DesktopPadViewController.OPEN_FOLDER, selectedFiles.get(0).getParent());
		}
		return selectedFiles;
	}

	public void onNew(ActionEvent event, PadContentFactory.PadContentTypeChooser padContentTypeChooser) throws NoSuchComponentException {
		final PadContentRegistry registry = PlayPadPlugin.getRegistries().getPadContents();
		final List<File> files = showMediaOpenFileChooser(event, registry.getSupportedFileTypes(), false);

		if (files != null) {
			final Path path = files.get(0).toPath();

			final List<PadContentFactory> connects = registry.getPadContentConnectsForFile(path);
			if (!connects.isEmpty()) {
				if (connects.size() > 1) { // Multiple content types possible
					padContentTypeChooser.showOptions(connects, padContent ->
					{
						if (padContent != null) {
							setNewPadContent(path, padContent);
						}
					});
				} else {
					PadContentFactory padContent = connects.iterator().next();
					setNewPadContent(path, padContent);
				}
			}
		}
	}

	private void setNewPadContent(Path path, PadContentFactory connect) {
		if (pad.getContent() == null || !pad.getContent().getType().equals(connect.getType())) {
			this.pad.setContentType(connect.getType());
		}

		if (pad.isPadVisible()) {
			pad.getController().getView().showBusyView(true);
		}

		pad.setPath(path);
	}

}
