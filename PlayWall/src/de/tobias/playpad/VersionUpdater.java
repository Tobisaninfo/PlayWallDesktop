package de.tobias.playpad;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.cartaction.CartAction;
import de.tobias.playpad.action.cartaction.CartAction.ControlMode;
import de.tobias.playpad.action.feedback.DoubleSimpleFeedback;
import de.tobias.playpad.action.mapper.MidiMapper;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.application.update.UpdateService;

public class VersionUpdater implements UpdateService {

	@Override
	public void update(App app, long oldVersion, long newVersion) {
		System.out.println("Update");
		if (oldVersion <= 18) {
			SAXReader reader = new SAXReader();
			Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "Profiles.xml");

			try {
				Document document = reader.read(Files.newInputStream(path));
				Element root = document.getRootElement();
				for (Object object : root.elements("Document")) {
					Element element = (Element) object;
					String name = element.getStringValue();

					UUID uuid = UUID.randomUUID();

					Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, name);
					Path newProjectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, uuid + Project.FILE_EXTENSION);
					Files.createDirectories(newProjectPath.getParent());

					Files.move(projectPath, newProjectPath);

					ProjectReference projectReference = new ProjectReference(uuid, name, ProfileReference.getProfiles().get(0));
					ProjectReference.addProject(projectReference);

					convertProject(newProjectPath);
				}

				ProjectReference.saveProjects();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (oldVersion <= 23) {
			System.out.println("Update Data");
			Path configPath = app.getPath(PathType.CONFIGURATION);
			SAXReader reader = new SAXReader();
			try {
				Document document = reader.read(Files.newInputStream(configPath.resolve("Profiles.xml")));
				for (Object profileObj : document.getRootElement().elements("Profile")) {
					if (profileObj instanceof Element) {
						String name = ((Element) profileObj).getStringValue();
						System.out.println("Start Profile: " + name);
						UUID uuid = UUID.randomUUID();

						Path profileOldPath = app.getPath(PathType.CONFIGURATION, name);
						Path profileNewPath = app.getPath(PathType.CONFIGURATION, uuid.toString());

						Files.move(profileOldPath, profileNewPath);

						convertMidiToMapping(profileNewPath);

						ProfileReference profileReference = new ProfileReference(uuid, name);
						ProfileReference.addProfile(profileReference);
						System.out.println("Finish Profile: " + name + " (" + uuid + ")");
					}
				}
			} catch (DocumentException | IOException e1) {
				e1.printStackTrace();
			}

			try {
				Document document = reader.read(Files.newInputStream(configPath.resolve("Projects.xml")));
				for (Object projectObj : document.getRootElement().elements("Project")) {
					if (projectObj instanceof Element) {
						try {
							String name = ((Element) projectObj).getStringValue();
							Path projectPath = app.getPath(PathType.DOCUMENTS, name);
							System.out.println("Start Project: " + projectPath);

							UUID uuid = UUID.randomUUID();
							Path newProjectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, uuid + Project.FILE_EXTENSION);

							Files.move(projectPath, newProjectPath);

							ProjectReference projectReference = new ProjectReference(uuid, projectPath.getFileName().toString(),
									ProfileReference.getProfiles().get(0));
							ProjectReference.addProject(projectReference);

							convertProject(newProjectPath);
							System.out.println("End Project: " + projectPath + " (" + uuid + ")");
						} catch (DocumentException | URISyntaxException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (IOException | DocumentException e) {
				e.printStackTrace();
			}
			try {
				ProfileReference.saveProfiles();
				ProjectReference.saveProjects();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void convertMidiToMapping(Path configPath) throws DocumentException, IOException {
		SAXReader reader = new SAXReader();
		Document midiDocument = reader.read(Files.newInputStream(configPath.resolve("Midi.xml")));

		List<Mapping> mappings = new ArrayList<>();

		for (Object presetObj : midiDocument.getRootElement().elements("Preset")) {
			Element presetElement = (Element) presetObj;
			String name = presetElement.attributeValue("name");

			Mapping mapping = new Mapping(false, null);
			mapping.setName(name);
			mapping.setUuid(UUID.randomUUID());

			for (Object midiObj : presetElement.elements("Midi")) {
				Element midiElement = (Element) midiObj;
				int command = Integer.valueOf(midiElement.attributeValue("command"));
				int key = Integer.valueOf(midiElement.attributeValue("channel"));

				if (midiElement.attributeValue("type") != null) {
					if (midiElement.attributeValue("type").equals("de.tobias.playpad.model.midi.type.PlayStopActionType")) {
						Element actionElement = midiElement.element("Action");
						if (actionElement.attributeValue("class").equals("de.tobias.playpad.model.midi.CartAction")) {
							int cart = Integer.valueOf(actionElement.element("CartID").getStringValue());

							CartAction action = new CartAction(cart, ControlMode.PLAY_STOP);
							mapping.addActionIfNotContains(action);

							MidiMapper mapper = new MidiMapper(command, key);
							action.addMapper(mapper);

							Element feedbackEventElement = actionElement.element("Feedback");
							Element feedbackDefaultElement = midiElement.element("Feedback");

							int feedbackEvent = Integer.valueOf(feedbackEventElement.element("MidiVelocity").getStringValue());
							int feedbackDefault = Integer.valueOf(feedbackDefaultElement.element("MidiVelocity").getStringValue());

							DoubleSimpleFeedback doubleSimpleFeedback = new DoubleSimpleFeedback(feedbackDefault, feedbackEvent);
							mapper.setFeedback(doubleSimpleFeedback);
						}
					}
				}
			}
			mappings.add(mapping);
		}

		Document mappingDocument = DocumentHelper.createDocument();
		Element rootElement = mappingDocument.addElement("List");

		for (Mapping mapping : mappings) {
			Element mappingElement = rootElement.addElement("Mapping");
			mapping.save(mappingElement);
		}

		XMLWriter writer = new XMLWriter(Files.newOutputStream(configPath.resolve("Mapping.xml")), OutputFormat.createPrettyPrint());
		writer.write(mappingDocument);
		writer.close();

		Files.delete(configPath.resolve("Midi.xml"));
	}

	public void convertProject(Path path) throws DocumentException, IOException, URISyntaxException {
		SAXReader reader = new SAXReader();
		Document oldDocument = reader.read(Files.newInputStream(path));
		Document newDocument = DocumentHelper.createDocument();

		Element newRootElement = newDocument.addElement("Project");

		for (Object oldPadObj : oldDocument.getRootElement().elements("Pad")) {
			try {
				Element oldPadElement = (Element) oldPadObj;
				Element newPadElement = newRootElement.addElement("Pad");

				newPadElement.addAttribute("index", oldPadElement.attributeValue("index"));
				newPadElement.addAttribute("name", oldPadElement.element("Title").getStringValue());
				newPadElement.addAttribute("status", oldPadElement.element("State").getStringValue());

				String file = oldPadElement.element("Path").getStringValue();
				URI uri = new URI(file);
				newPadElement.addElement("Content").addAttribute("type", "audio").addText(Paths.get(uri).toString());

				Element newSettingsElement = newPadElement.addElement("Settings");
				newSettingsElement.addElement("Volume").addText(oldPadElement.element("Volume").getStringValue());
				newSettingsElement.addElement("Loop").addText(oldPadElement.element("Loop").getStringValue());
				if (oldPadElement.element("TimeMode") != null)
					newSettingsElement.addElement("TimeMode").addText(oldPadElement.element("TimeMode").getStringValue());
			} catch (Exception e) {}
		}

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(newDocument);
		writer.close();
	}
}
