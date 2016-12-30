package de.tobias.playpad.project.page;

import java.util.Collection;
import java.util.HashMap;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;

/**
 * 
 * @author tobias
 * 
 * @since 6.0.0
 */
public class Page implements Cloneable {

	private int id;
	private String name;
	private HashMap<Integer, Pad> pads;

	private transient Project projectReference;

	public Page(int id, Project reference) {
		this.id = id;
		this.name = "";
		this.pads = new HashMap<>();

		this.projectReference = reference;
	}

	public Page(int id, String name, Project reference) {
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
		for (Pad pad : pads.values()) {
			pad.setPage(id);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Project getProjectReference() {
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
		if (pad == null) {
			pads.remove(id);
		} else {
			pads.put(id, pad);
			pad.setPage(this.id);
			pad.setIndex(id);
		}
	}

	public Collection<Pad> getPads() {
		return pads.values();
	}

	public void removePade(int id) {
		pads.remove(id);
	}

	@Override
	public String toString() {
		return "Page [id=" + id + "]";
	}

	@Override
	public Page clone() throws CloneNotSupportedException {
		Page clone = (Page) super.clone();
		clone.id = id;
		clone.name = name;
		clone.projectReference = projectReference;
		clone.pads = new HashMap<>();
		for (int key : pads.keySet()) {
			Pad padClone = pads.get(key).clone();
			clone.pads.put(key, padClone);
		}
		return clone;
	}
}