package de.tobias.playpad.viewcontroller.dialog;

import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.H1;
import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Tr;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.cell.PageNameListCell;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.Printer.MarginType;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Window;

public class PrintDialog extends ViewController {

	@FXML private WebView webView;
	@FXML private ComboBox<Integer> pageComboBox;
	@FXML private Button printButton;
	@FXML private Button cancelButton;

	private Project project;

	public PrintDialog(Project project, Window owner) {
		super("printDialog", "de/tobias/playpad/assets/dialog/project/", null, PlayPadMain.getUiResourceBundle());
		this.project = project;

		int pages = project.getPages().size();
		for (int i = 0; i < pages; i++) {
			pageComboBox.getItems().add(i);
		}
		pageComboBox.getSelectionModel().selectFirst();
		pageComboBox.setCellFactory(param -> new PageNameListCell());
		pageComboBox.setButtonCell(new PageNameListCell());

		getStage().initOwner(owner);
	}

	@Override
	public void init() {
		pageComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			createPreview(c);
		});

		addCloseKeyShortcut(() -> getStage().close());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(600);
		stage.setMinHeight(400);
		stage.setTitle(Localization.getString(Strings.UI_Dialog_Print_Title));

		Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	private void createPreview(int pageIndex) {
		Html html = new Html();
		Body body = new Body();
		body.setStyle("max-width: 1000px; font-family: sans-serif;");
		html.appendChild(body);

		H1 header = new H1();

		Page page = project.getPage(pageIndex);
		String pageName = page.getName();
		if (pageName.isEmpty()) {
			pageName = Localization.getString(Strings.UI_Window_Main_PageButton, (pageIndex + 1));
		}

		String headerString = Localization.getString(Strings.Info_Print_Header, project.getProjectReference().getName(), pageName);
		header.appendText(headerString);
		header.setStyle("text-align: center;");
		body.appendChild(header);

		Table table = new Table();
		table.setStyle("border:1px solid black;border-collapse:collapse;");

		ProjectSettings settings = project.getSettings();
		int padIndex = 0;

		for (int y = 0; y < settings.getRows(); y++) {
			Tr tr = new Tr();
			table.appendChild(tr);
			for (int x = 0; x < settings.getColumns(); x++) {
				Td td = new Td();
				td.setStyle("border:1px solid black; width: " + 1000 / (float) settings.getColumns()
						+ "px; padding: 5px; vertical-align: center; text-align: center; min-height: 30px; min-width: 100px;");
				Div div = new Div();
				div.setStyle("word-break: break-all; white-space: normal;");
				Pad pad = this.project.getPad(new PadIndex(padIndex, pageIndex));

				if (pad.getContent() != null && pad.getContent().isPadLoaded())
					div.appendText(pad.getName());
				else
					div.appendText("-");
				td.appendChild(div);
				padIndex++;
				tr.appendChild(td);
			}
		}
		body.appendChild(table);
		WebEngine e = webView.getEngine();
		e.loadContent(html.write());
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		getStage().close();
	}

	@FXML
	private void printButtonHandler(ActionEvent event) {
		Printer printer = Printer.getDefaultPrinter();
		PageLayout layout = printer.createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, MarginType.DEFAULT);
		PrinterJob job = PrinterJob.createPrinterJob(printer);
		job.getJobSettings().setPageLayout(layout);
		job.getJobSettings().setJobName(ApplicationUtils.getApplication().getInfo().getName());
		if (job != null && job.showPrintDialog(getStage())) {
			webView.getEngine().print(job);
			job.endJob();
			getStage().close();
		}
	}

}
