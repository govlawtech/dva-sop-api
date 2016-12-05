package au.gov.dva.sopref.casesummary;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;

import java.math.BigInteger;
import java.util.List;

public class CaseSummaryParagraph extends CaseSummaryComponent {

    private XWPFParagraph _paragraph;
    private String _text = "";
    private boolean _hasBullet = false;

    public static BigInteger bulletNumId;
    public static BigInteger bulletILvl;


    public CaseSummaryParagraph(String text) {
        _text = text;
    }

    public void hasBullet(boolean hasBullet) {
        _hasBullet = hasBullet;
    }

    @Override
    public void addToDocument(XWPFDocument document) {
        _paragraph = document.createParagraph();

        if (_hasBullet) {
            createBullet();
        }

        XWPFRun run = _paragraph.createRun();
        run.setText(_text);
    }

    private void createBullet() {
        // <w:p w:rsidP="00FE3266" w:rsidR="00FE3266" w:rsidRDefault="00FE3266">
        //  <w:pPr>
        //    <w:pStyle w:val="ListParagraph"/>
        //    <w:numPr>
        //      <w:ilvl w:val="0"/>
        //      <w:numId w:val="1"/>
        //    </w:numPr>
        //  </w:pPr>
        //  <w:r>
        //    <w:t>Bullet 1</w:t>
        //  </w:r>

        CTPPr ctpPr = _paragraph.getCTP().addNewPPr();
        ctpPr.addNewPStyle().setVal("ListParagraph");

        CTNumPr ctNumPr = ctpPr.addNewNumPr();
        ctNumPr.addNewIlvl().setVal(bulletILvl);
        ctNumPr.addNewNumId().setVal(bulletNumId);
    }

    public XWPFParagraph getXWPFParagraph() {
        return _paragraph;
    }
}
