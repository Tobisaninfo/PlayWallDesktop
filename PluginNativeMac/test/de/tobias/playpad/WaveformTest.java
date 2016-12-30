package de.tobias.playpad;

import de.tobias.playpad.view.WaveformView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WaveformTest extends Application {

	public static void main(String[] args) {
		System.load("/Users/tobias/Documents/Programmieren/Java/git/PlayWall/PlayWallNative/libNativeAudio.dylib");

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		float[] data = Waveform.createWaveform("/Users/tobias/Music/iTunes/iTunes Media/Music/Coldplay/Mylo Xyloto/04 Charlie Brown.mp3");
		float[] data2 = Waveform.createWaveform("/Users/tobias/Downloads/TNT-Loop.wav");
		WaveformView view = new WaveformView(data);
		WaveformView view2 = new WaveformView(data2);

		WritableImage image = new WritableImage(1200, 150);
		view.snapshot(null, image);
		WritableImage image2 = new WritableImage(1200, 150);
		view2.snapshot(null, image2);

		VBox root = new VBox(new ImageView(image), new ImageView(image2));
		Scene scene = new Scene(root);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
