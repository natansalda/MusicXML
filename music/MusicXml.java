package music;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.ibm.jzos.ZFile;

import music.ObjectFactory;
import music.Music;
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
		PrintWriter outputW = null;

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

		if (isWin == false) {
			// z/OS:
			inputZ = new ZFile(args[0], "rt"); // "rt" - readtext
			InputStream inpStream = inputZ.getInputStream();
			InputStreamReader streamRdr = new InputStreamReader(inpStream, "CP870");

			outputZ = new ZFile(args[1], "wb,type=record,noseek");
			music = (Music) unmarsh.unmarshal(streamRdr);
		} else {
			// Windows:
			inputW = new File(args[0]);
			music = (Music) unmarsh.unmarshal(inputW);
			outputW = new PrintWriter(args[1]);
		}

		List<Artist> listaArtystow = music.getArtist();
		for (Artist artysta : listaArtystow) {
			List<Album> listaAlbumow = artysta.getAlbum();
			for (Album album : listaAlbumow) {
				String opis = album.getDescription().toString();
				List<Song> listaPiosenek = album.getSong();
				System.out.println("Description: " + sep + opis);
				for (Song piosenka : listaPiosenek) {
					line = artysta.getName() + sep + album.getTitle() + "\n Description: " + sep + opis + "\n Songs: "
							+ sep + album.getSong();

					if (isWin == false) {
						outputZ.write(line.getBytes("CP870"));
						System.out.println(line);
					} else
						outputW.println(line);
				}
			}
		}

		if (isWin)
			outputW.close();
		else
			outputZ.close();

		date = new Date();
		Date stopDate = new Timestamp(date.getTime());
		System.out.println("Stop:  " + stopDate);
		long diffInMs = stopDate.getTime() - startDate.getTime();
		float diffInSec = diffInMs / 1000.00f;
		System.out.format("Czas przetwarzenia pliku XML: %.2f s.", diffInSec);
		System.exit(0);
	}
}
