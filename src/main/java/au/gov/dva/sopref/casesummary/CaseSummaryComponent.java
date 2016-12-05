package au.gov.dva.sopref.casesummary;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

abstract class CaseSummaryComponent {

    abstract void addToDocument(XWPFDocument document);

}
