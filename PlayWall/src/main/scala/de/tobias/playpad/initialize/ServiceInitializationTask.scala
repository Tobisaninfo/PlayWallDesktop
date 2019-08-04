package de.tobias.playpad.initialize
import de.thecodelabs.utils.application
import de.tobias.playpad.{PlayPadImpl, PlayPadPlugin, RegistryCollectionImpl}
import de.tobias.playpad.design.ModernStyleableImpl
import de.tobias.playpad.server.ServerHandlerImpl
import de.tobias.playpad.server.sync.command.CommandExecutorHandlerImpl
import de.tobias.playpad.view.MapperListViewControllerImpl
import de.tobias.playpad.viewcontroller.BaseMapperListViewController

class ServiceInitializationTask extends PlayPadInitializeTask {
	override def name(): String = "Services"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		PlayPadPlugin.setStyleable(new ModernStyleableImpl)
		PlayPadPlugin.setRegistryCollection(new RegistryCollectionImpl)
		PlayPadPlugin.setServerHandler(new ServerHandlerImpl)
		PlayPadPlugin.setCommandExecutorHandler(new CommandExecutorHandlerImpl)

		BaseMapperListViewController.setInstance(new MapperListViewControllerImpl)
	}
}
