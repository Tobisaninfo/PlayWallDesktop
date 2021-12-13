package de.tobias.playpad.design.modern.serializer;

import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ModernGlobalDesignSerializerTest
{
	private final ModernColor BACKGROUND_COLOR = ModernColor.BLUE1;
	private final ModernColor PLAY_COLOR = ModernColor.GRAY5;
	private final ModernColor CUE_IN_COLOR = ModernColor.PURPLE1;
	private final int INFO_FONT_SIZE = 12;
	private final int TITLE_FONT_SIZE = 18;

	@Test
	public void testSave()
	{
		// arrange
		final ModernGlobalDesign design = new ModernGlobalDesign();
		design.setFlatDesign(true);
		design.setBackgroundColor(BACKGROUND_COLOR);
		design.setPlayColor(PLAY_COLOR);
		design.setCueInColor(CUE_IN_COLOR);
		design.setInfoFontSize(INFO_FONT_SIZE);
		design.setTitleFontSize(TITLE_FONT_SIZE);
		design.setWarnAnimation(true);

		final Document document = DocumentHelper.createDocument();
		final Element rootElement = document.addElement("Design");

		// act
		final ModernGlobalDesignSerializer serializer = new ModernGlobalDesignSerializer();
		serializer.save(rootElement, design);

		// assert
		assertThat(rootElement.element("FlatDesign").getStringValue()).isEqualTo("true");
		assertThat(rootElement.element("BackgroundColor").getStringValue()).isEqualTo(BACKGROUND_COLOR.name());
		assertThat(rootElement.element("PlayColor").getStringValue()).isEqualTo(PLAY_COLOR.name());
		assertThat(rootElement.element("CueInColor").getStringValue()).isEqualTo(CUE_IN_COLOR.name());
		assertThat(rootElement.element("InfoFontSize").getStringValue()).isEqualTo("12");
		assertThat(rootElement.element("TitleFontSize").getStringValue()).isEqualTo("18");
		assertThat(rootElement.element("Animation").element("Warn").getStringValue()).isEqualTo("true");
	}

	@Test
	public void testLoad() throws DocumentException
	{
		// arrange
		final String filePath = "de/tobias/playpad/design/modern/serializer/modern_global_design.xml";
		final InputStream inputStream = ModernGlobalDesignSerializerTest.class.getClassLoader().getResourceAsStream(filePath);
		final SAXReader reader = new SAXReader();
		final Document document = reader.read(inputStream);

		// act
		final ModernGlobalDesignSerializer serializer = new ModernGlobalDesignSerializer();
		final ModernGlobalDesign design = serializer.load(document.getRootElement());

		// assert
		assertThat(design.isFlatDesign()).isTrue();
		assertThat(design.getBackgroundColor()).isEqualTo(BACKGROUND_COLOR);
		assertThat(design.getPlayColor()).isEqualTo(PLAY_COLOR);
		assertThat(design.getCueInColor()).isEqualTo(CUE_IN_COLOR);
		assertThat(design.getInfoFontSize()).isEqualTo(INFO_FONT_SIZE);
		assertThat(design.getTitleFontSize()).isEqualTo(TITLE_FONT_SIZE);
		assertThat(design.isWarnAnimation()).isTrue();
	}
}
