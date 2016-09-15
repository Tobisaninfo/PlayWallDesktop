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
import de.tobias.playpad.design.DesignConnect;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

public class Profile {

	private static final String PROFILE_SETTINGS_XML = "ProfileSettings.xml";
	private static final String MAPPING_XML = "Mapping.xml";
	private static final String LAYOUT_XML = "Layout.xml";

	public static final String profileNameEx = "[\\p{L}0-9]{1}[\\p{L}\\s-_0-9]{0,}";

	private static List<ProfileListener> listeners = new ArrayList<>();
	private static Profile currentProfile;

	// Settings
	private ProfileReference ref;

	private ProfileSettings profileSettings;
	private MappingList mappings;
	private HashMap<String, GlobalDesign> layouts;

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

	public HashMap<String, GlobalDesign> getLayouts() {
		return layouts;
	}

	public GlobalDesign getLayout(String type) {
		if (layouts.containsKey(type)) {
			return layouts.get(type);
		} else {
			try {
				DefaultRegistry<DesignConnect> registry = PlayPadPlugin.getRegistryCollection().getDesigns();
				GlobalDesign layout = registry.getComponent(type).newGlobalDesign();
				layouts.put(type, layout);
				return layout;
			} catch (NoSuchComponentException e) { // -> Throw exception
				// TODO Error Handling
				e.printStackTrace();
			}
		}
		return null;
	}

	public GlobalDesign currentLayout() {
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

		System.out.println("+++ Load Profile: " + ref + " (" + ref.getUuid() + ") +++");

		if (Files.exists(app.getPath(PathType.CONFIGURATION, ref.getFileName()))) {

			ProfileSettings profileSettings = ProfileSettings.load(app.getPath(PathType.CONFIGURATION, ref.getFileName(), PROFILE_SETTINGS_XML));
			HashMap<String, GlobalDesign> layouts = GlobalDesign
					.loadGlobalLayout(app.getPath(PathType.CONFIGURATION, ref.getFileName(), LAYOUT_XML));

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
			MappingList mappings = MappingList.load(app.getPath(PathType.CONFIGURATION, ref.getFileName(), MAPPING_XML), profile);
			profile.mappings = mappings;

			setCurrentProfile(profile);

			return profile;
		}
		throw new ProfileNotFoundException(ref);
	}

	public void save() throws UnsupportedEncodingException, IOException {
		ref.getRequestedModules().clear();
		
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

		profileSettings.save(getProfilePath(PROFILE_SETTINGS_XML));
		mappings.save(getProfilePath(MAPPING_XML));
		GlobalDesign.saveGlobal(layouts, getProfilePath(LAYOUT_XML));
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
