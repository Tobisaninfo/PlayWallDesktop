package de.tobias.playpad.plugin.playout.export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.plugin.playout.ColorUtils;
import de.tobias.playpad.plugin.playout.log.LogItem;
import de.tobias.playpad.plugin.playout.log.LogSeason;

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
		writer.setPageEvent(new HeaderFooter(season.getName()));
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
			logItemList.add(new LogItem(UUID.randomUUID(), "", "FFFFFF", page, i, season)); // add empty cart
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

	private static class HeaderFooter extends PdfPageEventHelper {

		private String text;

		public HeaderFooter(String text) {
			this.text = text;
		}

		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			Font font = new Font(Font.FontFamily.HELVETICA, 12);

			PdfPTable table = new PdfPTable(2);
			try {
				table.setWidths(new int[]{24, 24});
				table.setTotalWidth(document.getPageSize().getWidth() - 150);
				table.getDefaultCell().setBorder(Rectangle.BOTTOM);
				table.addCell(new Phrase(text, font));
				table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(new Phrase("Seite " + writer.getPageNumber(), font));

				final PdfContentByte canvas = writer.getDirectContent();
				canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
				table.writeSelectedRows(0, -1, 80, 30, canvas);
				canvas.endMarkedContentSequence();

			} catch (DocumentException e) {
				throw new ExceptionConverter(e);
			}
		}
	}

}
