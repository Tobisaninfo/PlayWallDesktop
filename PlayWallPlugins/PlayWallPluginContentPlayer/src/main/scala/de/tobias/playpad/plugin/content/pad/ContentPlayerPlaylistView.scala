package de.tobias.playpad.plugin.content.pad

import de.thecodelabs.utils.threading.Worker
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.pad.Pad
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.plugin.content.util.FfmpegUtils
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control.{Button, Label, ProgressIndicator, Separator}
import javafx.scene.layout.{HBox, VBox}

class ContentPlayerPlaylistView(pad: Pad, mediaPath: MediaPath) extends VBox {

	val progressIndicator = new ProgressIndicator()
	val sizeLabel: Label = new Label()
	val convertButton = new Button(Localization.getString("plugin.content.player.playlist.convert"))

	private val sizeTextLabel = new Label(Localization.getString("plugin.content.player.playlist.size"))
	private val sizeBox = new HBox(sizeTextLabel, sizeLabel)

	sizeTextLabel.setMinWidth(150)
	sizeBox.setSpacing(14)

	convertButton.setOnAction(onConvertVStackButton)
	progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS)
	progressIndicator.setPrefSize(30, 30)

	private val actionBox = new HBox(convertButton, progressIndicator)
	actionBox.setAlignment(Pos.CENTER_LEFT)
	actionBox.setSpacing(14)

	private val separator = new Separator()

	getChildren.addAll(separator, sizeBox, actionBox)
	setSpacing(14)

	Worker.runLater(() => {
		val resolution = FfmpegUtils.getResolution(mediaPath.getPath)
		Platform.runLater(() => {
			sizeLabel.setText(f"${resolution.getKey} x ${resolution.getValue}")
			progressIndicator.setVisible(false)
		})
	})


	private def onConvertVStackButton(event: ActionEvent): Unit = {
		progressIndicator.setVisible(true)
		Worker.runLater(() => {
			FfmpegUtils.convertMediaVStack(mediaPath.getPath)
			Platform.runLater(() => {
				progressIndicator.setVisible(false)
			})
		})
	}
}
