package de.tobias.playpad.viewcontroller;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadContentView;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;

@Deprecated
public interface IPadView {

	public void setBusy(boolean busy);

	public void pseudoClassState(PseudoClass pseudoClass, boolean active);

	public void setStyle(String string);

	public Node getNewButton();

	public Button getPlayButton();

	public Button getPauseButton();

	public Button getStopButton();

	public ProgressBar getPlayBar();

	public Parent getParent();

	public void showPlaybar(boolean b);

	public void setPreviewContent(Pad pad);

	public void addDefaultButton(Pad pad);

	public void setErrorLabelActive(boolean b);

	public void setTriggerLabelActive(boolean b);

	public IPadContentView getPadContentView();

}
