package de.tobias.playpad.initialize

import de.thecodelabs.storage.settings.StorageTypes
import de.thecodelabs.utils.application
import de.thecodelabs.utils.util.SystemUtils
import de.thecodelabs.versionizer.VersionizerItem
import de.thecodelabs.versionizer.config.{Artifact, Repository}
import de.thecodelabs.versionizer.service.UpdateService
import de.tobias.playpad.PlayPadImpl

class VersionizerSetupTask extends PlayPadInitializeTask {
	override def name(): String = "Versionizer"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		val artifact = app.getClasspathResource("build-app.json").deserialize(StorageTypes.JSON, classOf[Artifact])
		val repository = app.getClasspathResource("repository.yml").deserialize(StorageTypes.YAML, classOf[Repository])

		val globalSettings = instance.getGlobalSettings

		val versionizerItem = new VersionizerItem(repository, SystemUtils.getRunPath.toString)
		val updateService = UpdateService.startVersionizer(versionizerItem, UpdateService.Strategy.JAR, UpdateService.InteractionType.GUI)

		updateService.addArtifact(artifact, SystemUtils.getRunPath)
		updateService.setRepositoryType(globalSettings.getUpdateChannel)
		instance.setUpdateService(updateService)
	}
}
