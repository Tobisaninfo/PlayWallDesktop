package de.tobias.playpad.plugin.content.pad

import de.thecodelabs.utils.threading.Worker
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.pad.Pad
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.plugin.content.util.FfmpegUtils
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.scene.control.{Button, Label}
import javafx.scene.layout.{HBox, VBox}

class ContentPlayerPlaylistView(pad: Pad, mediaPath: MediaPath) extends VBox {

	var sizeLabel: Label = new Label()
	var convertButton = new Button(Localization.getString("plugin.content.player.playlist.convert"))

	private val sizeTextLabel = new Label(Localization.getString("plugin.content.player.playlist.size"))
	private val sizeBox = new HBox(sizeTextLabel, sizeLabel)

	sizeTextLabel.setMinWidth(150)
	sizeBox.setSpacing(14)

	convertButton.setOnAction(onConvertVStackButton)

	getChildren.addAll(sizeBox, convertButton)

	Worker.runLater(() => {
		val resolution = FfmpegUtils.getResolution(mediaPath.getPath)
		Platform.runLater(() => {
			sizeLabel.setText(f"${resolution.getKey} x ${resolution.getValue}")
		})
	})


	private def onConvertVStackButton(event: ActionEvent) = {
		FfmpegUtils.convertMediaVStack(mediaPath.getPath)
	}
}
