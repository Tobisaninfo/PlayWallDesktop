package de.tobias.playpad.project.page;

import java.io.Serializable;

/**
 * Struktur um den Index eines Pads zu beschrieben.
 *
 * @author tobias
 * @since 6.0.0
 */
public class PadIndex implements Serializable {

	private static final long serialVersionUID = 2026743397726990321L;

	private final int id;
	private final int page;

	public PadIndex(int id, int page) {
		this.id = id;
		this.page = page;
	}

	public int getId() {
		return id;
	}

	public int getPagePosition() {
		return page;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + page;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PadIndex other = (PadIndex) obj;
		if (id != other.id)
			return false;
		return page == other.page;
	}

	@Override
	public String toString() {
		return id + "-" + page;
	}

}
