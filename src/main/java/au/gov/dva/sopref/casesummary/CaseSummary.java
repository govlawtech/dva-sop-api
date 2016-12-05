package au.gov.dva.sopref.casesummary;

import au.gov.dva.sopref.interfaces.model.*;
import au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CaseSummary {

    private static CaseSummaryModel _model;
    private static CTStyles _ctStyles = CTStyles.Factory.newInstance();
    private static XWPFNumbering _numbering;

    public static CompletableFuture<byte[]> createCaseSummary(CaseSummaryModel caseSummaryModel) {
        _model = caseSummaryModel;
        return CompletableFuture.supplyAsync(CaseSummary::buildCaseSummary);
    }

    private static byte[] buildCaseSummary() {
        XWPFDocument document = new XWPFDocument();

        // Set up generated document with styles from the template
        getStylesAndNumberingFromTemplate();
        document.createStyles().setStyles(_ctStyles);

        // Numbering is required for bullets and lists
        document.createNumbering();

        // Can't get the CTNumbering so need to create manually
        transferTemplateNumbering(document.getNumbering());

        // Create the main sections
        CaseSummarySection documentSection = createDocumentSection();
        CaseSummarySection conditionSection = createConditionSection();
        CaseSummarySection serviceHistorySection = createServiceHistorySection();
        CaseSummarySection sopSection = createSopSection();

        documentSection.add(conditionSection);
        documentSection.add(serviceHistorySection);
        documentSection.add(sopSection);

        // Add all sections to the case summary
        documentSection.addToDocument(document);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            document.write(outputStream);
            outputStream.close();
        } catch (FileNotFoundException exception) {
            // TODO: Log exception
        } catch (IOException exception) {
            // TODO: Log exception
        }

        return outputStream.toByteArray();
    }

    private static void getStylesAndNumberingFromTemplate() {
        String templatePath = "docs/Case Summary Template.docx";
        InputStream inputStream = CaseSummary.class.getClassLoader().getResourceAsStream(templatePath);

        try (XWPFDocument template = new XWPFDocument(inputStream);) {
            _ctStyles = template.getStyle();
            _numbering = template.getNumbering();
        } catch (IOException e) {
            // TODO: Log exception
        } catch (XmlException e) {
            // TODO: Log exception
        }
    }

    private static void setDefaultBullet(int numId, List<CTLvl> ctLvls) {
        BigInteger lowestLeft = BigInteger.valueOf(Integer.MAX_VALUE);

        for (CTLvl ctLvl : ctLvls) {
            CTInd ctInd = ctLvl.getPPr().getInd();

            if (ctLvl.getNumFmt().getVal() == STNumberFormat.BULLET) {
                // Look for CTLvl with the lowest left value
                BigInteger left = ctInd.getLeft();

                if (left.compareTo(lowestLeft) == -1 ) {
                    lowestLeft = left;
                    CaseSummaryParagraph.bulletNumId = BigInteger.valueOf(numId);
                    CaseSummaryParagraph.bulletILvl = ctLvl.getIlvl();
                }
            }
        }
    }

    private static void transferTemplateNumbering(XWPFNumbering numbering) {
        // <w:abstractNum w:abstractNumId="1">
        // </w:abstractNum>
        // <w:num w:numId="1">
        //   <w:abstractNumId w:val="1"/>
        // </w:num>

        // numId begins at 1
        int i = 1;

        while (_numbering.getNum(BigInteger.valueOf(i)) != null) {
            XWPFNum num = _numbering.getNum(BigInteger.valueOf(i));
            BigInteger abstractNumId = num.getCTNum().getAbstractNumId().getVal();

            setDefaultBullet(i, _numbering.getAbstractNum(abstractNumId).getCTAbstractNum().getLvlList());

            numbering.addAbstractNum(_numbering.getAbstractNum(abstractNumId));
            numbering.addNum(abstractNumId, BigInteger.valueOf(i));

            i++;
        }
    }

    private static CaseSummarySection createDocumentSection() {
        CaseSummarySection documentSection = new CaseSummarySection();
        documentSection.add(new CaseSummaryHeading("CASE SUMMARY", "Heading1"));

        return documentSection;
    }

    private static CaseSummarySection createConditionSection() {
        Condition condition = _model.getCondition();

        CaseSummarySection conditionSection = new CaseSummarySection();
        CaseSummarySection conditionData = new CaseSummarySection();

        conditionData.add(new CaseSummaryHeading("CLAIMED CONDITION", "Heading2"));
        String conditionParagraph = "The claimed condition is " + condition.getName() + ".";
        conditionData.add(new CaseSummaryParagraph(conditionParagraph));

        conditionData.add(new CaseSummaryHeading("DATE OF ONSET", "Heading2"));
        String onset = getDatesAsRange(condition.getOnsetStartDate(), condition.getOnsetEndDate());
        String onsetParagraph = "This condition related to an incident dated " + onset + ".";
        conditionData.add(new CaseSummaryParagraph(onsetParagraph));

        conditionSection.add(conditionData);

        return conditionSection;
    }

    private static CaseSummarySection createServiceHistorySection() {
        ServiceHistory serviceHistory = _model.getServiceHistory();

        CaseSummarySection serviceHistorySection = new CaseSummarySection();
        serviceHistorySection.add(new CaseSummaryHeading("SERVICE HISTORY", "Heading2"));

        CaseSummarySection serviceHistoryData = new CaseSummarySection();

        String serviceHistoryDefinition = "The service history as defined in Section 6(1) of the " +
                "Military Rehabilitation and Compensation Act 2004 is:";
        serviceHistoryData.add(new CaseSummaryParagraph(serviceHistoryDefinition));

        for (Service service : serviceHistory.getServices()) {
            for (Operation operation : service.getOperations()) {
                String operationNameText = operation.getServiceType() == ServiceType.WARLIKE ?
                        " on " + operation.getName() : "";

                String operationText = WordUtils.capitalize(operation.getServiceType().toString()) +
                        " service from " +
                        getDatesAsRange(operation.getStartDate(), operation.getEndDate()) +
                        operationNameText;

                CaseSummaryParagraph operationParagraph = new CaseSummaryParagraph(operationText);
                operationParagraph.hasBullet(true);

                serviceHistoryData.add(operationParagraph);
            }
        }

        serviceHistorySection.add(serviceHistoryData);

        return serviceHistorySection;
    }

    private static CaseSummarySection createSopSection() {
        SoP sop = _model.getSop();

        CaseSummarySection sopSection = new CaseSummarySection();
        sopSection.add(new CaseSummaryHeading("STATEMENT OF PRINCIPLES", "Heading2"));

        CaseSummarySection sopData = new CaseSummarySection();

        String sopParagraph = "The relevant Statement of Principles is the " +
                sop.getCitation() + ". The standard of proof for this instrument is the " +
                sop.getStandardOfProof() + ".";
        sopData.add(new CaseSummaryParagraph(sopParagraph));

        String legislationParagraph = "This instrument is available on the Federal " +
                "Register of Legislative Instruments at:";
        sopData.add(new CaseSummaryParagraph(legislationParagraph));

        String url = "https://www.legislation.gov.au/Latest/" + sop.getRegisterId();
        sopData.add(new CaseSummaryHyperlink(url));

        sopData.add(new CaseSummaryHeading("CONNECTION TO SERVICE", "Heading2"));
        String connectionParagraph = "The factors that were used to connect the condition " +
                "to service are:";
        sopData.add(new CaseSummaryParagraph(connectionParagraph));

        for (Factor factor : _model.getFactorsConnectedToService()) {
            sopData.add(new CaseSummaryParagraph(factor.getParagraph() + ": " + factor.getText()));
        }

        sopSection.add(sopData);

        return sopSection;
    }

    private static String getDatesAsRange(LocalDate startDate, Optional<LocalDate> endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return endDate.isPresent() ?
                startDate.format(formatter) + " to " + endDate.get().format(formatter) :
                startDate.format(formatter);
    }
}
