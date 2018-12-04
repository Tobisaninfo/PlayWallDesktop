package de.tobias.playpad.design;

import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.view.PseudoClasses;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;

import java.util.HashMap;

public class ModernDesignAnimator {

	// alles nur static, neine objecte von der Klasse
	private ModernDesignAnimator() {
	}

	private static HashMap<Integer, Timeline> timelines = new HashMap<>();

	public static void animateFade(IPadViewController padViewController, FadeableColor startColor, FadeableColor endColor, Duration duration) {
		int index = padViewController.getPad().getPosition();

		if (timelines.containsKey(index)) {
			timelines.get(index).stop();
		}

		ChangeListener<FadeableColor> fadeListener = (observable, oldValue, newValue) -> padViewController.getView().setStyle("-fx-background-color: " + newValue.toString() + ";");

		ObjectProperty<FadeableColor> backgroundColor = new SimpleObjectProperty<>();
		backgroundColor.addListener(fadeListener);

		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), new KeyValue(backgroundColor, startColor)),
				new KeyFrame(duration, new KeyValue(backgroundColor, endColor)));

		timeline.playFromStart();
		timeline.setOnFinished(event ->
		{
			backgroundColor.removeListener(fadeListener);
			padViewController.getView().setStyle("");
			timelines.remove(index);
		});

		// Memory
		timelines.put(index, timeline);
	}

	public static void animateWarn(IPadViewController padViewController, FadeableColor startColor, FadeableColor endColor, Duration duration) {
		int index = padViewController.getPad().getPosition();

		if (timelines.containsKey(index)) {
			timelines.get(index).stop();
		}

		ChangeListener<FadeableColor> fadeListener = (observable, oldValue, newValue) -> padViewController.getView().setStyle("-fx-background-color: " + newValue.toString() + ";");

		ObjectProperty<FadeableColor> backgroundColor = new SimpleObjectProperty<>();
		backgroundColor.addListener(fadeListener);

		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), new KeyValue(backgroundColor, startColor)),
				new KeyFrame(Duration.seconds(0.125), new KeyValue(backgroundColor, startColor)),
				new KeyFrame(Duration.seconds(0.5), new KeyValue(backgroundColor, endColor)),
				new KeyFrame(Duration.seconds(0.625), new KeyValue(backgroundColor, endColor)));

		timeline.setAutoReverse(true);
		timeline.setCycleCount((int) (duration.toSeconds() / 0.625));
		timeline.playFromStart();

		timeline.setOnFinished(event ->
		{
			backgroundColor.removeListener(fadeListener);
			padViewController.getView().setStyle("");
			timelines.remove(index);
		});

		// Memory
		timelines.put(index, timeline);
	}

	public static void stopAnimation(IPadViewController controller) {
		int index = controller.getPad().getPosition();

		if (timelines.containsKey(index)) {
			timelines.get(index).stop();
		}
	}

	public static void warnFlash(IPadViewController controller) {
		final IPadView view = controller.getView();
		try {
			while (!Thread.interrupted()) {

				Platform.runLater(() ->
				{
					view.pseudoClassState(PseudoClasses.WARN_CLASS, true);
					view.pseudoClassState(PseudoClasses.PLAY_CALSS, false);
				});
				Thread.sleep(500);
				Platform.runLater(() ->
				{
					view.pseudoClassState(PseudoClasses.PLAY_CALSS, true);
					view.pseudoClassState(PseudoClasses.WARN_CLASS, false);
				});
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			Platform.runLater(() ->
			{
				view.pseudoClassState(PseudoClasses.WARN_CLASS, false);
				view.pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			});
		}
	}
}
