package au.gov.dva.sopref.casesummary;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.util.ArrayList;

public class CaseSummarySection extends CaseSummaryComponent {

    private ArrayList<CaseSummaryComponent> _children;

    public CaseSummarySection() {
        _children = new ArrayList<>();
    }

    @Override
    public void addToDocument(XWPFDocument document) {
        for (CaseSummaryComponent child : _children) {
            child.addToDocument(document);
        }
    }

    public void add(CaseSummaryComponent component) {
        _children.add(component);
    }
}
