package de.tobias.playpad.design;

public class Design {

	public static void startStyleClass(StringBuilder builder, String name) {
		builder.append(".").append(name).append(" {\n");
	}

	public static void addStyleParameter(StringBuilder builder, String key, String value) {
		builder.append("\t");
		builder.append(key);
		builder.append(": ");
		builder.append(value);
		builder.append(";\n");
	}

	public static void addStyleParameter(StringBuilder builder, String key, double value) {
		builder.append("\t");
		builder.append(key);
		builder.append(": ");
		builder.append(value);
		builder.append(";\n");
	}

	public static void endStyleClass(StringBuilder builder) {
		builder.append("}\n\n");
	}
}
