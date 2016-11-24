package au.gov.dva.sopref.casesummary;

import au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CaseSummary {

//    private List<CaseSummarySection> _sections = new ArrayList<CaseSummarySection>();
//    private XWPFDocument _document;
//    private CaseSummaryModel _model;

    public static CompletableFuture<byte[]> createCaseSummary(CaseSummaryModel caseSummaryModel) {
        // For each element in the model, convert to Apache POI component

        // TODO: Work out nicer way to load from resources directory
        String path = "C:\\Code\\DVA\\dva-sop-api\\src\\main\\resources\\docs\\";

        try {
//            FileInputStream inputStream = new FileInputStream(path + "\\Case Summary Template.docx");
            FileInputStream inputStream = (FileInputStream) CaseSummary.class.getClassLoader().getResourceAsStream("docs/Case Summary Template.docx");
            XWPFDocument template = new XWPFDocument(inputStream);

            String pathToTemplate = "docs/Case Summary Template.docx";
            String pathToDocs = CaseSummary.class.getResource(pathToTemplate).getPath();


            // Get styles from the template document so they can be applied to generated document
            XWPFStyles styles = template.getStyles();
            XWPFStyle heading1Style = styles.getStyle("Heading1");
            XWPFStyle heading2Style = styles.getStyle("Heading2");
            XWPFStyle heading3Style = styles.getStyle("Heading3");
            XWPFStyle heading4Style = styles.getStyle("Heading4");

            List<XWPFStyle> stylesToImport = new ArrayList<XWPFStyle>();
            stylesToImport.add(heading1Style);
            stylesToImport.add(heading2Style);
            stylesToImport.add(heading3Style);
            stylesToImport.add(heading4Style);

            inputStream.close();

            // Set up generated document
            XWPFDocument document = new XWPFDocument();
            FileOutputStream outputStream = new FileOutputStream(new File(path + "\\Case Summary.docx"));
            document.createStyles();

            // Import styles
            for (XWPFStyle style : stylesToImport) {
                List<XWPFStyle> usedStyles = styles.getUsedStyleList(style);

                for (XWPFStyle s : usedStyles) {
                    document.getStyles().addStyle(s);
                }
            }

            CaseSummaryHeading documentHeading = new CaseSummaryHeading("CASE SUMMARY", "Heading1");
            CaseSummaryHeading conditionHeading = new CaseSummaryHeading("CLAIMED CONDITION", "Heading2");

//            CaseSummaryCondition condition = new CaseSummaryCondition();
//            condition.setName("Joint instability");
//            condition.setICDCode("ICD-1234");
//            condition.setType("Accumulated over time (wear and tear)");
//            condition.setOnsetStartDate(LocalDate.of(2009, 12, 1));

//            CaseSummaryModel model = new au.gov.dva.sopref.casesummary.CaseSummaryModel(condition, null, null);

//            CaseSummary.createConditionSection(document, model);

            CaseSummaryHeading serviceHistoryHeading = new CaseSummaryHeading("SERVICE HISTORY", "Heading2");
            CaseSummaryHeading sopHeading = new CaseSummaryHeading("STATEMENT OF PRINCIPLES", "Heading2");

            documentHeading.addToDocument(document);
            conditionHeading.addToDocument(document);
            serviceHistoryHeading.addToDocument(document);
            sopHeading.addToDocument(document);

            document.write(outputStream);
            outputStream.close();

        } catch (FileNotFoundException exception) {
           exception.printStackTrace();
        } catch (IOException exception) {
           exception.printStackTrace();
        }

        return null;
    }

//    private static void createConditionSection(XWPFDocument document, CaseSummaryModel model) {
//        CaseSummaryHeading conditionHeading = new CaseSummaryHeading("CLAIMED CONDITION", "Heading2");
//        conditionHeading.addToDocument(document);
//
//
//        model.getCondition()
//    }
}
