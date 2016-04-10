package de.tobias.playpad.settings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.DocumentException;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.MappingList;
import de.tobias.playpad.layout.GlobalLayout;
import de.tobias.playpad.layout.LayoutRegistry;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

public class Profile {

	public static final String profileNameEx = "\\w{1}[\\w\\s-_]{0,}";

	private static List<ProfileListener> listeners = new ArrayList<>();
	private static Profile currentProfile;

	// Settings
	private ProfileReference ref;

	private ProfileSettings profileSettings;
	private MappingList mappings;
	private HashMap<String, GlobalLayout> layouts;

	Profile(ProfileReference ref) {
		this.ref = ref;
		this.profileSettings = new ProfileSettings();
		this.mappings = new MappingList(this);
		this.layouts = new HashMap<>();
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

	public HashMap<String, GlobalLayout> getLayouts() {
		return layouts;
	}

	public GlobalLayout getLayout(String type) {
		if (layouts.containsKey(type)) {
			return layouts.get(type);
		} else {
			GlobalLayout layout = LayoutRegistry.getLayout(type).newGlobalLayout();
			layouts.put(type, layout);
			return layout;
		}
	}

	public GlobalLayout currentLayout() {
		return getLayout(profileSettings.getLayoutType());
	}

	public MappingList getMappings() {
		return mappings;
	}

	public ProfileSettings getProfileSettings() {
		return profileSettings;
	}

	public static Profile load(ProfileReference ref) throws DocumentException, IOException, ProfileNotFoundException {
		// Altes Speichern bevor neues Geladen
		if (currentProfile != null)
			currentProfile.save();

		App app = ApplicationUtils.getApplication();
		Profile profile = new Profile(ref);

		System.out.println("+++ Load Profile: " + ref + " +++");

		if (Files.exists(app.getPath(PathType.CONFIGURATION, ref.getFileName()))) {

			ProfileSettings profileSettings = ProfileSettings
					.load(app.getPath(PathType.CONFIGURATION, ref.getFileName(), "ProfileSettings.xml"));
			HashMap<String, GlobalLayout> layouts = GlobalLayout
					.loadGlobalLayout(app.getPath(PathType.CONFIGURATION, ref.getFileName(), "Layout.xml"));

			profile.profileSettings = profileSettings;
			profile.layouts = layouts;

			// Listener
			PlayPadPlugin.getImplementation().getSettingsListener().forEach(l ->
			{
				try {
					l.onLoad(profile);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});

			// Mapping erst danach, weil das auf current Profile zugreifen muss
			MappingList mappings = MappingList.load(app.getPath(PathType.CONFIGURATION, ref.getFileName(), "Mapping.xml"), profile);
			profile.mappings = mappings;

			setCurrentProfile(profile);

			return profile;
		}
		throw new ProfileNotFoundException(ref);
	}

	public void save() throws UnsupportedEncodingException, IOException {
		PlayPadPlugin.getImplementation().getSettingsListener().forEach(l ->
		{
			try {
				l.onSave(this);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		App app = ApplicationUtils.getApplication();

		Path root = app.getPath(PathType.CONFIGURATION, ref.getFileName());
		if (Files.notExists(root))
			Files.createDirectories(root);

		profileSettings.save(app.getPath(PathType.CONFIGURATION, ref.getFileName(), "ProfileSettings.xml"));
		mappings.save(app.getPath(PathType.CONFIGURATION, ref.getFileName(), "Mapping.xml"));
		GlobalLayout.saveGlobal(layouts, app.getPath(PathType.CONFIGURATION, ref.getFileName(), "Layout.xml"));
	}

	@Override
	public String toString() {
		return ref.getName();
	}
}
