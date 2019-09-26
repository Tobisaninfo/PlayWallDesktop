package de.tobias.playpad.project;

import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.ref.ProjectReference;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by tobias on 21.02.17.
 */
public class ProjectJsonReader {

	private JSONObject object;

	public ProjectJsonReader(JSONObject data) {
		this.object = data;
	}

	public Project read(ProjectReference ref) {
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
			JSONObject jsonObject = jsonPads.getJSONObject(i);
			Pad pad = readPad(project, page, jsonObject);
			page.setPad(pad.getPosition(), pad);
		}

		return page;
	}

	private Pad readPad(Project project, Page page, JSONObject object) {
		UUID id = UUID.fromString(object.getString("id"));
		String name = object.getString("name");
		int position = object.getInt("position");

		String contentType = null;
		if (!object.isNull("contentType")) {
			contentType = object.getString("contentType");
		}

		Pad pad = new Pad(project, id, position, page, name, contentType);

		JSONArray jsonPaths = object.getJSONArray("paths");
		for (int i = 0; i < jsonPaths.length(); i++) {
			MediaPath path = readPath(pad, jsonPaths.getJSONObject(i));
			pad.getPaths().add(path); // TODO Use addPath Method in right scope
		}

		if (!pad.getPaths().isEmpty()) {
			pad.setStatus(PadStatus.READY);
		}

		if (!object.isNull("design")) {
			ModernCartDesign design = readModernCartDesign(pad, object.getJSONObject("design"));
			if (design != null) {
				pad.getPadSettings().setDesign(design);
				pad.getPadSettings().setCustomDesign(true); // TODO Sync
			}
		}

		return pad;
	}

	private MediaPath readPath(Pad pad, JSONObject object) {
		UUID id = UUID.fromString(object.getString("id"));
		String filename = null;
		if (!object.isNull("filename")) {
			filename = object.getString("filename");
		}

		return new MediaPath(id, filename, pad);
	}

	private ModernCartDesign readModernCartDesign(Pad pad, JSONObject object) {
		if (!object.isNull("id")) {
			UUID id = UUID.fromString(object.getString("id"));
			ModernColor backgroundColor = ModernColor.valueOf(object.getString("background_color"));
			ModernColor playColor = ModernColor.valueOf(object.getString("play_color"));

			return new ModernCartDesign(pad, id, backgroundColor, playColor, ModernColor.RED2); // TODO Fix Cue In Color
		}
		return null;
	}
}
