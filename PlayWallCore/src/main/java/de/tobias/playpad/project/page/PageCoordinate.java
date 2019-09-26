package de.tobias.playpad.project.page;

public class PageCoordinate {

	private final int x;
	private final int y;

	public PageCoordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return "PageCoordinate{" +
				"x=" + x +
				", y=" + y +
				'}';
	}
}
