package au.gov.dva.sopapi.sopsupport.casesummary;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

class CaseSummaryHeading extends CaseSummaryComponent {

    private String _headingText = "";
    private String _styleId = "";

    CaseSummaryHeading(String headingText, String styleId) {
        _headingText = headingText;
        _styleId = styleId;
    }

    @Override
    void addToDocument(XWPFDocument document) {
        CaseSummaryParagraph paragraph = new CaseSummaryParagraph(_headingText);
        paragraph.addToDocument(document);
        paragraph.getXWPFParagraph().setStyle(_styleId);
    }

}
