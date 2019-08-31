package de.tobias.playpad.design.modern.model;

import de.thecodelabs.storage.settings.annotation.Key;

public class ModernColorBean {

	public static class Colors {
		@Key
		private String hi;
		@Key
		private String low;
		@Key
		private String font;
		@Key
		private String button;
		@Key
		private Playbar playbar;

		public String getHi() {
			return hi;
		}

		public String getLow() {
			return low;
		}

		public String getFont() {
			return font;
		}

		public String getButton() {
			return button;
		}

		public Playbar getPlaybar() {
			return playbar;
		}
	}

	public static class Playbar {
		@Key
		private String background;
		@Key
		private String track;

		public String getBackground() {
			return background;
		}

		public String getTrack() {
			return track;
		}
	}

	@Key
	private String name;
	@Key
	private Colors colors;

	public String getName() {
		return name;
	}

	public Colors getColors() {
		return colors;
	}
}
