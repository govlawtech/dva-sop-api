package au.gov.dva.sopref.casesummary;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;

import java.math.BigInteger;
import java.util.List;

public class CaseSummaryParagraph extends CaseSummaryComponent {

    private XWPFParagraph _paragraph;
    private String _text = "";

    public CaseSummaryParagraph(String text) {
        _text = text;
    }

    @Override
    public void addToDocument(XWPFDocument document) {
        _paragraph = document.createParagraph();
        XWPFRun run = _paragraph.createRun();
        run.setText(_text);

        CTPPr ctpPr = _paragraph.getCTP().addNewPPr();
        ctpPr.addNewPStyle().setVal("ListParagraph");

        CTNumPr ctNumPr = ctpPr.addNewNumPr();
        ctNumPr.addNewIlvl().setVal(BigInteger.valueOf(0));
        ctNumPr.addNewNumId().setVal(BigInteger.valueOf(1));

        int x = 0;

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

//  <xml-fragment xmlns:main="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
//  <main:pPr>
//    <main:pStyle main:val="ListParagraph"/>
//    <main:numPr>
//      <main:ilvl main:val="0"/>
//      <main:numId main:val="1"/>
//    </main:numPr>
//  </main:pPr>
//  <main:r>
//    <main:t>CASE SUMMARY</main:t>
//  </main:r>
//</xml-fragment>
    }

    public XWPFParagraph getXWPFParagraph() {
        return _paragraph;
    }
}
