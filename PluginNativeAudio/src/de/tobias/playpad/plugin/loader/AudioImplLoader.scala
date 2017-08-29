package de.tobias.playpad.plugin.loader

import java.io.IOException
import java.nio.file.Path

import de.tobias.playpad.plugin.Module
import de.tobias.utils.util.IOUtils

/**
  * Created by tobias on 16.04.17.
  */
trait AudioImplLoader {
	/**
	  * Load the native resources
	  */
	def preInit(): Unit

	/**
	  * Init the audio interface
	  */
	def init(module: Module): Unit

	@throws[IOException]
	def copyResource(resourceFolder: Path, packageName: String, file: String): Path = {
		val dest = resourceFolder.resolve(file)
		IOUtils.copy(getClass.getClassLoader.getResourceAsStream(packageName + file), dest)
		dest
	}
}
