package au.gov.dva.sopsupport.casesummary;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

abstract class CaseSummaryComponent {

    abstract void addToDocument(XWPFDocument document);

}
