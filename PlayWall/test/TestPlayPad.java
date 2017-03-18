import de.tobias.playpad.project.importer.ConverterV6;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.UUID;

public class TestPlayPad {

	public static void main(String[] args) {
		try {
			App app = ApplicationUtils.registerMainApplication(TestPlayPad.class);
			app.start(args);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			ConverterV6.convert(UUID.fromString("3bbd88d2-c6ed-40dd-b138-2ff9bf132ca3"), "Test");
		} catch (IOException | DocumentException e) {
			e.printStackTrace();
		}
	}
}
