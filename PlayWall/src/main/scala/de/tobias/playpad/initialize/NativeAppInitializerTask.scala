package de.tobias.playpad.initialize
import de.thecodelabs.utils.application
import de.thecodelabs.utils.application.system.NativeApplication
import de.tobias.playpad.PlayPadImpl
import javafx.scene.image.Image

class NativeAppInitializerTask extends PlayPadInitializeTask {
	override def name(): String = "Native"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		NativeApplication.sharedInstance.setDockIcon(new Image("icon_large.png"))
		NativeApplication.sharedInstance.setAppearance(true)
	}
}
