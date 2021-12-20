package de.tobias.playpad.profile;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.MappingCollection;
import de.thecodelabs.midi.serialize.MappingCollectionSerializer;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.registry.Registry;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Profile {

	private static final String PROFILE_SETTINGS_XML = "ProfileSettings.xml";
	private static final String MAPPING_JSON = "Mapping.json";

	private static List<ProfileListener> listeners = new ArrayList<>();
	private static Profile currentProfile;

	// Settings
	private final ProfileReference ref;

	private ProfileSettings profileSettings;
	private MappingCollection mappings;

	private Map<String, Object> customSettings;

	/**
	 * Use {@link ProfileReferenceManager#addProfile(ProfileReference)} instead
	 *
	 * @param ref Ref
	 */
	public Profile(ProfileReference ref) {
		this.ref = ref;
		this.profileSettings = new ProfileSettings();
		this.mappings = new MappingCollection();
		this.customSettings = new HashMap<>();
	}

	public static Mapping createMappingWithDefaultActions() {
		Mapping preset = new Mapping();
		preset.setName("Default");

		// Add default actions
		final Registry<ActionProvider> actions = PlayPadPlugin.getRegistries().getActions();
		actions.getComponents().forEach(provider -> provider.createDefaultActions(preset));
		return preset;
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

	public MappingCollection getMappings() {
		return mappings;
	}

	public ProfileSettings getProfileSettings() {
		return profileSettings;
	}

	public Object getCustomSettings(String name) {
		return customSettings.get(name);
	}

	public void addCustomSettings(String name, Object settings) {
		customSettings.put(name, settings);
	}

	public static Profile load(ProfileReference ref) throws DocumentException, IOException, ProfileNotFoundException {
		if (ref == null) {
			throw new IllegalArgumentException("Profile is null");
		}
		// Altes Speichern bevor neues Geladen
		if (currentProfile != null) {
			currentProfile.save();
		}

		// Dont load profile, that is currently loaded
		if (currentProfile != null && currentProfile.getRef().equals(ref)) {
			return currentProfile;
		}

		App app = ApplicationUtils.getApplication();
		Profile profile = new Profile(ref);

		Logger.info("+++ Load Profile: " + ref + " (" + ref.getUuid() + ") +++");

		if (Files.exists(app.getPath(PathType.CONFIGURATION, ref.getFileName()))) {

			profile.profileSettings = ProfileSettings.load(app.getPath(PathType.CONFIGURATION, ref.getFileName(), PROFILE_SETTINGS_XML));

			// Listener
			PlayPadPlugin.getInstance().getSettingsListener().forEach(l ->
			{
				try {
					l.onLoad(profile);
				} catch (Exception ex) {
					Logger.error(ex);
				}
			});

			// Mapping erst danach, weil das auf current Profile zugreifen muss
			try {
				profile.mappings = MappingCollectionSerializer.load(app.getPath(PathType.CONFIGURATION, ref.getFileName(), MAPPING_JSON));
			} catch (IOException e) {
				profile.mappings = new MappingCollection();
			}

			// Set current mapping
			final Optional<Mapping> activeMapping = profile.getMappings().getActiveMapping();
			final Optional<Mapping> anyMapping = profile.mappings.getMappings().stream().findAny();
			final Mapping currentMapping = activeMapping.orElse(anyMapping.orElseGet(() -> {
				final Mapping mapping = createMappingWithDefaultActions();
				profile.mappings.addMapping(mapping);
				return mapping;
			}));
			profile.mappings.setActiveMapping(currentMapping);
			Mapping.setCurrentMapping(currentMapping);

			setCurrentProfile(profile);

			// Update mapping with new actions
			final Registry<ActionProvider> actions = PlayPadPlugin.getRegistries().getActions();
			actions.getComponents().forEach(provider -> provider.createDefaultActions(currentMapping));

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
		MappingCollectionSerializer.save(mappings, getProfilePath(MAPPING_JSON));
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
