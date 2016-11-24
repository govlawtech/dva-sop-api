package au.gov.dva.sopref.casesummary;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

abstract class CaseSummaryComponent {

    public abstract IBodyElement addToDocument(XWPFDocument document);

}
