package de.tobias.playpad.project;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import org.dom4j.DocumentException;

import java.io.IOException;

/**
 * Created by tobias on 22.02.17.
 */
public class ProjectSyncReader implements ProjectReader {

	@Override
	public Project read(ProjectReference projectReference, ProjectReaderDelegate delegate) throws IOException, DocumentException, ProfileNotFoundException {
		// TODO Why should the profile be loaded first
		if (projectReference.getProfileReference() == null) {
			// Lädt Profile / Erstellt neues und hat es gleich im Speicher
			ProfileReference profile = delegate.getProfileReference();
			projectReference.setProfileReference(profile);
		}

		// Lädt das entsprechende Profile und aktiviert es
		Profile.load(projectReference.getProfileReference());

		Server server = PlayPadPlugin.getServerHandler().getServer();
		return server.getProject(projectReference);
	}
}
