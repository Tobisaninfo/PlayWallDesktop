package de.tobias.playpad.pad;

import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.settings.FadeSettings;
import javafx.util.Duration;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

import java.util.UUID;

import static de.tobias.playpad.pad.PadSerializer.SETTINGS_ELEMENT;
import static org.assertj.core.api.Assertions.assertThat;

public class PadSettingsSerializerTest {
	@Test
	public void testSave_General() {
		// arrange
		final Document document = DocumentHelper.createDocument();
		final Element rootElement = document.addElement(SETTINGS_ELEMENT);

		final Project project = new Project(new ProjectReference(UUID.randomUUID(), "TestProject", false));
		final Pad pad = new Pad(project, 1, null);

		final UUID padUUID = UUID.randomUUID();
		final PadSettings padSettings = new PadSettings(pad, padUUID);
		padSettings.setVolume(0.5);
		padSettings.setLoop(true);
		padSettings.setTimeMode(TimeMode.REST);
		padSettings.setFade(new FadeSettings(Duration.seconds(1), Duration.seconds(2)));

		final Duration expectedWarningDuration = Duration.seconds(3);
		padSettings.setWarning(expectedWarningDuration);
		final Duration expectedCueInDuration = Duration.seconds(4);
		padSettings.setCueIn(expectedCueInDuration);

		// act
		final PadSettingsSerializer serializer = new PadSettingsSerializer();
		serializer.saveElement(rootElement, padSettings);

		// assert
		assertThat(rootElement.attribute("id").getStringValue()).isEqualTo(padUUID.toString());
		assertThat(rootElement.element("Volume").getStringValue()).isEqualTo("0.5");
		assertThat(rootElement.element("Loop").getStringValue()).isEqualTo("true");
		assertThat(rootElement.element("TimeMode").getStringValue()).isEqualTo(TimeMode.REST.toString());
		assertThat(rootElement.element("Fade").getStringValue()).isNotNull();
		assertThat(rootElement.element("Warning").getStringValue()).isEqualTo(expectedWarningDuration.toString());
		assertThat(rootElement.element("CueIn").getStringValue()).isEqualTo(expectedCueInDuration.toString());
	}
}
