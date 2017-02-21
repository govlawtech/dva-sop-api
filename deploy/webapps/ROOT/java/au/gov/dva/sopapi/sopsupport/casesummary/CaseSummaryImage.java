package au.gov.dva.sopapi.sopsupport.casesummary;

import com.google.common.collect.ImmutableList;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CaseSummaryImage extends CaseSummaryComponent {

    private byte[] pngImageData;

    public CaseSummaryImage(ImmutableList<byte[]> pngImageDataList) {
        this.pngImageData = pngImageDataList.get(0);
    }

    @Override
    void addToDocument(XWPFDocument document) {
        CaseSummaryParagraph paragraph = new CaseSummaryParagraph("");
        paragraph.addToDocument(document);
        XWPFParagraph para = paragraph.getXWPFParagraph();

        XWPFRun run = para.createRun();

        try {
            run.addPicture(new ByteArrayInputStream(pngImageData), Document.PICTURE_TYPE_PNG, "timeline.png", Units.pixelToEMU(400), Units.pixelToEMU(1000));
        } catch (InvalidFormatException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
