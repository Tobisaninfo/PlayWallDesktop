package de.tobias.playpad.plugin.content.pad

import de.thecodelabs.utils.io.PathUtils
import de.tobias.playpad.pad.Pad
import de.tobias.playpad.pad.view.IPadContentView
import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.geometry.{Insets, Pos}
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.{Pane, Priority, VBox}
import javafx.scene.text.TextAlignment

class ContentPlayerPadPreview(pad: Pad, parent: Pane) extends VBox with IPadContentView {

	val nameLabel: Label = new Label()
	val subTitleLabel: Label = new Label()

	setupLabel(nameLabel)
	setupLabel(subTitleLabel)

	getChildren.addAll(nameLabel, subTitleLabel)
	setSpacing(3)
	setPadding(new Insets(7))

	getStyleClass.addListener(new ListChangeListener[String] {
		override def onChanged(c: ListChangeListener.Change[_ <: String]): Unit = {
			while (c.next()) {
				if (c.wasRemoved()) {
					nameLabel.getStyleClass.removeAll(c.getRemoved)
					subTitleLabel.getStyleClass.removeAll(c.getRemoved)
				}

				if (c.wasAdded()) {
					nameLabel.getStyleClass.addAll(c.getAddedSubList)
					subTitleLabel.getStyleClass.addAll(c.getAddedSubList)
				}
			}
		}
	})

	nameLabel.textProperty.bind(pad.nameProperty)
	pad.getContent match {
		case content: ContentPlayerPadContent =>
			subTitleLabel.textProperty().bind(Bindings.createStringBinding(() => {
				if (content.currentPlayingMediaIndex < 0) {
					""
				} else {
					PathUtils.getFilenameWithoutExtension(pad.getPaths.get(content.currentPlayingMediaIndex).getPath.getFileName)
				}
			}, content.currentPlayingMediaIndexProperty()))
		case _ =>
	}

	private def setupLabel(label: Label): Unit = {
		label.setWrapText(true)
		label.setAlignment(Pos.CENTER)
		label.setTextAlignment(TextAlignment.CENTER)

		label.prefWidthProperty.bind(parent.widthProperty)
		label.setMaxHeight(Double.MaxValue)

		VBox.setVgrow(label, Priority.ALWAYS)
	}

	override def getNode: Node = this

	override def deInit(): Unit = {
		nameLabel.textProperty().unbind()
		subTitleLabel.textProperty().unbind()
	}
}
