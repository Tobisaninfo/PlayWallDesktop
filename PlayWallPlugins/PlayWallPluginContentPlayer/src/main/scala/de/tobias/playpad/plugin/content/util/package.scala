package de.tobias.playpad.plugin.content

import javafx.collections.ObservableList

package object util {
	implicit class ObservableListExtension[E >: Null](list: ObservableList[E]) {
		def head: E = {
			if (list.isEmpty) {
				return null
			}

			list.get(0)
		}

		def apply(index: Int): E = list.get(index)

		def length: Long = list.size()

		def isNotEmpty: Boolean = !list.isEmpty

		def indexWhere(predicate: E => Boolean): Int = {
			for (i <- 0 to list.size()) {
				if (predicate(list.get(i))) {
					return i
				}
			}
			-1
		}
	}
}
