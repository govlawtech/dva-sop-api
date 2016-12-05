package au.gov.dva.sopref.casesummary;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

abstract class CaseSummaryComponent {

    public abstract void addToDocument(XWPFDocument document);

}
