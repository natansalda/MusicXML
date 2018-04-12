package music;

import com.ibm.jzos.FileFactory;
import com.ibm.jzos.ZFile;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

public class MusicXMLNew {

    public static void main(String[] args) {
        BufferedReader xmlrdr = null;
        PdfWriter writer;
        Document pdf = new Document(PageSize.A4);
        Paragraph parag = new Paragraph();

        String line, codePage = "CP1250";
        Date date;
        Font fnt10n;
        PageEvent pageEvent = new PageEvent();
        boolean split = false;

        PdfPTable table = null;
        PdfPCell cell1 = null, cell2 = null, cell3 = null;
        PdfPCell cell[] = {cell1, cell2, cell3};
        float columnWidths[] = {1f, 2f, 3f};

        try {
            date = new Date();
            Date startDate = new Timestamp(date.getTime());
            System.out.println("Start: " + startDate);

            if (args.length < 3) {
                System.out.println("Wymaga trzech argumentow:\n args[0] - nazwa zbioru tekstowego,"
                        + "\n args[1] - nazwa wyjsciowego zbioru PDF,\n args[2] - sciezka do pliku czcionki."
                        + "\n args[3] - strona kodowa pliku wejsciowego (domyslnie: windows-1250");
                System.exit(20);
            }
            String os = System.getProperty("os.name");
            System.out.println("System: " + os);

            System.out.println("Plik XML: " + args[0]);
            System.out.println("Plik PDF: " + args[1]);
            System.out.println("Plik czcionki: " + args[2]);

            if (args.length == 4)
                codePage = args[3];
            System.out.println("Strona kodowa zbioru wejsciowego: " + codePage);

            // zbior XML, ktory zostanie przetworzony na PDF:+
            xmlrdr = FileFactory.newBufferedReader(args[0], codePage);

            // wyjsciowy PDF:
            if (os.contains("Win"))
                writer = PdfWriter.getInstance(pdf, new FileOutputStream(args[1]));
            else
                writer = PdfWriter.getInstance(pdf, (new ZFile(args[1], "wb")).getOutputStream());

            // Czcionki:
            FontFactory.register(args[2], "pdfFont");
            Font font = FontFactory.getFont("pdfFont", BaseFont.CP1250, BaseFont.EMBEDDED);
            BaseFont bf = font.getBaseFont();
            fnt10n = new Font(bf, 10f, Font.NORMAL, BaseColor.BLACK);

            // PDF
            writer.setPdfVersion(PdfWriter.VERSION_1_7);
            writer.createXmpMetadata();
            writer.setFullCompression();
            writer.setPageEvent(pageEvent);

            pageEvent.setBaseFonts(bf);
            pageEvent.setTxt("Musical collection");
            pageEvent.setShift(25);

            pdf.addTitle("Musical collection");
            pdf.addAuthor("Natalia Nazaruk");
            pdf.addSubject("Cwiczenie z czytania XML do PDF");
            pdf.addKeywords("Metadata, Java, iText, PDF, XML");
            pdf.addCreator("Program: MusicXMLNew");

            pdf.setMargins(50, 40, 26, 54);
            pdf.open();
            pdf.newPage();

            table = new PdfPTable(3);
            table.setWidths(columnWidths);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // rozmieszczenie tekstu w akapicie:
            parag.setAlignment(Element.ALIGN_JUSTIFIED);
            // odleglosci miedzy akapitami:
            parag.setSpacingAfter(16f);
            // odstep miedzy liniami w akapicie:
            parag.setLeading(14f);
            // wciecie pierwszej linii akapitu:
            parag.setFirstLineIndent(30f);
            // czcionka dla akapitu:
            parag.setFont(fnt10n);

            while (true) {
                line = xmlrdr.readLine();
                if (line == null)
                    break;
                if (line.contains("Lata pracy"))
                    split = true;
                if (split == false) {
                    parag.add(line);
                    pdf.add(parag);
                    parag.clear();
                } else {
                    String words[] = line.split("!");
                    for (int k = 0; k < 3; k++) {
                        cell[k] = new PdfPCell(new Paragraph(words[k].trim(), fnt10n));
                        if (words[0].contains("Lata pracy")) {
                            // usuni�cie linii z lewej strony kom�rki
                            cell[k].disableBorderSide(Rectangle.LEFT);
                            // usuni�cie linii z prawej strony kom�rki
                            cell[k].disableBorderSide(Rectangle.RIGHT);
                            // pogrubienie dolnej linii:
                            cell[k].setBorderWidthBottom(0.75f);
                        } else
                            // usuni�cie obramowania kom�rki
                            cell[k].disableBorderSide(Rectangle.BOX);
                        table.addCell(cell[k]);
                    }
                }
            }
            pdf.add(table);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        } finally {
            try {
                xmlrdr.close();
                pdf.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(20);
            }
            date = new Date();
            Date stopDate = new Timestamp(date.getTime());
            System.out.println("Stop: " + stopDate);
            System.out.println("OK.");
        }
    }
}
