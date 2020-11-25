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

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

public class PadNewContentListener {

	private final Pad pad;

	public PadNewContentListener(Pad pad) {
		this.pad = pad;
	}

	public void onNew(ActionEvent event, PadContentFactory.PadContentTypeChooser padContentTypeChooser) throws NoSuchComponentException {
		GlobalSettings settings = PlayPadPlugin.getInstance().getGlobalSettings();
		if (pad.getProject() != null && settings.isLiveMode() && settings.isLiveModeFile() && pad.getProject().getActivePlayers() > 0) {
			return;
		}

		final FileChooser chooser = new FileChooser();
		PadContentRegistry registry = PlayPadPlugin.getRegistries().getPadContents();

		// File Extension
		final FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(Localization.getString(Strings.FILE_FILTER_MEDIA), registry.getSupportedFileTypes());
		chooser.getExtensionFilters().add(extensionFilter);

		// Last Folder
		final Object openFolder = ApplicationUtils.getApplication().getUserDefaults().getData(DesktopPadViewController.OPEN_FOLDER);
		if (openFolder != null) {
			File folder = new File(openFolder.toString());
			if (folder.exists()) {
				chooser.setInitialDirectory(folder);
			}
		}

		final File file = chooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
		if (file != null) {
			Path path = file.toPath();

			final Set<PadContentFactory> connects = registry.getPadContentConnectsForFile(file.toPath());
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

			ApplicationUtils.getApplication().getUserDefaults().setData(DesktopPadViewController.OPEN_FOLDER, path.getParent().toString());
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
