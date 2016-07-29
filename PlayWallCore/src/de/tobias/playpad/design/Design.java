package de.tobias.playpad.design;

public abstract class Design {

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
