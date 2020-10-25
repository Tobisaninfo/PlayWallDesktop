package de.tobias.playpad.initialize

import java.io.IOException

import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.application
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.profile.ref.ProfileReferenceManager
import org.dom4j.DocumentException

class ProfileLoadingTask extends PlayPadInitializeTask {
	override def name(): String = "Profile loading"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		try ProfileReferenceManager.loadProfiles()
		catch {
			case e@(_: IOException | _: DocumentException) => Logger.error(e)
		}
	}
}
