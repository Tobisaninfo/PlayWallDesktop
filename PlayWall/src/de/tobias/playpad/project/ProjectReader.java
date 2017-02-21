package de.tobias.playpad.project;

import de.tobias.playpad.pad.MediaPath;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.ref.ProjectReference;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Created by tobias on 21.02.17.
 */
public class ProjectReader {

	public Project read(ProjectReference ref, JSONObject object) {
		Project project = new Project(ref);

		JSONArray jsonPages = object.getJSONArray("pages");
		for (int i = 0; i < jsonPages.length(); i++) {
			JSONObject pageObject = jsonPages.getJSONObject(i);
			Page page = readPage(project, pageObject);
			project.pages.add(page);
		}
		return project;
	}

	private Page readPage(Project project, JSONObject object) {
		UUID id = UUID.fromString(object.getString("id"));
		String name = object.getString("name");
		int position = object.getInt("position");

		Page page = new Page(id, position, name, project);

		JSONArray jsonPads = object.getJSONArray("pads");
		for (int i = 0; i < jsonPads.length(); i++) {
			Pad pad = readPad(project, page, jsonPads.getJSONObject(i));
			page.setPad(pad.getPosition(), pad);
		}

		return page;
	}

	private Pad readPad(Project project, Page page, JSONObject object) {
		UUID id = UUID.fromString(object.getString("id"));
		String name = object.getString("name");
		int position = object.getInt("position");
		String contentType = object.getString("contentType");

		Pad pad = new Pad(project, position, page, name, contentType);

		JSONArray jsonPaths = object.getJSONArray("paths");
		for (int i = 0; i < jsonPaths.length(); i++) {
			MediaPath path = readPath(pad, jsonPaths.getJSONObject(i));
			pad.getPaths().add(path); // TODO Use addPath Method in right scope
		}

		return pad;
	}

	private MediaPath readPath(Pad pad, JSONObject object) {
		UUID id = UUID.fromString(object.getString("id"));
		Path path = Paths.get(object.getString("path"));

		return new MediaPath(id, path, pad);
	}
}
