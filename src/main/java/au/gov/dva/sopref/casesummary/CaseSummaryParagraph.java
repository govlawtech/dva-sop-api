package au.gov.dva.sopref.casesummary;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class CaseSummaryParagraph extends CaseSummaryComponent {

    private XWPFParagraph _paragraph;
    private String _text = "";

    public CaseSummaryParagraph(String text) {
        _text = text;
    }

    @Override
    public IBodyElement addToDocument(XWPFDocument document) {
        _paragraph = document.createParagraph();
        XWPFRun run = _paragraph.createRun();
        run.setText(_text);

        return _paragraph;
    }
}
