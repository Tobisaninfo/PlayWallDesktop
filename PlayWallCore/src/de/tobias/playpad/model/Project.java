package de.tobias.playpad.model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.sun.nio.zipfs.ZipFileSystem;

import de.tobias.playpad.model.Pad.TimeMode;
import de.tobias.playpad.model.layout.CartLayout;
import de.tobias.playpad.model.layout.LayoutRegistry;
import de.tobias.playpad.model.midi.Displayable;
import de.tobias.playpad.model.settings.Warning;
import de.tobias.playpad.pad.Fade;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.Pad.PadStatus;
import de.tobias.playpad.plugin.PlayPadPlugin;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.settings.UserDefaults;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Project implements Displayable {

	private StringProperty displayProperty = new SimpleStringProperty();

	private Profile profile;

	private HashMap<Integer, Pad> pads = new HashMap<>();
	private ObjectProperty<String> nameProperty;
	private ProjectReference projectReference;

	public Project(Profile profile, ProjectReference projectReference) {
		this.profile = profile;
		this.projectReference = projectReference;
		this.nameProperty = new SimpleObjectProperty<>(projectReference.getName());
	}

	public Pad getPad(int index) {
		Pad pad = pads.get(index);
		if (pad != null) {
			return pad;
		} else {
			pad = new Pad(index, this);
			pads.put(index, pad);
			return pad;
		}
	}

	public void setPad(Pad pad, int index) {
		this.pads.put(index, pad);
	}

	public HashMap<Integer, Pad> getPads() {
		return pads;
	}

	public int size() {
		return pads.size();
	}

	public void add(Pad pad) {
		pads.put(pad.getIndex(), pad);
	}

	public String getName() {
		return nameProperty.get();
	}

	public void setName(String name) throws Exception {
		Path oldPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, nameProperty.get());
		Path newPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, name);

		projectReference.setName(name);
		this.nameProperty.set(name);

		// Move File on Disk
		Files.move(oldPath, newPath);
	}

	public ProjectReference getProjectReference() {
		return projectReference;
	}

	public ObjectProperty<String> nameProperty() {
		return nameProperty;
	}

	public void clearUnneededPads() {
		ProfileSettings profilSettings = Profile.currentProfile().getProfileSettings();
		int maxPads = profilSettings.getPageCount() * profilSettings.getColumns() * profilSettings.getRows();

		for (int i = maxPads; i < pads.size(); i++) {
			Pad pad = getPad(i);
			pad.clearPad();
		}
		List<Integer> removeList = new ArrayList<>();
		for (int key : pads.keySet()) {
			if (key >= maxPads)
				removeList.add(key);
		}
		removeList.forEach(pads::remove);
	}

	public void clearPads() {
		pads.values().forEach(pad ->
		{
			pad.clearPad();
		});
		pads.clear();
	}

	/**
	 * Öffnet Project und Profile
	 * 
	 * @param name
	 * @param request
	 * @param profileName
	 * @return
	 * @throws DocumentException
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	public static Project loadFile(ProjectReference projectReference, ProfileChooseable request, String profileName)
			throws DocumentException, IOException, IllegalArgumentException, InstantiationException, IllegalAccessException {
		return loadFile(projectReference, request, profileName, true);
	}

	/**
	 * Öffnet Project und Profile (name + XML)
	 * 
	 * @param name
	 * @param request
	 * @param profileName
	 * @param loadMedia
	 * @return
	 * @throws DocumentException
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	public static Project loadFile(ProjectReference projectReference, ProfileChooseable request, String profileName, boolean loadMedia)
			throws DocumentException, IOException, IllegalArgumentException, InstantiationException, IllegalAccessException {
		Path path = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, projectReference.getName());

		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(path));
		Element root = document.getRootElement();

		// Lädt das Profile
		Profile profile = null;
		if (profileName != null) {
			profile = Profile.load(profileName);
		} else {
			Element profileElement = root.element("Profile");
			if (profileElement != null) {
				if (Profile.getProfiles().contains(profileElement.getStringValue())) {
					profile = Profile.load(profileElement.getStringValue());
				}
			}
		}
		// Wenn Profile laden nicht ging
		if (profile == null) {
			if (request != null)
				profile = request.getUnkownProfile();
			else
				profile = Profile.load(Profile.getProfiles().get(0));
		}

		Project project = new Project(profile, projectReference);

		for (Object element : root.elements("Pad")) {
			Element padElement = (Element) element;
			int index = Integer.valueOf(padElement.attribute("index").getValue()); // Index
			PadStatus state = PadStatus.valueOf(padElement.element("State").getStringValue()); // State

			Element titleElement = padElement.element("Title");
			Element pathElement = padElement.element("Path");

			Pad pad;

			if (titleElement != null && pathElement != null) {
				pad = new Pad(index, pathElement.getStringValue(), titleElement.getStringValue(), state, loadMedia, project);
			} else {
				pad = new Pad(index, project);
			}

			double volume = padElement.element("Volume") != null ? Double.valueOf(padElement.element("Volume").getStringValue()) : 1.0; // Volume
			pad.setVolume(volume);

			if (padElement.element("Loop") != null) {
				boolean loop = Boolean.valueOf(padElement.element("Loop").getStringValue());
				pad.setLoop(loop);
			}

			Element timeModeElement = padElement.element("TimeMode");
			if (timeModeElement != null) {
				de.tobias.playpad.pad.Pad.TimeMode timeMode = TimeMode.valueOf(timeModeElement.getStringValue());
				pad.setTimeMode(timeMode);
			}

			Element fadeElement = padElement.element("Fade");
			if (fadeElement != null) {
				Optional<Fade> fade = Fade.load(fadeElement);
				if (fade.isPresent())
					pad.setFade(fade.get());
			}

			Element layoutElement = padElement.element("Layout");
			// Old Version
			if (layoutElement != null) {
				int version = Integer.valueOf(layoutElement.attributeValue("version"));
				if (version == 2) {
					for (Object layoutObj : layoutElement.elements("CartLayout")) {
						try {
							Element cartLayoutElement = (Element) layoutObj;
							String type = cartLayoutElement.attributeValue("type");

							CartLayout layout = LayoutRegistry.newCartLayoutInstance(type);
							layout.load(cartLayoutElement);

							pad.setLayout(type, layout);
							pad.setCustomLayout(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			Element warningElement = padElement.element("Warning");
			if (warningElement != null) {
				Optional<Warning> warningFeedback = Warning.loadV2(warningElement);
				if (warningFeedback.isPresent()) {
					pad.setWarningFeedback(warningFeedback.get());
				}
			}

			Element userInfoElement = padElement.element("UserInfo");
			if (userInfoElement != null) {
				for (Object object : userInfoElement.elements()) {
					if (object instanceof Element) {
						Element item = (Element) object;
						String key = item.attributeValue("key");
						if (item.attribute("type") == null) {
							// Old
							String value = item.getStringValue();
							pad.getUserInfo().put(key, value);
						} else {
							Object data = UserDefaults.loadElement(item);
							pad.getUserInfo().put(key, data);
						}
					}
				}
			}

			project.add(pad);
		}

		project.displayProperty.set(project.toString());

		// Mit Leeren Pads auffüllen
		ProfileSettings profilSettings = Profile.currentProfile().getProfileSettings();
		for (int i = 0; i < profilSettings.getPageCount() * profilSettings.getColumns() * profilSettings.getRows() - 1; i++) {
			project.getPad(i);
		}

		System.out.println("Load Project: " + project.getName());

		return project;
	}

	public void saveFile() throws UnsupportedEncodingException, IOException {
		Path path = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, getName());

		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Pads");

		root.addElement("Profile").addText(profile.getName());

		for (Pad pad : pads.values()) {
			if (pad.getState() == State.PLAY || pad.getState() == State.PAUSE || pad.getState() == State.STOP) {
				pad.setState(State.READY);
			}

			PlayPadPlugin.getImplementation().getPadListener().forEach(l -> l.onSave(pad));

			if (pad.getState() != State.EMPTY) {
				Element padElement = root.addElement("Pad");
				padElement.addAttribute("index", String.valueOf(pad.getIndex()));

				// Basic
				if (pad.getFile() != null) {
					Element pathElement = padElement.addElement("Path");
					pathElement.addText(pad.getFile());
				}

				Element titleElement = padElement.addElement("Title");
				titleElement.addText(pad.getTitle());

				Element stateElement = padElement.addElement("State");
				stateElement.addText(pad.getState().name());

				// Einstellungen
				padElement.addElement("Volume").addText(String.valueOf(pad.getVolume()));
				padElement.addElement("Loop").addText(String.valueOf(pad.isLoop()));

				if (pad.isCustomTimeMode())
					padElement.addElement("TimeMode").addText(pad.getTimeMode().get().name());

				if (pad.isCustomWarning()) {
					Element warningElement = padElement.addElement("Warning");
					pad.getWarningFeedback().get().save(warningElement);
				}

				if (pad.isCustomLayout()) {
					Element layoutElement = padElement.addElement("Layout");
					layoutElement.addAttribute("version", "2");
					for (String layoutType : pad.getLayouts().keySet()) {
						Optional<CartLayout> layoutOpt = pad.getLayout(layoutType);

						layoutOpt.ifPresent(layout ->
						{
							Element cartLayoutElement = layoutElement.addElement("CartLayout");
							cartLayoutElement.addAttribute("type", layoutType);
							layout.save(cartLayoutElement);
						});
					}
				}

				if (pad.isCustomFade()) {
					Element fadeElement = padElement.addElement("Fade");
					pad.getFade().get().save(fadeElement);
				}

				Element userInfoElement = padElement.addElement("UserInfo");
				for (String key : pad.getUserInfo().keySet()) {
					Element itemElement = userInfoElement.addElement("Item");
					UserDefaults.save(itemElement, pad.getUserInfo().get(key), key);
				}
			} else {
				if (pad.isCustomLayout()) {
					Element padElement = root.addElement("Pad");
					padElement.addAttribute("index", String.valueOf(pad.getIndex()));

					Element stateElement = padElement.addElement("State");
					stateElement.addText(pad.getState().name());

					Element layoutElement = padElement.addElement("Layout");
					layoutElement.addAttribute("version", "2");
					for (String layoutType : pad.getLayouts().keySet()) {
						Optional<CartLayout> layoutOpt = pad.getLayout(layoutType);

						layoutOpt.ifPresent(layout ->
						{
							Element cartLayoutElement = layoutElement.addElement("CartLayout");
							cartLayoutElement.addAttribute("type", layoutType);
							layout.save(cartLayoutElement);
						});
					}
				}
			}
		}

		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	public Profile getProfile() {
		return profile;
	}

	public void replace(int src, int des) {
		Pad srcPad = getPad(src);
		srcPad.setIndex(des);
		pads.put(des, srcPad);
		pads.remove(src);
	}

	public void move(int src, int des) {
		Pad oldPad = getPad(src);
		Pad newPad = getPad(des);

		oldPad.setIndex(des);
		newPad.setIndex(src);

		pads.put(des, oldPad);
		pads.put(src, newPad);
	}

	@Override
	public String toString() {
		return nameProperty.get();
	}

	@Override
	public StringProperty displayProperty() {
		return displayProperty;
	}

	public void exportProject(Path zipFile, boolean includeProfile, boolean includeMedia) throws IOException {
		saveFile();

		URI p = Paths.get(zipFile.toString()).toUri();
		URI uri = URI.create("jar:" + p);

		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		env.put("encoding", "UTF-8");

		// Delete Esisting Zip File
		if (Files.exists(zipFile))
			Files.delete(zipFile);

		FileSystem zipfs = FileSystems.newFileSystem(uri, env);

		// Info Datei
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Info");
		root.addElement("Project").addText(nameProperty.get());
		if (includeProfile)
			root.addElement("Profile").addText(profile.getName());

		XMLWriter writer = new XMLWriter(Files.newOutputStream(zipfs.getPath("Info.xml")), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();

		// Profile
		if (includeMedia) {
			Path profilePath = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, profile.getName());
			Files.copy(profilePath, zipfs.getPath(profile.getName()), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(profilePath.resolve("ProfileSettings.xml"), zipfs.getPath(profile.getName(), "ProfileSettings.xml"),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(profilePath.resolve("Midi.xml"), zipfs.getPath(profile.getName(), "Midi.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(profilePath.resolve("Layout.xml"), zipfs.getPath(profile.getName(), "Layout.xml"), StandardCopyOption.REPLACE_EXISTING);
		}

		// Mediafiles
		if (includeMedia) {
			Path mediaFolder = zipfs.getPath("/media");
			Files.createDirectory(mediaFolder);

			for (Pad pad : pads.values()) {
				if (pad.isPadLoaded())
					try {
						Path mediaPath = pad.getPath();
						Files.copy(mediaPath, mediaFolder.resolve(mediaPath.getFileName().toString()));
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}

		// Project
		Path projectFile = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, getName());
		Files.copy(projectFile, zipfs.getPath("/", nameProperty.get()), StandardCopyOption.REPLACE_EXISTING);

		zipfs.close();
	}

	public static Project importProject(Path zipFile, Importable replaceRequest)
			throws DocumentException, IOException, IllegalArgumentException, InstantiationException, IllegalAccessException {
		ZipFileSystem fileSystem = (ZipFileSystem) FileSystems.newFileSystem(zipFile, null);

		Path infoPath = fileSystem.getPath("/Info.xml");
		if (Files.exists(infoPath)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(infoPath));
			Element root = document.getRootElement();

			if (root.element("Project") != null) {
				String projectFile = root.element("Project").getStringValue();

				// Profile Einstellungen
				String importProfile = null;
				if (root.element("Profile") != null) {
					String profile = root.element("Profile").getStringValue();

					// Profile
					Path profilePath = fileSystem.getPath("/", profile);
					Path profilePathDes = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, profile);

					importProfile = profile;
					if (Files.exists(profilePathDes)) {
						importProfile = replaceRequest.replaceProfile(profile);
					}
					if (importProfile != null) {
						profilePathDes = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, importProfile);
						System.out.println("Copy Profile: " + profilePathDes);

						if (Files.exists(profilePath)) {
							if (Files.notExists(profilePathDes))
								Files.createDirectories(profilePathDes);

							Files.copy(profilePath.resolve("ProfileSettings.xml"), profilePathDes.resolve("ProfileSettings.xml"),
									StandardCopyOption.REPLACE_EXISTING);
							Files.copy(profilePath.resolve("Midi.xml"), profilePathDes.resolve("Midi.xml"), StandardCopyOption.REPLACE_EXISTING);
							Files.copy(profilePath.resolve("Layout.xml"), profilePathDes.resolve("Layout.xml"),
									StandardCopyOption.REPLACE_EXISTING);

							Profile.getProfiles().add(importProfile);
						}
					}
				}

				// Media Data Copy
				boolean customMediaFolder = true;
				Path mediaFolder = null;
				if (Files.exists(fileSystem.getPath("/media"))) {
					mediaFolder = replaceRequest.mediaFolder();
					if (mediaFolder != null) {
						Stream<Path> stream = Files.list(fileSystem.getPath("/media"));
						for (Object obj : stream.toArray()) {
							Path path = (Path) obj;
							Path desPath = Paths.get(mediaFolder.toString(), path.getFileName().toString());
							if (Files.notExists(desPath)) {
								Files.copy(path, desPath);
							}
						}
						stream.close();
					}
				}

				// Project
				Path projectPath = fileSystem.getPath("/" + projectFile);
				Path des = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, projectFile);

				String importProject = projectFile;
				if (Files.exists(des)) {
					importProject = replaceRequest.replaceProject(projectFile);
					// Stop Import, da kein Name vom Nutzer angegeben wurde
					if (importProject == null) {
						return null;
					}

					// Add XML to FileName
					if (!importProject.matches("." + PlayPadPlugin.getImplementation().getProjectFiles()[0])) {
						importProject += PlayPadPlugin.getImplementation().getProjectFiles()[0].substring(1);
					}
				} else {
					Files.createDirectories(des.getParent());
					Files.createFile(des);
				}

				des = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, importProject);

				if (Files.exists(projectPath)) {
					System.out.println("Copy Project: " + des);

					Files.copy(projectPath, des, StandardCopyOption.REPLACE_EXISTING);
					fileSystem.close();

					ProjectReference importReference = new ProjectReference(importProject);
					Project project = loadFile(importReference, null, importProfile, false);
					ProjectReference.addProject(importReference);

					for (Pad pad : project.getPads().values()) {
						if (pad.hasMedia() && customMediaFolder) {
							try {
								Path path = Paths.get(mediaFolder.toString(), pad.getFileName());
								pad.setPath(path, false);
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
						}
						pad.loadMedia();
					}
					return project;
				}
			}
		}
		return null;
	}

	public interface Importable {

		public String replaceProfile(String name);

		public String replaceProject(String name);

		public Path mediaFolder();
	}

	public interface ProfileChooseable {

		public Profile getUnkownProfile();
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public void loadAudio() {
		for (Pad pad : pads.values()) {
			if (pad.isPadLoaded()) {
				pad.getAudioHandler().unloadMedia(pad);
			}
			pad.loadMedia();
		}
	}

	public static void duplicate(ProjectReference currentProject, ProjectReference newProjectReference) throws IOException {
		ProjectReference.addProject(newProjectReference);

		Path oldPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, currentProject.getName());
		Path newPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, newProjectReference.getName());

		Files.copy(oldPath, newPath, StandardCopyOption.COPY_ATTRIBUTES);
		Profile.saveProfiles();
	}
}
