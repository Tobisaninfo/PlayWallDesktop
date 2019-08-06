package de.tobias.playpad.initialize
import de.thecodelabs.midi.action.{ActionKeyHandler, ActionRegistry}
import de.thecodelabs.midi.midi.MidiCommandHandler
import de.thecodelabs.utils.application
import de.tobias.playpad.{PlayPadImpl, PlayPadPlugin}
import de.tobias.playpad.action.ActionProvider
import de.tobias.playpad.midi.PD12

class MidiActionsInitializerTask extends PlayPadInitializeTask {

	override def name(): String = "Midi"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		val registryCollection = PlayPadPlugin.getRegistries

		ActionKeyHandler.setRunOnFxThread(true)
		registryCollection.getActions.getComponents.forEach((actionProvider: ActionProvider) => {
			ActionRegistry.registerActionHandler(actionProvider.getActionHandler)
		})

		MidiCommandHandler.getInstance.addMidiListener(new PD12)
	}
}
