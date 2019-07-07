package de.tobias.playpad.profile;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.MappingList;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Profile {

	private static final String PROFILE_SETTINGS_XML = "ProfileSettings.xml";
	private static final String MAPPING_XML = "Mapping.xml";

	private static List<ProfileListener> listeners = new ArrayList<>();
	private static Profile currentProfile;

	// Settings
	private ProfileReference ref;

	private ProfileSettings profileSettings;
	private MappingList mappings;

	/**
	 * Use {@link ProfileReferenceManager#addProfile(ProfileReference)} instead
	 *
	 * @param ref Ref
	 */
	public Profile(ProfileReference ref) {
		this.ref = ref;
		this.profileSettings = new ProfileSettings();
		this.mappings = new MappingList(this);
	}

	public static void registerListener(ProfileListener listener) {
		listeners.add(listener);
	}

	public ProfileReference getRef() {
		return ref;
	}

	public static Profile currentProfile() {
		return currentProfile;
	}

	public static void setCurrentProfile(Profile currentProfile) {
		Profile old = Profile.currentProfile;
		Profile.currentProfile = currentProfile;

		// Notify Profile Change
		listeners.forEach(listener -> listener.reloadSettings(old, currentProfile));
	}

	public MappingList getMappings() {
		return mappings;
	}

	public ProfileSettings getProfileSettings() {
		return profileSettings;
	}

	public static Profile load(ProfileReference ref) throws DocumentException, IOException, ProfileNotFoundException {
		if (ref == null) {
			throw new IllegalArgumentException("Profile is null"); // TODO Check if to catch exception somewhere
		}
		// Altes Speichern bevor neues Geladen
		if (currentProfile != null)
			currentProfile.save();

		App app = ApplicationUtils.getApplication();
		Profile profile = new Profile(ref);

		System.out.println("+++ Load Profile: " + ref + " (" + ref.getUuid() + ") +++");

		if (Files.exists(app.getPath(PathType.CONFIGURATION, ref.getFileName()))) {

			profile.profileSettings = ProfileSettings.load(app.getPath(PathType.CONFIGURATION, ref.getFileName(), PROFILE_SETTINGS_XML));

			// Listener
			PlayPadPlugin.getInstance().getSettingsListener().forEach(l ->
			{
				try {
					l.onLoad(profile);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});

			// Mapping erst danach, weil das auf current Profile zugreifen muss
			profile.mappings = MappingList.load(app.getPath(PathType.CONFIGURATION, ref.getFileName(), MAPPING_XML), profile);

			setCurrentProfile(profile);

			return profile;
		}
		throw new ProfileNotFoundException(ref);
	}

	public void save() throws IOException {
		ref.getRequestedModules().clear();

		App app = ApplicationUtils.getApplication();

		Path root = app.getPath(PathType.CONFIGURATION, ref.getFileName());
		if (Files.notExists(root)) {
			Files.createDirectories(root);
		}

		PlayPadPlugin.getInstance().getSettingsListener().forEach(l -> {
			try {
				l.onSave(this);
			} catch (Exception ex) {
				Logger.error(ex);
			}
		});

		// Add audio settings to module list
		ref.addRequestedModule(PlayPadPlugin.getRegistries().getAudioHandlers().getModule(profileSettings.getAudioClass()));

		profileSettings.save(getProfilePath(PROFILE_SETTINGS_XML));
		mappings.save(getProfilePath(MAPPING_XML));
	}

	private Path getProfilePath(String fileName) {
		App app = ApplicationUtils.getApplication();
		return app.getPath(PathType.CONFIGURATION, ref.getFileName(), fileName);
	}

	@Override
	public String toString() {
		return ref.getName();
	}
}
