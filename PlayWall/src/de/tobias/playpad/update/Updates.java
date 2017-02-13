package de.tobias.playpad.update;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.updater.client.UpdateRegistery;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.NativeLauncher;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.IOUtils;
import de.tobias.utils.util.OS;
import javafx.application.Platform;

public class Updates {

	// Ornder im App-Folder wo Updates zwischengespeichert werden sollen.
	private static final String CACHE_FOLER = "Updates";

	// Name der Updater Datei
	private static final String JAR_NAME = "PlayWallUp.jar";
	private static final String EXE_NAME = "PlayWallUp.exe";

	/**
	 * Sollte in einem Extra Thread gemacht werden, da der Updater gedownloaded wird.
	 *
	 * @throws IOException Internal Error
	 */
	public static void startUpdate() throws IOException {
		App app = ApplicationUtils.getApplication();
		String downloadPath = app.getPath(PathType.DOWNLOAD, CACHE_FOLER).toString();
		String updateParameter = UpdateRegistery.buildParamaterString(downloadPath);

		boolean successfulStartUpdate = false;

		// Start des Update Prozesses
		if (OS.isWindows()) {
			successfulStartUpdate = updateWindows(updateParameter, UpdateRegistery.needsAdminPermission());
		} else if (OS.isMacOS()) {
			successfulStartUpdate = updateMacOS(updateParameter);
		}

		if (successfulStartUpdate) {
			// Kill the program
			Platform.exit();
			System.exit(0);
		}
	}

	private static boolean updateWindows(String parameter, boolean needAdminPermission) throws IOException {
		Path path = searchForFile(EXE_NAME);
		if (path == null) {
			App app = ApplicationUtils.getApplication();

			String updaterURL = app.getInfo().getUserInfo().get(AppUserInfoStrings.UPDATER_PROGRAM) + EXE_NAME;
			path = ApplicationUtils.getApplication().getPath(PathType.DOWNLOAD, EXE_NAME);

			downloadUpdater(updaterURL, path);
		}
		startExeFile(parameter, path, needAdminPermission);
		return false;
	}

	private static boolean updateMacOS(String parameter) throws IOException {
		Path path = searchForFile(JAR_NAME);
		if (path == null) {
			App app = ApplicationUtils.getApplication();

			String updaterURL = app.getInfo().getUserInfo().get(AppUserInfoStrings.UPDATER_PROGRAM) + JAR_NAME;
			path = ApplicationUtils.getApplication().getPath(PathType.DOWNLOAD, JAR_NAME);

			downloadUpdater(updaterURL, path);
		}
		startJarFile(parameter, path);
		return false;
	}

	private static void downloadUpdater(String updaterURL, Path path) throws IOException {
		URL url = new URL(updaterURL);
		InputStream iStr = url.openStream();

		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
		}

		IOUtils.copy(iStr, path);

		iStr.close();
	}

	private static void startExeFile(String parameter, Path fileExe, boolean needAdminPermission) throws IOException {
		if (needAdminPermission) {
			NativeLauncher.executeAsAdministrator(fileExe.toAbsolutePath().toString(), parameter);
		} else {
			ProcessBuilder builder = new ProcessBuilder(fileExe.toAbsolutePath().toString(), parameter);
			builder.start();
		}

		System.exit(0);
	}

	private static Path searchForFile(String name) {
		Path file = Paths.get(name);
		Path fileFolder = ApplicationUtils.getApplication().getPath(PathType.DOWNLOAD, name);

		if (Files.exists(file)) {
			return file;
		} else if (Files.exists(fileFolder)) {
			return fileFolder;
		}
		return null;
	}

	private static void startJarFile(String parameter, Path fileJar) throws IOException {
		ProcessBuilder builder = new ProcessBuilder("java", "-jar", fileJar.toAbsolutePath().toString(), parameter);
		builder.start();
		System.exit(0);
	}

}
