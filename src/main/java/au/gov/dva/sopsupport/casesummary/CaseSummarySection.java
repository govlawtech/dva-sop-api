package au.gov.dva.sopsupport.casesummary;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.util.ArrayList;

class CaseSummarySection extends CaseSummaryComponent {

    private ArrayList<CaseSummaryComponent> _children;

    CaseSummarySection() {
        _children = new ArrayList<>();
    }

    @Override
    void addToDocument(XWPFDocument document) {
        for (CaseSummaryComponent child : _children) {
            child.addToDocument(document);
        }
    }

    void add(CaseSummaryComponent component) {
        _children.add(component);
    }
}
