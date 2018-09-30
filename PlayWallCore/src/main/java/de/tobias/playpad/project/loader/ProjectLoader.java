package de.tobias.playpad.project.loader;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.*;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.ConnectionState;
import de.tobias.playpad.server.Server;
import de.tobias.utils.threading.Worker;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tobias on 19.03.17.
 */
public class ProjectLoader {
	private final ProjectReference projectReference;

	private ProjectReader.ProjectReaderDelegate delegate;
	private ProjectReader.ProjectReaderListener listener;

	private boolean loadMedia = true;
	private boolean loadProfile = true;

	public ProjectLoader(ProjectReference projectReference) {
		this.projectReference = projectReference;
	}

	public ProjectReference getProjectReference() {
		return projectReference;
	}

	public ProjectReader.ProjectReaderDelegate getDelegate() {
		return delegate;
	}

	public ProjectLoader setDelegate(ProjectReader.ProjectReaderDelegate delegate) {
		this.delegate = delegate;
		return this;
	}

	public ProjectReader.ProjectReaderListener getListener() {
		return listener;
	}

	public ProjectLoader setListener(ProjectReader.ProjectReaderListener listener) {
		this.listener = listener;
		return this;
	}

	public boolean isLoadMedia() {
		return loadMedia;
	}

	public ProjectLoader setLoadMedia(boolean loadMedia) {
		this.loadMedia = loadMedia;
		return this;
	}

	public boolean isLoadProfile() {
		return loadProfile;
	}

	public void setLoadProfile(boolean loadProfile) {
		this.loadProfile = loadProfile;
	}

	public Project load() throws DocumentException, ProfileNotFoundException, IOException, ProjectNotFoundException, ProjectReader.ProjectReaderDelegate.ProfileAbortException {
		// Load Profile
		if (loadProfile) {
			// TODO Why should the profile be loaded first
			if (projectReference.getProfileReference() == null && delegate != null) {
				// Lädt Profile / Erstellt neues und hat es gleich im Speicher
				ProfileReference profile = delegate.getProfileReference();
				projectReference.setProfileReference(profile);
			}

			// Lädt das entsprechende Profile und aktiviert es
			Profile.load(projectReference.getProfileReference());
		}

		if (listener != null)
			listener.startReadProject();

		Project project = loadProjectImpl(projectReference, delegate);

		if (listener != null)
			listener.finishReadProject();

		Worker.runLater(() -> {
			if (loadMedia) {
				loadMedia(project);
			}
			if (listener != null) {
				listener.finish();
			}
		});
		return project;
	}

	private Project loadProjectImpl(ProjectReference projectReference, ProjectReader.ProjectReaderDelegate delegate) throws IOException, DocumentException, ProjectNotFoundException {
		Server server = PlayPadPlugin.getServerHandler().getServer();

		ProjectReader reader;
		if (projectReference.isSync() && server.getConnectionState() == ConnectionState.CONNECTED) {
			reader = new ProjectSyncSerializer();
		} else {
			reader = new ProjectSerializer();
		}
		return reader.read(projectReference, delegate);
	}

	private void loadMedia(Project project) {
		Collection<Pad> pads = project.getPads();
		List<Pad> filteredPads = pads.parallelStream().filter(pad -> pad.getStatus() != PadStatus.EMPTY).collect(Collectors.toList());
		long padContentCount = filteredPads.size();

		if (listener != null)
			listener.totalMedia((int) padContentCount);

		for (Pad pad : filteredPads) {
			if (listener != null)
				listener.readMedia(pad.getName());
			pad.loadContent();
		}
	}
}
