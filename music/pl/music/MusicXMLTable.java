package music.pl.music;

import com.ibm.jzos.FileFactory;
import com.ibm.jzos.ZFile;
import com.itextpdf.layout.element.Table;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import music.Music;
import music.ObjectFactory;
import music.PageEvent;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class MusicXMLTable {

    public static void main(String[] args) {
        BufferedReader xmlrdr = null;
        PdfWriter writer;
        Document pdf = new Document(PageSize.A4);
        pdf.setMargins(20, 20, 20, 20);

        String line, codePage = "CP1250", sep = " ", intro, albumName, songDuration, songTitle, artistName, albumDescription;
        String starSeparator = "\n****************************************************************\n\n";
        int numberOfSongs = 0;
        Date date;
        Font fnt10n, fnt14b, fnt16i;
        PageEvent pageEvent = new PageEvent();

        PdfPTable table = null;
        PdfPCell cell, cellAlbum;
        float columnWidths[] = {2f, 3f, 1f};

        int rowspan = 2;

        JAXBContext jaxb = null;
        Unmarshaller unmarsh = null;

        Music music;

        try {
            date = new Date();
            Date startDate = new Timestamp(date.getTime());
            System.out.println("Start: " + startDate);

            jaxb = JAXBContext.newInstance(ObjectFactory.class);
            unmarsh = jaxb.createUnmarshaller();

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
            music = (Music) unmarsh.unmarshal(xmlrdr);
            List<Music.Artist> listaArtystow = music.getArtist();

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
            fnt14b = new Font(bf, 14f, Font.BOLD, BaseColor.RED);
            fnt16i = new Font(bf, 16f, Font.ITALIC, BaseColor.BLUE);

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

            intro = "This is Music Collection";
            pdf.add(new Paragraph(intro + starSeparator, fnt16i));


            for (Music.Artist artysta : listaArtystow) {
                artistName = artysta.getName();
                line = "Artist: " + artistName;
                pdf.add(new Paragraph(line, fnt14b));

                table = new PdfPTable(3);
                table.setWidths(columnWidths);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);

                // Artist cell
                cell = new PdfPCell(new Phrase("Artist name: " + artistName));
                cell.setColspan(3);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                List<Music.Artist.Album> listaAlbumow = artysta.getAlbum();
                for (Music.Artist.Album album : listaAlbumow) {
                    albumName = album.getTitle();
                    line = "Album name: " + albumName + "\n\n";
                    pdf.add(new Paragraph(line, fnt14b));

                    albumDescription = album.getDescription().getValue();

                    line = "Songs list: " + "\n";
                    pdf.add(new Paragraph(line, fnt10n));

                    List<Music.Artist.Album.Song> listaPiosenek = album.getSong();

                    // Album cell
                    Paragraph paragraph = new Paragraph("Album title:" + "\n" + albumName);
                    cellAlbum = new PdfPCell();
                    numberOfSongs = listaPiosenek.size();
                    cellAlbum.setRowspan(numberOfSongs);
                    cellAlbum.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cellAlbum.addElement(paragraph);
//                    if (albumName.contains("Limb")) {
//                        Image image = Image.getInstance("C:\\Users\\natalia.nazaruk\\Pictures\\rad1.png");
//                        image.scaleAbsolute(100, 100);
//                        cellAlbum.addElement(image);
//                    } else if (albumName.contains("Comp")) {
//                        Image image = Image.getInstance("C:\\Users\\natalia.nazaruk\\Pictures\\rad2.png");
//                        image.scaleAbsolute(100, 100);
//                        cellAlbum.addElement(image);
//                    } else if (albumName.contains("Dummy")) {
//                        Image image = Image.getInstance("C:\\Users\\natalia.nazaruk\\Pictures\\por1.png");
//                        image.scaleAbsolute(100, 100);
//                        cellAlbum.addElement(image);
//                    } else {
//                        Image image = Image.getInstance("C:\\Users\\natalia.nazaruk\\Pictures\\por2.png");
//                        image.scaleAbsolute(100, 100);
//                        cellAlbum.addElement(image);
//                    }
                    table.addCell(cellAlbum);

                    for (Music.Artist.Album.Song piosenka : listaPiosenek) {
                        songTitle = piosenka.getTitle();
                        songDuration = piosenka.getLength();

                        line = songTitle + sep + songDuration;
                        pdf.add(new Paragraph(line, fnt10n));

                        table.addCell(songTitle);
                        table.addCell(songDuration);
                    }

                    line = "\nNumber of songs in the album: " + numberOfSongs + "\n\n";
                    pdf.add(new Paragraph(line, fnt10n));

                    line = "Album description:" + albumDescription;
                    pdf.add(new Paragraph(line, fnt10n));
                }

                pdf.add(table);
            }


        } catch (IOException | DocumentException | JAXBException e) {
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