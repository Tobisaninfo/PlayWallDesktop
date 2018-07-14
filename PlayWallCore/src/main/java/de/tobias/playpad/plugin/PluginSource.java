package de.tobias.playpad.plugin;

import java.net.URL;

/**
 * Diese Klasse verwaltet PluginSources, indem sie die URL zu einerm plugin.yml auf einem Server speichert. Das Format für solch eine
 * plugin.yml muss folgendes enthalten: version, filename, remotepath, build, dependencies (optional), id.
 *
 * @author tobias
 * @since 5.1.0
 */
public class PluginSource {

	private String name;
	private URL url;

	/**
	 * Erstellt eine neues Plugin Quelle.
	 *
	 * @param name Name der Quelle
	 * @param url  Adresse der Quelle
	 */
	public PluginSource(String name, URL url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public URL getUrl() {
		return url;
	}
}
