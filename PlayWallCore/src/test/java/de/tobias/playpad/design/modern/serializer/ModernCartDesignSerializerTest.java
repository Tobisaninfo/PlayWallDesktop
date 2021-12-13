package de.tobias.playpad.design.modern.serializer;

import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ModernCartDesignSerializerTest
{
	final ModernColor BACKGROUND_COLOR = ModernColor.BLUE1;
	final ModernColor PLAY_COLOR = ModernColor.GRAY5;
	final ModernColor CUE_IN_COLOR = ModernColor.PURPLE1;

	final UUID DESIGN_UUID = UUID.fromString("33977b97-60d6-49f1-897a-2f6f80de74e4");


	@Test
	public void testSave()
	{
		// arrange
		final ModernCartDesign design = new ModernCartDesign.ModernCartDesignBuilder(null, DESIGN_UUID)
				.withBackgroundColor(BACKGROUND_COLOR, true)
				.withPlayColor(PLAY_COLOR, true)
				.withCueInColor(CUE_IN_COLOR, false)
				.build();

		final Document document = DocumentHelper.createDocument();
		final Element rootElement = document.addElement("Design");

		// act
		final ModernCartDesignSerializer serializer = new ModernCartDesignSerializer();
		serializer.save(rootElement, design);

		// assert
		assertThat(rootElement.attributeValue("id")).isEqualTo(DESIGN_UUID.toString());

		assertThat(rootElement.element("EnableCustomBackgroundColor").getStringValue()).isEqualTo("true");
		assertThat(rootElement.element("BackgroundColor").getStringValue()).isEqualTo(BACKGROUND_COLOR.name());

		assertThat(rootElement.element("EnableCustomPlayColor").getStringValue()).isEqualTo("true");
		assertThat(rootElement.element("PlayColor").getStringValue()).isEqualTo(PLAY_COLOR.name());

		assertThat(rootElement.element("EnableCustomCueInColor").getStringValue()).isEqualTo("false");
		assertThat(rootElement.element("CueInColor").getStringValue()).isEqualTo(CUE_IN_COLOR.name());
	}

	@Test
	public void testLoad() throws DocumentException
	{
		// arrange
		final String filePath = "de/tobias/playpad/design/modern/serializer/modern_cart_design.xml";
		final InputStream inputStream = ModernCartDesignSerializerTest.class.getClassLoader().getResourceAsStream(filePath);
		final SAXReader reader = new SAXReader();
		final Document document = reader.read(inputStream);

		// act
		final ModernCartDesignSerializer serializer = new ModernCartDesignSerializer();
		final ModernCartDesign design = serializer.load(document.getRootElement(), null);

		// assert
		assertThat(design.getId()).isEqualTo(DESIGN_UUID);

		assertThat(design.isEnableCustomBackgroundColor()).isTrue();
		assertThat(design.getBackgroundColor()).isEqualTo(BACKGROUND_COLOR);

		assertThat(design.isEnableCustomPlayColor()).isTrue();
		assertThat(design.getPlayColor()).isEqualTo(PLAY_COLOR);

		assertThat(design.isEnableCustomCueInColor()).isFalse();
		assertThat(design.getCueInColor()).isEqualTo(CUE_IN_COLOR);
	}
}
