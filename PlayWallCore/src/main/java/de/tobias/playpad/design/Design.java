package de.tobias.playpad.design;

class Design {

	static void startStyleClass(StringBuilder builder, String name) {
		builder.append(".").append(name).append(" {\n");
	}

	static void addStyleParameter(StringBuilder builder, String key, Object value) {
		builder.append("\t");
		builder.append(key);
		builder.append(": ");
		builder.append(value);
		builder.append(";\n");
	}

	static void endStyleClass(StringBuilder builder) {
		builder.append("}\n\n");
	}
}
