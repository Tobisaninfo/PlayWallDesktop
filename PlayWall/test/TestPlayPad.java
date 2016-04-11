import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

public class TestPlayPad {

	public static void main(String[] args) throws LineUnavailableException {
		Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);

		DataLine.Info info = new Info(Clip.class, null);
		try {
			Clip clip = (Clip) mixer.getLine(info);

			Path path = Paths.get("/Users/tobias/Downloads/TURN ALL THE LIGHTS ON.wav");
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(path.toFile());

			clip.open(audioInputStream);
			clip.start();

			do {
				Thread.sleep(50);
			} while (clip.isActive());

			clip.close();
			mixer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
