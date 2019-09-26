package de.tobias.playpad.util;

public class Minifier {

	private Minifier() {
	}

	private static String[] expressions = {
			"\n",
			"\t"
	};

	public static String minify(String input) {
		for (String expression : expressions) {
			input = input.replaceAll(expression, "");
		}
		return input;
	}
}
