package de.tobias.playpad.plugin.api.client;

import com.neovisionaries.ws.client.WebSocketState;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.api.PlayPadClient;
import de.tobias.playpad.plugin.MainWindowListener;
import de.tobias.playpad.plugin.api.WebApiPlugin$;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Tooltip;

import java.util.stream.Collectors;

public class WebApiRemoteConnectionStateListener implements MainWindowListener {

	private final FontIcon connectionStateIcon;
	private final IntegerProperty connectedProperty;

	public WebApiRemoteConnectionStateListener() {
		connectionStateIcon = new FontIcon(FontAwesomeType.CLOUD);
		connectionStateIcon.setSize(20);

		connectedProperty = new SimpleIntegerProperty(0);
		connectedProperty.addListener((observable, oldValue, newValue) -> {
			boolean allConnected = newValue.intValue() == 0;

			connectionStateIcon.setIcons(allConnected ? FontAwesomeType.CLOUD : FontAwesomeType.EXCLAMATION_CIRCLE);
			if (!allConnected) {
				final String disconnectedServers = WebApiPlugin$.MODULE$.connections().entrySet().stream()
						.filter(entry -> entry.getValue().getPlayPadConnectionState() != WebSocketState.OPEN)
						.map(entry -> entry.getKey().getName()).collect(Collectors.joining(", "));
				connectionStateIcon.setTooltip(new Tooltip(Localization.getString("webapi-settings.remote.state.tooltip", disconnectedServers)));
				connectionStateIcon.setStyle("-fx-text-fill: red;");
			} else {
				connectionStateIcon.setTooltip(null);
				connectionStateIcon.setStyle("");
			}
		});

		WebApiPlugin$.MODULE$.connections().addListener((InvalidationListener) observable -> createConnectionStateBinding());
		createConnectionStateBinding();
	}

	private void createConnectionStateBinding() {
		connectedProperty.bind(Bindings.createIntegerBinding(() -> (int) WebApiPlugin$.MODULE$.connections().values().stream()
						.filter(client -> client.getPlayPadConnectionState() != WebSocketState.OPEN)
						.count(),
				WebApiPlugin$.MODULE$.connections().values().stream()
						.map(PlayPadClient::playPadConnectionState)
						.toArray(ObjectProperty[]::new))
		);
	}

	@Override
	public void onInit(IMainViewController mainViewController) {
		mainViewController.performLayoutDependedAction((oldToolbar, newToolbar) -> {
			if (oldToolbar != null) {
				oldToolbar.removeToolbarItem(connectionStateIcon);
			}
			newToolbar.addToolbarItem(connectionStateIcon);
		});
	}
}
