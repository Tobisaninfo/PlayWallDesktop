package de.tobias.playpad.update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public interface Updatable {

	public int getCurrentBuild();

	public String getCurrentVersion();

	public int getNewBuild();

	public String getNewVersion();

	public boolean isUpdateAvailable();
	
	public void loadInformation(UpdateChannel channel) throws IOException, URISyntaxException;

	public URL getDownloadPath();
	
	public Path getLocalPath();
	
	public String name();

}
