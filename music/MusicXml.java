package music;

import java.io.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.ibm.jzos.ZFile;

import java.io.FileOutputStream;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import music.Music.Artist;
import music.Music.Artist.Album;
import music.Music.Artist.Album.Description;
import music.Music.Artist.Album.Song;

public class MusicXml {

    public static void main(String[] args) throws JAXBException, IOException {

        // zbiory z/OS:
        ZFile inputZ = null, outputZ = null;

        // zbiory windows:
        File inputW = null;
        PdfWriter outputW = null;

        // pisanie pdf
        PdfContentByte cb = null;
        Document pdf = new Document(PageSize.A4);
        Paragraph paragraf = new Paragraph();

        // Font
        Font fnt12n;

        JAXBContext jaxb = null;
        Unmarshaller unmarsh = null;

        String line = null, sep = " ";
        Music music;

        Date date = new Date();
        Date startDate = new Timestamp(date.getTime());
        System.out.println("Start: " + startDate);

        jaxb = JAXBContext.newInstance(ObjectFactory.class);
        unmarsh = jaxb.createUnmarshaller();

        String os = System.getProperty("os.name");
        System.out.println("System: " + os);
        boolean isWin = os.toLowerCase().contains("wind");

        if (!isWin) {
            // z/OS:
            inputZ = new ZFile(args[0], "rt"); // "rt" - readtext
            InputStream inpStream = inputZ.getInputStream();
            InputStreamReader streamRdr = new InputStreamReader(inpStream, "CP870");
            try {
                outputW = PdfWriter.getInstance(pdf, (new ZFile(args[1], "wb")).getOutputStream());
            } catch (DocumentException e) {
                e.printStackTrace();
            }

            music = (Music) unmarsh.unmarshal(streamRdr);

        } else {
            // Windows:
            inputW = new File(args[0]);
            music = (Music) unmarsh.unmarshal(inputW);
            try {
                outputW = PdfWriter.getInstance(pdf, new FileOutputStream(args[1]));
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }

        List<Artist> listaArtystow = music.getArtist();
        for (Artist artysta : listaArtystow) {
            List<Album> listaAlbumow = artysta.getAlbum();
            for (Album album : listaAlbumow) {
                Description opis = album.getDescription();
                List<Song> listaPiosenek = album.getSong();
                for (Song piosenka : listaPiosenek) {
                    // Elementy do wydruku
                    String artistName = artysta.getName();
                    String albumName = album.getTitle();
                    int numberOfSongs = listaPiosenek.size();
                    String albumDescription = album.getDescription().getValue();
                    String songTitle = piosenka.getTitle();
                    String songDuration = piosenka.getLength();

                    // Czcionki:
                    FontFactory.register(args[2], "jakiesFonty");
                    Font font = FontFactory.getFont("jakiesFonty", BaseFont.CP1250, BaseFont.EMBEDDED);
                    BaseFont bf = font.getBaseFont();
                    fnt12n = new Font(bf, 12f, Font.NORMAL, BaseColor.BLACK);

                    // PDF
                    outputW.setPdfVersion(PdfWriter.VERSION_1_7);

                    pdf.addTitle("Musical collection");
                    pdf.addAuthor("Natalia Nazaruk");
                    pdf.addSubject("Cwiczenie tworzenia PDF z XML");
                    pdf.addKeywords("Metadata, Java, iText, PDF");
                    pdf.addCreator("Program: MusicXML");
                    pdf.setMargins(60, 60, 50, 40);

                    pdf.open();
                    pdf.newPage();
                    try {
                        pdf.add(new Paragraph(paragraf));
                        // rozmieszczenie tekstu w akapicie:
                        paragraf.setAlignment(Element.ALIGN_JUSTIFIED);
                        // odleg�o�� mi�dzy akapitami:
                        paragraf.setSpacingAfter(16f);
                        // odst�p miedzy liniami w akapicie:
                        paragraf.setLeading(14f);
                        // wci�cie pierwszej linii akapitu:
                        paragraf.setFirstLineIndent(30f);
                        // czcionka dla akapitu:
                        paragraf.setFont(fnt12n);

                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        date = new Date();
        Date stopDate = new Timestamp(date.getTime());
        System.out.println("Stop:  " + stopDate);
        long diffInMs = stopDate.getTime() - startDate.getTime();
        float diffInSec = diffInMs / 1000.00f;
        System.out.format("Czas przetwarzenia pliku XML: %.2f s.", diffInSec);
        System.exit(0);

        if (isWin) {
            outputW.close();
        } else
            outputZ.close();
    }
}
