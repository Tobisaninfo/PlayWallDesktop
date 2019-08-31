package de.tobias.playpad.viewcontroller.dialog;

import de.thecodelabs.utils.ui.AdvancedDialog;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.versionizer.config.Artifact;
import de.thecodelabs.versionizer.model.Version;
import de.thecodelabs.versionizer.service.UpdateService;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Map;

public class AutoUpdateDialog extends AdvancedDialog {

	public AutoUpdateDialog(UpdateService updateService, Window owner) {
		super(owner);
		final Map<Artifact, Version> remoteVersions = updateService.getRemoteVersions();

		StringBuilder builder = new StringBuilder();
		for (Artifact artifact : remoteVersions.keySet()) {
			builder.append(artifact.getArtifactId());
			builder.append(" ");
			builder.append(remoteVersions.get(artifact).toVersionString());
			builder.append("\n");
		}

		setTitle(Localization.getString(Strings.UI_DIALOG_AUTO_UPDATE_TITLE));
		setContent(Localization.getString(Strings.UI_DIALOG_AUTO_UPDATE_CONTENT, builder.toString()));
		setHeaderText(Localization.getString(Strings.UI_DIALOG_AUTO_UPDATE_HEADER));
		setCheckboxText(Localization.getString(Strings.UI_DIALOG_AUTO_UPDATE_CHECKBOX));

		setIcon(PlayPadPlugin.getInstance().getIcon());

		ButtonType updateButton = new ButtonType(Localization.getString(Strings.UI_DIALOG_AUTO_UPDATE_BUTTON_UPDATE), ButtonData.APPLY);
		ButtonType cancelButton = new ButtonType(Localization.getString(Strings.UI_DIALOG_AUTO_UPDATE_BUTTON_CANCEL), ButtonData.CANCEL_CLOSE);

		addButtonType(updateButton);
		addButtonType(cancelButton);
	}
}
