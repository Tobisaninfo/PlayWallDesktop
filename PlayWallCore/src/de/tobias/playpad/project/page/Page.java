package de.tobias.playpad.project.page;

import java.util.Collection;
import java.util.HashMap;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.project.v2.ProjectV2;

/**
 * 
 * @author tobias
 * 
 * @since 6.0.0
 */
public class Page {

	private int id;
	private String name;
	private HashMap<Integer, Pad> pads;

	private transient ProjectV2 projectReference;

	public Page(int id, ProjectV2 reference) {
		this.id = id;
		this.name = "";
		this.pads = new HashMap<>();

		this.projectReference = reference;
	}

	public Page(int id, String name, ProjectV2 reference) {
		this.id = id;
		this.name = name;
		this.pads = new HashMap<>();

		this.projectReference = reference;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProjectV2 getProjectReference() {
		return projectReference;
	}

	public Pad getPad(int id) {
		if (!pads.containsKey(id)) {
			// Create new pad for id
			setPad(id, new Pad(projectReference, id, this.id));
		}
		return pads.get(id);
	}

	public Pad getPad(int x, int y) {
		ProjectSettings settings = projectReference.getSettings();
		if (x < settings.getColumns() && y < settings.getRows()) {
			int id = y * settings.getColumns() + x;
			return getPad(id);
		}
		return null;
	}

	public void setPad(int id, Pad pad) {
		pads.put(id, pad);
		pad.setIndex(id);
	}

	public Collection<Pad> getPads() {
		return pads.values();
	}

	public void removePade(int id) {
		pads.remove(id);
	}
}
