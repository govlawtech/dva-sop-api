package au.gov.dva.sopapi.sopsupport.casesummary;

import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.exceptions.CaseSummaryRuntimeException;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.interfaces.model.casesummary.CaseSummaryModel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;

public class CaseSummary {
    private static CaseSummaryModel _model;
    private static CaseTrace _standaloneCaseTrace;
    private static CTStyles _ctStyles = CTStyles.Factory.newInstance();
    private static XWPFNumbering _numbering;

    public static CompletableFuture<byte[]> createCaseSummary(CaseSummaryModel caseSummaryModel, Predicate<Deployment> isOperational, boolean convertToPdf) {
        _model = caseSummaryModel;
        _standaloneCaseTrace = null;
        return CompletableFuture.supplyAsync(() -> buildCaseSummary(isOperational, convertToPdf));
    }

    public static CompletableFuture<byte[]> createCaseSummary(CaseTrace caseTrace, Predicate<Deployment> isOperational, boolean convertToPdf) {
        _model = null;
        _standaloneCaseTrace = caseTrace;
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
        CaseSummarySection recommendationSection = createRecommendationSection();
        CaseSummarySection documentSection = createDocumentSection();
        if (_model == null) {
            CaseSummarySection sopSection = createSopSectionWithNoModel();
            documentSection.add(sopSection);
        }
        else {
            CaseSummarySection conditionSection = createConditionSection();
            CaseSummarySection sopSection = createSopSection();
            CaseSummarySection serviceHistorySection = createServiceHistorySection(isOperational);

            documentSection.add(conditionSection);
            documentSection.add(sopSection);
            documentSection.add(serviceHistorySection);
        }

        // Add all sections to the case summary
        recommendationSection.addToDocument(document);
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
            throw new CaseSummaryRuntimeException(e);
        } catch (IOException e) {
            throw new CaseSummaryRuntimeException(e);
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
            throw new CaseSummaryRuntimeException(e);
        } catch (XmlException e) {
            throw new CaseSummaryRuntimeException(e);
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

    private static CaseSummarySection createRecommendationSection() {
        CaseSummarySection recommendationSection = new CaseSummarySection();
        if (_model == null) {
            recommendationSection.add(new CaseSummaryHeading("RECOMMENDATION TO DELEGATE", "RecommendationReviewHeading1"));
            recommendationSection.add(new CaseSummaryParagraph(Recommendation.STP_NOT_APPLICABLE.toString(), "RecommendationReviewNormal"));
        }
        else {
            boolean acceptClaim = _model.getRecommendation() == Recommendation.APPROVED;
            String recommendationHeadingStyle = acceptClaim ? "RecommendationPositiveHeading1" : "RecommendationReviewHeading1";
            String recommendationTextStyle = acceptClaim ? "RecommendationPositiveNormal" : "RecommendationReviewNormal";

            recommendationSection.add(new CaseSummaryHeading("RECOMMENDATION TO DELEGATE", recommendationHeadingStyle));
            recommendationSection.add(new CaseSummaryParagraph(_model.getRecommendation().toString(), recommendationTextStyle));
        }

        return recommendationSection;
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
        String onsetParagraph = "The date of onset for this condition is " + onset + ".";
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

        serviceHistoryData.add(new CaseSummaryParagraph("The total number of days on Warlike/Non-Warlike service is " + _model.getCaseTrace().getActualOperationalDays().orElse(0)));
        serviceHistoryData.add(new CaseSummaryParagraph("The total number of days of continuous full time service is " + _model.getCaseTrace().getActualCftsDays().orElse(0)));

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

            ImmutableList<byte[]> pieImages;
            CaseTrace caseTrace = _model.getCaseTrace();
            if (caseTrace.getActualOperationalDays().isPresent() && caseTrace.getActualCftsDays().isPresent()) {
                int warDays = caseTrace.getActualOperationalDays().get();
                int peaceDays = caseTrace.getActualCftsDays().get() - warDays;
                pieImages = ServiceTypePie.createPieImages(warDays, peaceDays);
            }
            else {
                // Alternative calculation method - will miss peace days:
                pieImages = ServiceTypePie.createPieImages(serviceHistory.getServices().asList().get(0), isOperational);
            }
            CaseSummaryImage pieImage = new CaseSummaryImage(pieImages, 600, 500);

            return Arrays.asList(timelineImage, pieImage);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();

    }

    private static CaseSummarySection createSopSectionWithNoModel() {
        CaseSummarySection sopData = new CaseSummarySection();
        ImmutableList<String> abortReasoning = _standaloneCaseTrace.getReasoningFor(ReasoningFor.ABORT_PROCESSING);

        // Rationale
        sopData.add(new CaseSummaryHeading("RATIONALE", "Heading2"));
        sopData.add(new CaseSummaryParagraph("Straight through processing couldn't proceed because: "));
        if (abortReasoning.size() > 0) {
            for (String reason : abortReasoning) {
                sopData.add(new CaseSummaryParagraph(reason));
            }
        }
        else {
            sopData.add(new CaseSummaryParagraph("Unknown system error"));
        }
        return sopData;
    }

    private static CaseSummarySection createSopSection() {
        SoP sop = _model.getApplicableSop();
        ImmutableSet<Factor> factors = _model.getFactorsConnectedToService();
        CaseTrace caseTrace = _model.getCaseTrace();
        ImmutableList<String> abortReasoning = caseTrace.getReasoningFor(ReasoningFor.ABORT_PROCESSING);
        ImmutableList<String> standardOfProofReasoning = caseTrace.getReasoningFor(ReasoningFor.STANDARD_OF_PROOF);
        ImmutableList<String> factorsReasoning = caseTrace.getReasoningFor(ReasoningFor.MEETING_FACTORS);
        boolean usingRh = caseTrace.getApplicableStandardOfProof().get() == StandardOfProof.ReasonableHypothesis;
        ImmutableList<Factor> consideredFactors = usingRh ? caseTrace.getRhFactors() : caseTrace.getBopFactors();

        // Statement of principles
        CaseSummarySection sopSection = new CaseSummarySection();
        sopSection.add(new CaseSummaryHeading("STATEMENT OF PRINCIPLES", "Heading2"));

        CaseSummarySection sopData = new CaseSummarySection();

        // Citation
        String sopParagraph = "The relevant Statement of Principles is the " +
                sop.getCitation() + ". The standard of proof for this instrument is the " +
                sop.getStandardOfProof() + ".";
        sopData.add(new CaseSummaryParagraph(sopParagraph));

        String legislationParagraph = "This instrument is available on the Federal " +
                "Register of Legislative Instruments at:";
        sopData.add(new CaseSummaryParagraph(legislationParagraph));

        String url = "https://www.legislation.gov.au/Latest/" + sop.getRegisterId();
        sopData.add(new CaseSummaryHyperlink(url));

        // Connection to service
        sopData.add(new CaseSummaryHeading("CONNECTION TO SERVICE", "Heading3"));

        if (factors.size() > 0) {
            String connectionParagraph = (factors.size() > 1)
                ? "The factors that were used to connect the condition to service are:"
                : "The factor that was used to connect the condition to service is:";
            sopData.add(new CaseSummaryParagraph(connectionParagraph));
            for (Factor factor : factors) {
                sopData.add(new CaseSummaryParagraph(factor.getParagraph() + ": " + factor.getText()));
            }
        }
        else {
            sopData.add(new CaseSummaryParagraph("No factors were able to be used for straight through processing to connect the condition to service"));
        }

        // Rationale
        sopData.add(new CaseSummaryHeading("RATIONALE", "Heading3"));
        if (abortReasoning.size() > 0) {
            for (String reason : abortReasoning) {
                sopData.add(new CaseSummaryParagraph(reason));
            }
        }
        else {
            if (standardOfProofReasoning.size() > 0) {
                sopData.add(new CaseSummaryParagraph(String.format("The standard of proof is %s, because:", caseTrace.getApplicableStandardOfProof().get().toString())));
                for (String reason : standardOfProofReasoning) {
                    sopData.add(new CaseSummaryParagraph(reason));
                }
            }

            if (factors.size() > 0) {
                String deemingParagraph = (factors.size() > 1)
                    ? "The factors above were deemed to be met, because:"
                    : "The factor above was deemed to be met, because:";

                sopData.add(new CaseSummaryParagraph(deemingParagraph));
                for (String reason : factorsReasoning) {
                    sopData.add(new CaseSummaryParagraph(reason));
                }
            }
            else {
                String considerationParagraph = (factors.size() > 1)
                    ? "The following factors were considered:"
                    : "The following factor was considered:";
                sopData.add(new CaseSummaryParagraph(considerationParagraph));
                for (Factor factor : consideredFactors) {
                    sopData.add(new CaseSummaryParagraph(factor.getParagraph() + ": " + factor.getText()));
                }
                String notMetParagraph = (factors.size() > 1)
                    ? "But were considered not met, because:"
                    : "But was considered not met, because:";
                sopData.add(new CaseSummaryParagraph(notMetParagraph));
                for (String reason : factorsReasoning) {
                    sopData.add(new CaseSummaryParagraph(reason));
                }
            }
            if (!usingRh && caseTrace.getActualOperationalDays().orElse(0) > 0) {
                String extraRhConsideration = (factors.size() > 1)
                    ? "Additionally, the following RH factors were deemed not to be applicable, as the RH standard of proof was not considered applicable:"
                    : "Additionally, the following RH factor was deemed not to be applicable, as the RH standard of proof was not considered applicable:";
                sopData.add(new CaseSummaryParagraph(extraRhConsideration));
                for (Factor factor : caseTrace.getRhFactors()) {
                    sopData.add(new CaseSummaryParagraph(factor.getParagraph() + ": " + factor.getText()));
                }
            }
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

    private static String getDatesAsRange(LocalDate startDate, LocalDate endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (startDate.isEqual(endDate))
        {
            return startDate.format(formatter);
        }

        return startDate.format(formatter) + " to " + endDate.format(formatter);
    }
}
