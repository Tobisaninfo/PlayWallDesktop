package de.tobias.playpad.mediaplugin.main.impl;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.mediaplugin.image.ImageContent;
import de.tobias.playpad.mediaplugin.main.VideoSettings;
import de.tobias.playpad.mediaplugin.video.VideoContent;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileListener;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.OS;
import de.tobias.utils.util.OS.OSType;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class MediaViewController implements ProfileListener {

	private MediaView mediaView;
	private Pane imageView;

	private Stage stage;

	private boolean finish = true;
	private VideoSettings settings;

	private Pad currentDisplayedPad;

	MediaViewController(VideoSettings settings) {
		Profile.registerListener(this);
		this.settings = settings;

		mediaView = new MediaView();
		imageView = new Pane();

		VBox root = new VBox(mediaView);
		root.setAlignment(Pos.CENTER);

		StackPane stackPane = new StackPane(root, imageView);
		stackPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
		stackPane.setAlignment(Pos.CENTER);
		stackPane.prefWidthProperty().bind(root.widthProperty());

		mediaView.fitHeightProperty().bind(root.heightProperty());
		VBox.setVgrow(mediaView, Priority.ALWAYS);

		imageView.prefWidthProperty().bind(stackPane.widthProperty());
		imageView.prefHeightProperty().bind(stackPane.heightProperty());
		imageView.getStyleClass().add("image-style");

		stage = new Stage();
		stage.setScene(new Scene(stackPane, Color.BLACK));
		PlayPadPlugin.getImplementation().getIcon().ifPresent(stage.getIcons()::add);

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "style.css");
		if (Files.exists(path))
			getStage().getScene().getStylesheets().add(path.toUri().toString());

		stage.setMinWidth(600);
		stage.setMinHeight(400);
		if (OS.getType() == OSType.Windows)
			stage.setAlwaysOnTop(true);

		reloadSettings();
	}

	void reloadSettings() {
		Platform.runLater(() -> {
			if (stage.isFullScreen())
				stage.setFullScreen(false);

			if (stage.isShowing())
				stage.close();

			if (Screen.getScreens().size() > settings.getScreenId()) {
				Screen screen = Screen.getScreens().get(settings.getScreenId());

				Rectangle2D bounds;
				if (OS.getType() == OSType.Windows)
					bounds = screen.getBounds();
				else
					bounds = screen.getVisualBounds();

				stage.setX(bounds.getMinX());
				stage.setY(bounds.getMinY());
				stage.setWidth(bounds.getWidth());
				stage.setHeight(bounds.getHeight());
			}

			if (settings.isOpenAtLaunch() && !stage.isShowing())
				stage.show();

			if (settings.isFullScreen() && !stage.isFullScreen())
				stage.setFullScreen(true);
		});
	}

	Stage getStage() {
		return stage;
	}

	// Media
	public void setMediaPlayer(MediaPlayer player, Pad pad) {
		if (currentDisplayedPad != null && currentDisplayedPad != pad)
			this.currentDisplayedPad.setStatus(PadStatus.STOP);

		mediaView.setMediaPlayer(player);
		if (player == null) {
			finish = true;
			this.currentDisplayedPad = null;
		} else {
			finish = false;
			this.currentDisplayedPad = pad;
		}
	}

	public boolean isPlaying() {
		if (mediaView.getMediaPlayer() != null) {
			if (finish)
				return false;
			else
				return mediaView.getMediaPlayer().getStatus() == Status.PLAYING;
		} else {
			return false;
		}
	}

	@Override
	public void reloadSettings(Profile old, Profile currentProfile) {
		Platform.runLater(this::reloadSettings);
	}

	public boolean isFinish() {
		return finish;
	}

	// Image
	public void setImage(String path, Pad pad) {
		if (currentDisplayedPad != null)
			this.currentDisplayedPad.setStatus(PadStatus.STOP);

		if (path != null) {
			this.imageView.setStyle("-fx-background-image: url(\"" + path
					+ "\"); -fx-background-size: contain; -fx-background-repeat: no-repeat; -fx-background-position: center");
			this.currentDisplayedPad = pad;
		} else {
			this.imageView.setStyle("");
			this.currentDisplayedPad = null;
		}
	}

	public boolean isPicutureShowing() {
		return !imageView.getStyle().isEmpty();
	}

	// Utils
	public Pad getCurrentDisplayedPad() {
		return currentDisplayedPad;
	}

	void blind(boolean blind) {
		if (blind) {
			this.imageView.setStyle("");
			mediaView.setMediaPlayer(null);
		} else {
			if (currentDisplayedPad != null) {
				if (currentDisplayedPad.getContent() instanceof VideoContent) {
					VideoContent content = (VideoContent) currentDisplayedPad.getContent();
					mediaView.setMediaPlayer(content.getPlayer());
				} else if (currentDisplayedPad.getContent() instanceof ImageContent) {
					ImageContent content = (ImageContent) currentDisplayedPad.getContent();
					URI uri = currentDisplayedPad.getPath().toUri();
					setImage(uri.toString(), currentDisplayedPad);
				}
			}
		}
	}
}
