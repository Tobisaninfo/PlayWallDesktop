package de.tobias.playpad.project;

import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.modern.ModernCartDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.project.page.Page;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by tobias on 26.02.17.
 */
public class ProjectJsonWriter {
	public JSONObject write(Project project) {
		JSONObject json = new JSONObject();

		json.put("id", project.getProjectReference().getUuid().toString());
		json.put("name", project.getProjectReference().getName());

		JSONArray pageArray = new JSONArray();
		project.pages.forEach(page -> pageArray.put(writePage(page)));
		json.put("pages", pageArray);

		return json;
	}

	private JSONObject writePage(Page page) {
		JSONObject json = new JSONObject();

		json.put("id", page.getId().toString());
		json.put("name", page.getName());
		json.put("position", page.getPosition());

		JSONArray padArray = new JSONArray();
		page.getPads().forEach(pad -> padArray.put(writePad(pad)));
		json.put("pads", padArray);

		return json;
	}

	private JSONObject writePad(Pad pad) {
		JSONObject json = new JSONObject();

		json.put("id", pad.getUuid().toString());
		json.put("name", pad.getName());
		json.put("position", pad.getPosition());
		json.put("contentType", pad.getContentType());

		JSONArray pathArray = new JSONArray();
		pad.getPaths().forEach(path -> pathArray.put(writePath(path)));

		json.put("paths", pathArray);

		CartDesign design = pad.getPadSettings().getDesign(ModernCartDesign.TYPE);
		if (design != null && design instanceof ModernCartDesign) {
			JSONObject designJson = writeModernDesign((ModernCartDesign) design);
			json.put("design", designJson);
		}

		return json;
	}

	private JSONObject writePath(MediaPath path) {
		JSONObject json = new JSONObject();

		json.put("id", path.getId().toString());
		json.put("path", path.getPath().toString());

		return json;
	}

	private JSONObject writeModernDesign(ModernCartDesign design) {
		JSONObject json = new JSONObject();

		json.put("id", design.getId().toString());
		json.put("background_color", design.getBackgroundColor().name());
		json.put("play_color", design.getPlayColor().name());

		return json;
	}
}
