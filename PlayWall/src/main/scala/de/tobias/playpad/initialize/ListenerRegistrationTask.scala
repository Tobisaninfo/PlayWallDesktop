package de.tobias.playpad.initialize

import de.thecodelabs.utils.application
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.trigger.VolumeTriggerVolumeFilter

class ListenerRegistrationTask extends PlayPadInitializeTask {
	override def name(): String = "ListenerRegistration"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		instance.addPadListener(VolumeTriggerVolumeFilter.getInstance())
	}
}
