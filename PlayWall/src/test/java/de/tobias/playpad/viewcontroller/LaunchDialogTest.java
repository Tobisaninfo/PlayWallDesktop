package de.tobias.playpad.viewcontroller;

import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.update.VersionUpdater;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.testfx.assertions.api.Assertions.assertThat;

public class LaunchDialogTest extends ApplicationTest {

	private LaunchDialog launchDialog;

	@Before
	public void init() {
		App app = ApplicationUtils.registerMainApplication(PlayPadMain.class);
		ApplicationUtils.registerUpdateService(new VersionUpdater());

		Logger.init(app.getPath(PathType.LOG));
		Logger.setLevelFilter(LogLevelFilter.DEBUG);

		PlayPadMain playPadMain = new PlayPadMain();
		playPadMain.init();
	}

	@Override
	public void start(Stage stage) {
		launchDialog = new LaunchDialog(stage);
	}

	@Test
	public void testNewDialogTest() {
		assertThat(lookup("#newProjectButton").queryButton()).isNotNull();
	}
}
