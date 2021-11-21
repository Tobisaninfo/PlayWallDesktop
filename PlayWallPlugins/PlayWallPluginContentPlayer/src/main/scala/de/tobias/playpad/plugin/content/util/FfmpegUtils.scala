package de.tobias.playpad.plugin.content.util

import de.thecodelabs.logger.Logger
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.settings.ContentPlayerPluginConfiguration
import de.tobias.playpad.profile.Profile
import javafx.util.Pair
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.{FFmpeg, FFmpegExecutor, FFprobe}
import org.apache.commons.lang3.StringUtils

import java.nio.file.{Files, Path}

object FfmpegUtils {

	private var ffmpeg: FFmpeg = _
	private var ffprobe: FFprobe = _

	def initialize(): Unit = {
		Logger.debug("Initialize ffmpeg and ffprobe")
		val profile = Profile.currentProfile()
		val contentPluginConfiguration = profile.getCustomSettings(ContentPluginMain.zoneConfigurationKey).asInstanceOf[ContentPlayerPluginConfiguration]

		if (StringUtils.isNotEmpty(contentPluginConfiguration.ffmpegExecutable) && StringUtils.isNotEmpty(contentPluginConfiguration.ffprobeExecutable)) {
			ffmpeg = new FFmpeg(contentPluginConfiguration.ffmpegExecutable)
			ffprobe = new FFprobe(contentPluginConfiguration.ffprobeExecutable)
		}
	}

	def checkInitialization(): Boolean = {
		if (ffprobe == null || ffmpeg == null) {
			initialize()

			return ffprobe != null && ffmpeg != null
		}
		true
	}

	def getResolution(path: Path): Pair[Int, Int] = {
		if (!checkInitialization()) {
			return null
		}

		val probeResult = ffprobe.probe(path.toAbsolutePath.toString)

		val stream = probeResult.streams.head
		if (stream != null) {
			return new Pair(stream.width, stream.height)
		}
		null
	}

	def convertMediaVStack(path: Path): Unit = {
		if (!checkInitialization()) {
			return
		}

		val globalSettings = PlayPadPlugin.getInstance.getGlobalSettings
		val convertPath = globalSettings.getCachePath.resolve(path.getFileName + ".mp4")

		if (Files.notExists(convertPath.getParent)) {
			Files.createDirectories(convertPath.getParent)
		}

		val builder = new FFmpegBuilder()
		  .addInput(path.toAbsolutePath.toString)
		  .addInput(path.toAbsolutePath.toString)
		  .overrideOutputFiles(true)
		  .addOutput(convertPath.toAbsolutePath.toString)
		  .setVideoBitRate(5_000_000)
		  .setVideoCodec("libx264")
		  .addExtraArgs("-filter_complex", "vstack")
		  .done()

		val executor = new FFmpegExecutor(ffmpeg, ffprobe)
		executor.createJob(builder).run()
	}
}
