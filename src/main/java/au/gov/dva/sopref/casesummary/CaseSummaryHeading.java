package au.gov.dva.sopref.casesummary;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class CaseSummaryHeading extends CaseSummaryComponent {

    private String _headingText = "";
    private String _styleId = "";

    public CaseSummaryHeading(String headingText, String styleId) {
        _headingText = headingText;
        _styleId = styleId;
    }

    @Override
    public void addToDocument(XWPFDocument document) {
        CaseSummaryParagraph paragraph = new CaseSummaryParagraph(_headingText);
        paragraph.addToDocument(document);
        paragraph.getXWPFParagraph().setStyle(_styleId);
    }

}
