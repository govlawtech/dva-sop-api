package au.gov.dva.sopapi.sopsupport.casesummary;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.exceptions.CaseSummaryError;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.interfaces.model.casesummary.CaseSummaryModel;
import com.google.common.collect.ImmutableList;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.commons.lang.WordUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNum;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;

public class CaseSummary {

    private static CaseSummaryModel _model;
    private static CTStyles _ctStyles = CTStyles.Factory.newInstance();
    private static XWPFNumbering _numbering;

    public static CompletableFuture<byte[]> createCaseSummary(CaseSummaryModel caseSummaryModel, Predicate<Deployment> isOperational, boolean convertToPdf) {
        _model = caseSummaryModel;
        return CompletableFuture.supplyAsync(() -> buildCaseSummary(isOperational, convertToPdf));
    }

    // Extension of XWPF document needed to sort out errors in the pdf converter
    private static class SopApiXWPFDocument extends XWPFDocument {
        public SopApiXWPFDocument() {
            super();
        }

        @Override
        protected void onDocumentCreate() {
            super.onDocumentCreate();
            CTDocument1 doc = this.getDocument();
            if (!doc.isSetBody()) doc.addNewBody();
            CTBody body = doc.getBody();
            if (!body.isSetSectPr()) body.addNewSectPr();
            CTSectPr sectPr = body.getSectPr();
            if (!sectPr.isSetPgSz()) sectPr.addNewPgSz();
            CTPageSz pageSz = sectPr.getPgSz();
            if (!pageSz.isSetH()) pageSz.setH(BigInteger.valueOf(297 * 80)); // A4 dimensions x 80. Not sure what the units here are supposed to be
            if (!pageSz.isSetW()) pageSz.setW(BigInteger.valueOf(210 * 80));
        }

        @Override
        public CTStyles getStyle() throws XmlException, IOException {
            return _ctStyles;
        }
    }

    private static byte[] buildCaseSummary(Predicate<Deployment> isOperational, boolean convertToPdf) {
        XWPFDocument document = convertToPdf ? new SopApiXWPFDocument() : new XWPFDocument();
        document.createHeaderFooterPolicy();

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
        CaseSummarySection sopSection = createSopSection();
        CaseSummarySection serviceHistorySection = createServiceHistorySection(isOperational);

        documentSection.add(conditionSection);
        documentSection.add(sopSection);
        documentSection.add(serviceHistorySection);

        // Add all sections to the case summary
        documentSection.addToDocument(document);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            if (convertToPdf) {
                PdfOptions pdfOptions = PdfOptions.create().fontEncoding("UTF-8");
                PdfConverter.getInstance().convert(document, outputStream, pdfOptions);
            }
            else {
                document.write(outputStream);
            }

            outputStream.close();
        } catch (FileNotFoundException e) {
            throw new CaseSummaryError(e);
        } catch (IOException e) {
            throw new CaseSummaryError(e);
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
            throw new CaseSummaryError(e);
        } catch (XmlException e) {
            throw new CaseSummaryError(e);
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
        String conditionParagraph = "The claimed condition is " + _model.getApplicableSop().getConditionName() + ".";
        conditionData.add(new CaseSummaryParagraph(conditionParagraph));

        conditionData.add(new CaseSummaryHeading("DATE OF ONSET", "Heading2"));
        String onset = getDatesAsRange(condition.getStartDate(), condition.getEndDate());
        String onsetParagraph = "This condition related to an incident dated " + onset + ".";
        conditionData.add(new CaseSummaryParagraph(onsetParagraph));

        conditionSection.add(conditionData);

        return conditionSection;
    }

    private static CaseSummarySection createServiceHistorySection(Predicate<Deployment> isOperational) {
        ServiceHistory serviceHistory = _model.getServiceHistory();

        CaseSummarySection serviceHistorySection = new CaseSummarySection();
        serviceHistorySection.add(new CaseSummaryHeading("SERVICE HISTORY", "Heading2"));

        CaseSummarySection serviceHistoryData = new CaseSummarySection();

        String serviceHistoryDefinition = "The service history as defined in Section 6(1) of the " +
                "Military Rehabilitation and Compensation Act 2004 is:";
        serviceHistoryData.add(new CaseSummaryParagraph(serviceHistoryDefinition));

        for (Service service : serviceHistory.getServices()) {
            for (Deployment deployment : service.getDeployments()) {
                String typeOfServiceText = isOperational.test(deployment) ? "Warlike/Non-Warlike" : "Peacetime";
                String operationText = WordUtils.capitalize(typeOfServiceText) +
                        " service on " +
                        deployment.getOperationName() +
                        " from " +
                        getDatesAsRange(deployment.getStartDate(), deployment.getEndDate());

                CaseSummaryParagraph operationParagraph = new CaseSummaryParagraph(operationText);
                operationParagraph.hasBullet(true);

                serviceHistoryData.add(operationParagraph);
            }
        }

        serviceHistorySection.add(serviceHistoryData);

        serviceHistorySection.addAll(createServiceImages(serviceHistory, isOperational));

        return serviceHistorySection;
    }

    private static Collection<CaseSummaryImage> createServiceImages(ServiceHistory serviceHistory, Predicate<Deployment> isOperational) {
        try {
            ImmutableList<byte[]> timelineImages = Timeline.createTimelineImages(serviceHistory.getServices().asList().get(0), isOperational);
            CaseSummaryImage timelineImage = new CaseSummaryImage(timelineImages, 400, 1000);

            ImmutableList<byte[]> pieImages = ServiceTypePie.createPieImages(serviceHistory.getServices().asList().get(0), isOperational);
            CaseSummaryImage pieImage = new CaseSummaryImage(pieImages, 600, 500);

            return Arrays.asList(timelineImage, pieImage);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();

    }

    private static CaseSummarySection createSopSection() {
        SoP sop = _model.getApplicableSop();

        CaseSummarySection sopSection = new CaseSummarySection();
        sopSection.add(new CaseSummaryHeading("STATEMENT OF PRINCIPLES", "Heading2"));

        CaseSummarySection sopData = new CaseSummarySection();

        String sopParagraph = "The relevant Statement of Principles is the " +
                sop.getCitation() + ". The standard of proof for this instrument is the " +
                sop.getStandardOfProof() + ".";
        sopData.add(new CaseSummaryParagraph(sopParagraph));

        sopData.add(new CaseSummaryParagraph(String.format(
                "The required number of days of operational service for Reasonable Hypothesis is %d, and the client has %d days of operational service.\n"
                , _model.getCaseTrace().getRequiredOperationalDaysForRh().get(), _model.getCaseTrace().getActualOperationalDays().get()
        )));

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

        sopData.add(new CaseSummaryParagraph(String.format(
                "This is because the required number of days of continuous full time service is %d, and the client has %d days of continuous full time service.\n"
                , _model.getCaseTrace().getRequiredCftsDays().get(), _model.getCaseTrace().getActualCftsDays().get()
        )));

        if (_model.getFactorsConnectedToService().size() > 0 && sop.getStandardOfProof() == StandardOfProof.BalanceOfProbabilities) {
            sopData.add(new CaseSummaryParagraph("The corresponding RH factors NOT met were:"));
            for (Factor factor : _model.getCaseTrace().getRhFactors()) {
                sopData.add(new CaseSummaryParagraph(factor.getParagraph() + ": " + factor.getText()));
            }
        }

        sopSection.add(sopData);

        return sopSection;
    }

    private static String getDatesAsRange(OffsetDateTime startDate, Optional<OffsetDateTime> endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return endDate.isPresent() ?
                startDate.format(formatter) + " to " + endDate.get().format(formatter) :
                startDate.format(formatter);
    }

    private static String getDatesAsRange(OffsetDateTime startDate, OffsetDateTime endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (startDate.isEqual(endDate))
        {
            return startDate.format(formatter);
        }

        return startDate.format(formatter) + " to " + endDate.format(formatter);
    }
}
