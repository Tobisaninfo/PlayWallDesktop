package de.tobias.playpad.update;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionUpdaterTest
{
	@Test
	public void testMigrateProject_UpdatePadDesignSettings() throws DocumentException
	{
		final InputStream projectInputStream = VersionUpdaterTest.class.getClassLoader().getResourceAsStream("de/tobias/playpad/update/ProjectToMigrateToVersion44.xml");
		final Document migratedProject = VersionUpdater.updateProject(projectInputStream);

		final Element rootElement = migratedProject.getRootElement();
		final Element pageElement = rootElement.elements().get(0);

		// pad with custom design enabled
		final Element padElement = pageElement.elements().get(0);
		final Element designElement = padElement.element("Settings").element("Design");

		assertThat(designElement.attribute("custom")).isNull();

		assertThat(designElement.element("EnableCustomBackgroundColor").getStringValue()).isEqualTo("true");
		assertThat(designElement.element("BackgroundColor").getStringValue()).isEqualTo("RED2");

		assertThat(designElement.element("EnableCustomPlayColor").getStringValue()).isEqualTo("true");
		assertThat(designElement.element("PlayColor").getStringValue()).isEqualTo("YELLOW2");

		assertThat(designElement.element("EnableCustomCueInColor").getStringValue()).isEqualTo("true");
		assertThat(designElement.element("CueInColor").getStringValue()).isEqualTo("LIGHT_GREEN2");


		// pad without custom design
		final Element padElement2 = pageElement.elements().get(1);
		final Element designElement2 = padElement2.element("Settings").element("Design");

		assertThat(designElement2.attribute("custom")).isNull();
	}
}
