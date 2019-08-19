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

		setTitle(Localization.getString(Strings.UI_Dialog_AutoUpdate_Title));
		setContent(Localization.getString(Strings.UI_Dialog_AutoUpdate_Content, builder.toString()));
		setHeaderText(Localization.getString(Strings.UI_Dialog_AutoUpdate_Header));
		setCheckboxText(Localization.getString(Strings.UI_Dialog_AutoUpdate_Checkbox));

		setIcon(PlayPadPlugin.getInstance().getIcon());

		ButtonType updateButton = new ButtonType(Localization.getString(Strings.UI_Dialog_AutoUpdate_Button_Update), ButtonData.APPLY);
		ButtonType cancelButton = new ButtonType(Localization.getString(Strings.UI_Dialog_AutoUpdate_Button_Cancel), ButtonData.CANCEL_CLOSE);

		addButtonType(updateButton);
		addButtonType(cancelButton);
	}
}
