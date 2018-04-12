package music;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class PageEvent extends PdfPageEventHelper {
    protected String page, txt;
    protected int shift = 10; // przesuni�cie
    protected BaseFont bf;
    Chunk chunk;

    public void onEndPage(PdfWriter wr, Document pdf) {
        PdfContentByte cb = wr.getDirectContent();
        int pageNumAbs = wr.getPageNumber();

        page = "Strona: " + String.format("%d", pageNumAbs);
        chunk = new Chunk(page, (new Font(bf, 9f, Font.UNDERLINE, BaseColor.BLACK)));
        cb.saveState();
        cb.beginText();
        cb.moveText(pdf.right() - chunk.getWidthPoint(), pdf.bottom() - shift);
        cb.setFontAndSize(bf, 9f);
        cb.showText(page);
        cb.endText();

        cb.beginText();
        cb.moveText(pdf.left(), pdf.bottom() - shift);
        cb.showText(txt);
        cb.endText();

        // Linia pozioma
        cb.setLineWidth(0.5f);
        cb.moveTo(pdf.right(), pdf.bottom() - shift + 10);
        cb.lineTo(pdf.left(), pdf.bottom() - shift + 10);
        cb.stroke();
        cb.restoreState();
    }

    public void setBaseFonts(BaseFont bf) {
        this.bf = bf;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }
}
