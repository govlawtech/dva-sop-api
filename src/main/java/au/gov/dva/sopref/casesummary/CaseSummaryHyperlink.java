package au.gov.dva.sopref.casesummary;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

class CaseSummaryHyperlink extends CaseSummaryComponent {

    private String _url = "";

    CaseSummaryHyperlink(String url) {
        _url = url;
    }

    @Override
    void addToDocument(XWPFDocument document) {
        CaseSummaryParagraph paragraph = new CaseSummaryParagraph("");
        paragraph.addToDocument(document);
        XWPFParagraph para = paragraph.getXWPFParagraph();

        // Add link as an external relationship
        String id = para.getDocument().getPackagePart()
                .addExternalRelationship(_url, XWPFRelation.HYPERLINK.getRelation()).getId();

        // Bind link to the external relationship
        CTHyperlink ctHyperlink = para.getCTP().addNewHyperlink();
        ctHyperlink.setId(id);

        // Create text to display
        CTText ctText = CTText.Factory.newInstance();
        ctText.setStringValue(_url);

        CTR ctr = CTR.Factory.newInstance();
        ctr.setTArray(new CTText[] {ctText});

        // Style the link
        CTRPr rpr = ctr.addNewRPr();
        CTColor colour = CTColor.Factory.newInstance();
        colour.setVal("0066cc");
        rpr.setColor(colour);
        CTRPr rpr1 = ctr.addNewRPr();
        rpr1.addNewU().setVal(STUnderline.SINGLE);

        // Associate text to display with link
        ctHyperlink.setRArray(new CTR[] {ctr});
    }

}
