package music;

import com.ibm.jzos.FileFactory;
import com.ibm.jzos.ZFile;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class MusicXMLNew {

    public static void main(String[] args) {
        BufferedReader xmlrdr = null;
        PdfWriter writer;
        Document pdf = new Document(PageSize.A4);
        Paragraph parag = new Paragraph();

        String line = null, codePage = "CP1250", sep = " ", intro = null;
        Date date;
        Font fnt10n;
        PageEvent pageEvent = new PageEvent();

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

            intro = "This is Music Collection \n\n";
            pdf.add(new Paragraph(intro, fnt10n));

            for (Music.Artist artysta : listaArtystow) {
                List<Music.Artist.Album> listaAlbumow = artysta.getAlbum();
                for (Music.Artist.Album album : listaAlbumow) {
                    Music.Artist.Album.Description opis = album.getDescription();
                    List<Music.Artist.Album.Song> listaPiosenek = album.getSong();
                    for (Music.Artist.Album.Song piosenka : listaPiosenek) {
                        // Elementy do wydruku
                        String artistName = artysta.getName();
                        String albumName = album.getTitle();
                        int numberOfSongs = listaPiosenek.size();
                        String albumDescription = album.getDescription().getValue();
                        String songTitle = piosenka.getTitle();
                        String songDuration = piosenka.getLength();


                        line = "Artist: " + artistName + ". Album name: " + albumName + sep + "Songs list: " + songTitle + sep + songDuration;

                        pdf.add(new Paragraph(line, fnt10n));
                    }
                }
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