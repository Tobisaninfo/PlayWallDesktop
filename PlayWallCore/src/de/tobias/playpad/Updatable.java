package de.tobias.playpad;

import java.net.URL;
import java.nio.file.Path;

public interface Updatable {

	public int getCurrentBuild();

	public String getCurrentVersion();

	public int getNewBuild();

	public String getNewVersion();

	public boolean checkUpdate();

	public URL getDownloadPath();
	
	public Path getLocalPath();
	
	public String name();

}
