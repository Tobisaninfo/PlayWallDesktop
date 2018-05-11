package de.tobias.playpad.log.export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.log.LogItem;
import de.tobias.playpad.log.LogSeason;
import de.tobias.playpad.util.ColorUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayoutLogPdfExport {

	public static void createPdfFile(Path path, LogSeason season) throws IOException, DocumentException {
		Document document = new Document(PageSize.A4.rotate());

		PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(path));
		document.open();

		List<Integer> pages = new ArrayList<>();
		for (LogItem logItem : season.getLogItems()) {
			if (!pages.contains(logItem.getPage())) {
				pages.add(logItem.getPage());
			}
		}
		pages.sort(Integer::compare);

		for (int i = 0; i < pages.size(); i++) {
			document.add(createPage(season, pages.get(i)));
			if (i + 1 < pages.size()) {
				document.newPage();
			}
		}

		document.close();
	}

	private static PdfPTable createPage(LogSeason season, int page) {
		List<LogItem> logItemList = season.getLogItems()
				.stream()
				.filter(logItem -> logItem.getPage() == page)
				.sorted(Comparator.comparingInt(LogItem::getPosition))
				.collect(Collectors.toList());

		int total = season.getColumns() * season.getRows();

		while (logItemList.size() < total) {
			int i;
			for (i = 0; i < logItemList.size(); i++) {
				if (i != logItemList.get(i).getPosition()) {
					break;
				}
			}
			logItemList.add(new LogItem(UUID.randomUUID(), "", "FFFFFF", page, i, season)); // add empty car
			logItemList.sort(Comparator.comparingInt(LogItem::getPosition));
		}

		PdfPTable table = new PdfPTable(season.getColumns());

		for (LogItem logItem : logItemList) {
			table.addCell(createCell(logItem, season));
		}

		return table;
	}

	private static PdfPCell createCell(LogItem logItem, LogSeason season) {
		String text = logItem.getName();
		if (!logItem.getName().isEmpty()) {
			text += "\n\n" + logItem.getPlayOutItems().size();
		}

		Font font = new Font(Font.FontFamily.HELVETICA, 12);
		ModernColor color = ModernColor.modernColorByBackgroundColor(logItem.getColor());
		if (color != null) {
			font.setColor(ColorUtils.toBaseColor(color.getFontColor()));
		} else {
			font.setColor(BaseColor.BLACK);
		}

		final Phrase phrase = new Phrase(text, font);
		PdfPCell cell = new PdfPCell(phrase);

		cell.setBackgroundColor(ColorUtils.toBaseColor(logItem.getColor()));

		cell.setMinimumHeight((PageSize.A4.rotate().getHeight() - 125.0f) / season.getRows());
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

		return cell;
	}

}
