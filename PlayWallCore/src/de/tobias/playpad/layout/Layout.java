package de.tobias.playpad.layout;

public abstract class Layout {

	protected void startStyleClass(StringBuilder builder, String name) {
		builder.append("." + name + " {\n");
	}

	protected void addStyleParameter(StringBuilder builder, String key, String value) {
		builder.append("\t");
		builder.append(key);
		builder.append(": ");
		builder.append(value);
		builder.append(";\n");
	}

	protected void addStyleParameter(StringBuilder builder, String key, double value) {
		builder.append("\t");
		builder.append(key);
		builder.append(": ");
		builder.append(value);
		builder.append(";\n");
	}

	protected void endStyleClass(StringBuilder builder) {
		builder.append("}\n\n");
	}
}
