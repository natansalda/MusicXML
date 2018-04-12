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
        BufferedReader txtrdr = null;
        PdfWriter wr;
        Document pdf = new Document(PageSize.A4);
        Paragraph parag = new Paragraph();

        String line, codePage = "CP1250";
        Date date;
        Font fnt10n;
        PageEvent pageEvent = new PageEvent();
        boolean split = false;

        PdfPTable table = null;
        PdfPCell cell1 = null, cell2 = null, cell3 = null;
        PdfPCell cell[] = { cell1, cell2, cell3 };
        float columnWidths[] = { 1f, 2f, 3f };

        try {
            date = new Date();
            Date startDate = new Timestamp(date.getTime());
            System.out.println("Start: " + startDate);

            if (args.length < 3) {
                System.out.println("FirstPdf wymaga trzech argument�w:\n args[0] - nazwa zbioru tekstowego,"
                        + "\n args[1] - nazwa wyj�ciowego zbioru PDF,\n args[2] - �cie�ka do pliku czcionki."
                        + "\n args[3] - strona kodowa pliku wej�ciowego (domy�lnie: windows-1250");
                System.exit(20);
            }
            String os = System.getProperty("os.name");
            System.out.println("System: " + os);

            System.out.println("Plik TXT: " + args[0]);
            System.out.println("Plik PDF: " + args[1]);
            System.out.println("Plik czcionki: " + args[2]);

            if (args.length == 4)
                codePage = args[3];
            System.out.println("Strona kodowa zbioru wej�ciowego: " + codePage);

            // zbi�r tekstowy, kt�ry zostanie przetworzony na PDF:+
            txtrdr = FileFactory.newBufferedReader(args[0], codePage);

            // wyj�ciowy PDF:
            if (os.contains("Win"))
                wr = PdfWriter.getInstance(pdf, new FileOutputStream(args[1]));
            else
                wr = PdfWriter.getInstance(pdf, (new ZFile(args[1], "wb")).getOutputStream());

            // Czcionki:
            FontFactory.register(args[2], "pdfFont");
            Font font = FontFactory.getFont("pdfFont", BaseFont.CP1250, BaseFont.EMBEDDED);
            BaseFont bf = font.getBaseFont();

            fnt10n = new Font(bf, 10f, Font.NORMAL, BaseColor.BLACK);
            // PDF
            wr.setPdfVersion(PdfWriter.VERSION_1_7);
            // wr.setEncryption(USER, OWNER, PdfWriter.ALLOW_PRINTING,
            // PdfWriter.STANDARD_ENCRYPTION_128);
            wr.createXmpMetadata();
            wr.setFullCompression();
            wr.setPageEvent(pageEvent);
            pageEvent.setBaseFonts(bf);
            pageEvent.setTxt("Maurice Ravel");
            pageEvent.setShift(25);

            pdf.addTitle("Third PDF");
            pdf.addAuthor("Asseco DATA SYSTEMS SA");
            pdf.addSubject("Trzeci przyk�ad tworzenia pliku PDF");
            pdf.addKeywords("Metadata, Java, iText, PDF");
            pdf.addCreator("Program: FirstPdf");

            pdf.setMargins(50, 40, 26, 54);
            pdf.open();
            pdf.newPage();

            table = new PdfPTable(3);
            table.setWidths(columnWidths);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // rozmieszczenie tekstu w akapicie:
            parag.setAlignment(Element.ALIGN_JUSTIFIED);
            // odleg�o�� mi�dzy akapitami:
            parag.setSpacingAfter(16f);
            // odst�p miedzy liniami w akapicie:
            parag.setLeading(14f);
            // wci�cie pierwszej linii akapitu:
            parag.setFirstLineIndent(30f);
            // czcionka dla akapitu:
            parag.setFont(fnt10n);

            while (true) {
                line = txtrdr.readLine();
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
                txtrdr.close();
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
