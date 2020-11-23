package de.tobias.playpad.util;

import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.List;

public class NodeWalker {

	private NodeWalker() {
	}

	public static List<Node> getAllNodes(Parent root) {
		List<Node> nodes = new ArrayList<>();
		nodes.add(root);
		addAllDescendents(root, nodes);
		return nodes;
	}

	private static void addAllDescendents(Parent parent, List<Node> nodes) {
		for (Node node : parent.getChildrenUnmodifiable()) {
			nodes.add(node);
			if (node instanceof Parent)
				addAllDescendents((Parent) node, nodes);
		}
	}
}
