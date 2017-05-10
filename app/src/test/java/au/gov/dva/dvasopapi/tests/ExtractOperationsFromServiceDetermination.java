package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.interfaces.model.ServiceType;
import au.gov.dva.sopapi.sopref.data.ServiceDeterminations;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.StoredOperation;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static au.gov.dva.dvasopapi.tests.TestUtils.actOdtOf;

public class ExtractOperationsFromServiceDetermination {

    @Test
    public void getWarlikeOps() throws IOException {
        URL inputDocx = Resources.getResource("F2016L00994.docx");
        byte[] pdfBytes = Resources.toByteArray(inputDocx);
        ImmutableList<Operation> results = ServiceDeterminations.extractOperations(pdfBytes);
        Assert.assertTrue(results.size() == 19);
    }

    @Test
    public void checkAllWarlikeOperationsExist() throws IOException {
        URL inputDocx = Resources.getResource("F2016L00994.docx");
        byte[] docxBytes = Resources.toByteArray(inputDocx);
        ImmutableList<Operation> results = ServiceDeterminations.extractOperations(docxBytes);

        Operation opEnduringFreedom = new StoredOperation("Enduring Freedom\u2014Afghanistan",
                actOdtOf(2001, 10, 7), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opSlipper = new StoredOperation("Slipper",
                actOdtOf(2001, 10, 11), Optional.of(actOdtOf(2009, 7, 30)),
                ServiceType.WARLIKE);

        Operation opAriki = new StoredOperation("Ariki",
                actOdtOf(2001, 12, 1), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opPalate = new StoredOperation("Palate",
                actOdtOf(2003, 4, 18), Optional.of(actOdtOf(2004, 7, 5)),
                ServiceType.WARLIKE);

        Operation opCatalyst = new StoredOperation("Catalyst",
                actOdtOf(2003, 7, 16), Optional.of(actOdtOf(2009, 7, 31)),
                ServiceType.WARLIKE);

        Operation opAthena = new StoredOperation("Athena",
                actOdtOf(2003, 7, 17), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opISAF = new StoredOperation("International Security Assistance Force",
                actOdtOf(2003, 8, 11), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opHerrick = new StoredOperation("Herrick",
                actOdtOf(2004, 9, 1), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opPalateII = new StoredOperation("Palate II",
                actOdtOf(2005, 6, 27), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opPaladin = new StoredOperation("Paladin",
                actOdtOf(2006, 7, 12), Optional.of(actOdtOf(2006, 8, 14)),
                ServiceType.WARLIKE);

        Operation opRiverbank = new StoredOperation("Riverbank",
                actOdtOf(2008, 7, 21), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opKruger = new StoredOperation("Kruger",
                actOdtOf(2009, 1, 1), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opSlipperII = new StoredOperation("Slipper",
                actOdtOf(2009, 7, 31), Optional.of(actOdtOf(2012, 2, 19)),
                ServiceType.WARLIKE);

        Operation opNNMEOAL = new StoredOperation("NNMEOAL",
                actOdtOf(2011, 3, 31), Optional.of(actOdtOf(2011, 10, 31)),
                ServiceType.WARLIKE);

        Operation opSlipperIII = new StoredOperation("Slipper",
                actOdtOf(2012, 2, 20), Optional.of(actOdtOf(2014, 6, 30)),
                ServiceType.WARLIKE);

        Operation opSlipperIV = new StoredOperation("Slipper",
                actOdtOf(2014, 7, 1), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opOkra = new StoredOperation("Okra",
                actOdtOf(2014, 8, 9), Optional.of(actOdtOf(2015, 9, 8)),
                ServiceType.WARLIKE);

        Operation opHighroad = new StoredOperation("Highroad",
                actOdtOf(2015, 1, 1), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opOkraII = new StoredOperation("Okra",
                actOdtOf(2015, 9, 9), Optional.empty(),
                ServiceType.WARLIKE);


        Assert.assertTrue(results.contains(opEnduringFreedom));
        Assert.assertTrue(results.contains(opSlipper));
        Assert.assertTrue(results.contains(opAriki));
        Assert.assertTrue(results.contains(opPalate));
        Assert.assertTrue(results.contains(opCatalyst));
        Assert.assertTrue(results.contains(opAthena));
        Assert.assertTrue(results.contains(opISAF));
        Assert.assertTrue(results.contains(opHerrick));
        Assert.assertTrue(results.contains(opPalateII));
        Assert.assertTrue(results.contains(opPaladin));
        Assert.assertTrue(results.contains(opRiverbank));
        Assert.assertTrue(results.contains(opKruger));
        Assert.assertTrue(results.contains(opSlipperII));
        Assert.assertTrue(results.contains(opNNMEOAL));
        Assert.assertTrue(results.contains(opSlipperIII));
        Assert.assertTrue(results.contains(opSlipperIV));
        Assert.assertTrue(results.contains(opOkra));
        Assert.assertTrue(results.contains(opHighroad));
        Assert.assertTrue(results.contains(opOkraII));
    }

    @Test
    public void getNonWarlikeOps() throws IOException {
        URL inputDocx = Resources.getResource("F2016L00995.docx");
        byte[] pdfBytes = Resources.toByteArray(inputDocx);
        ImmutableList<Operation> results = ServiceDeterminations.extractOperations(pdfBytes);
        Assert.assertTrue(results.size() == 26);
    }

    @Test
    public void checkAllNonWarlikeOperationsExist() throws IOException {
        URL inputDocx = Resources.getResource("F2016L00995.docx");
        byte[] docXBytes = Resources.toByteArray(inputDocx);
        ImmutableList<Operation> results = ServiceDeterminations.extractOperations(docXBytes);

        Operation opMazurka = new StoredOperation("Mazurka",
                actOdtOf(1993, 1, 28), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opOsier = new StoredOperation("Osier",
                actOdtOf(1997, 1, 24), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opJointGuardian = new StoredOperation("Joint Guardian",
                actOdtOf(1999, 6, 11), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opPomelo = new StoredOperation("Pomelo",
                actOdtOf(2001, 1, 15), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opPaladin = new StoredOperation("Paladin",
                actOdtOf(2003, 4, 21), Optional.of(actOdtOf(2006, 7, 11)),
                ServiceType.NON_WARLIKE);

        Operation opPaladinII = new StoredOperation("Paladin",
                actOdtOf(2006, 7, 12), Optional.of(actOdtOf(2006, 8, 14)),
                ServiceType.NON_WARLIKE);

        Operation opPaladinIII = new StoredOperation("Paladin",
                actOdtOf(2006, 8, 15), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opAnode = new StoredOperation("Anode",
                actOdtOf(2003, 7, 24), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opCitadel = new StoredOperation("Citadel",
                actOdtOf(2003, 8, 18), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opSpire = new StoredOperation("Spire",
                actOdtOf(2004, 5, 20), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opAzure = new StoredOperation("Azure",
                actOdtOf(2005, 4, 10), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opAstute = new StoredOperation("Astute",
                actOdtOf(2006, 5, 12), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opRamp = new StoredOperation("Ramp",
                actOdtOf(2006, 7, 20), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opQuickstep = new StoredOperation("Quickstep",
                actOdtOf(2006, 10, 31), Optional.of(actOdtOf(2006, 12, 22)),
                ServiceType.NON_WARLIKE);

        Operation opQuickstepTonga = new StoredOperation("Quickstep Tonga",
                actOdtOf(2006, 11, 18), Optional.of(actOdtOf(2006, 11, 30)),
                ServiceType.NON_WARLIKE);

        Operation opHedgerow = new StoredOperation("Hedgerow",
                actOdtOf(2008, 7, 28), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opNNMEOAL = new StoredOperation("NNMEOAL",
                actOdtOf(2011, 3, 31), Optional.of(actOdtOf(2011, 10, 31)),
                ServiceType.NON_WARLIKE);

        Operation opAslan = new StoredOperation("Aslan",
                actOdtOf(2011, 9, 23), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opAccordion = new StoredOperation("Accordion",
                actOdtOf(2014, 7, 1), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opManitou = new StoredOperation("Manitou",
                actOdtOf(2014, 7, 1), Optional.of(actOdtOf(2015, 5, 13)),
                ServiceType.NON_WARLIKE);

        Operation opManitouII = new StoredOperation("Manitou",
                actOdtOf(2015, 5, 14), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opOkra = new StoredOperation("Okra",
                actOdtOf(2014, 7, 1), Optional.of(actOdtOf(2014, 8, 8)),
                ServiceType.NON_WARLIKE);

        Operation opOkraII = new StoredOperation("Okra",
                actOdtOf(2014, 8, 9), Optional.of(actOdtOf(2015, 9, 8)),
                ServiceType.NON_WARLIKE);

        Operation opOkraIII = new StoredOperation("Okra",
                actOdtOf(2015, 9, 9), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opAugury = new StoredOperation("Augury",
                actOdtOf(2014, 7, 4), Optional.empty(),
                ServiceType.NON_WARLIKE);

        Operation opHawick = new StoredOperation("Hawick",
                actOdtOf(2014, 7, 21), Optional.empty(),
                ServiceType.NON_WARLIKE);
        
        Assert.assertTrue(results.contains(opMazurka));
        Assert.assertTrue(results.contains(opOsier));
        Assert.assertTrue(results.contains(opJointGuardian));
        Assert.assertTrue(results.contains(opPomelo));
        Assert.assertTrue(results.contains(opPaladin));
        Assert.assertTrue(results.contains(opPaladinII));
        Assert.assertTrue(results.contains(opPaladinIII));
        Assert.assertTrue(results.contains(opAnode));
        Assert.assertTrue(results.contains(opCitadel));
        Assert.assertTrue(results.contains(opSpire));
        Assert.assertTrue(results.contains(opAzure));
        Assert.assertTrue(results.contains(opAstute));
        Assert.assertTrue(results.contains(opRamp));
        Assert.assertTrue(results.contains(opQuickstep));
        Assert.assertTrue(results.contains(opQuickstepTonga));
        Assert.assertTrue(results.contains(opHedgerow));
        Assert.assertTrue(results.contains(opNNMEOAL));
        Assert.assertTrue(results.contains(opAslan));
        Assert.assertTrue(results.contains(opAccordion));
        Assert.assertTrue(results.contains(opManitou));
        Assert.assertTrue(results.contains(opManitouII));
        Assert.assertTrue(results.contains(opOkra));
        Assert.assertTrue(results.contains(opOkraII));
        Assert.assertTrue(results.contains(opOkraIII));
        Assert.assertTrue(results.contains(opAugury));
        Assert.assertTrue(results.contains(opHawick));
    }


    @Test
    public void extractCommencementDate() throws IOException {

        URL inputDocx = Resources.getResource("F2016L00994.docx");
        byte[] docXBytes = Resources.toByteArray(inputDocx);
        OffsetDateTime commencementDate = ServiceDeterminations.extractCommencementDateFromDocx(docXBytes).get();
        Assert.assertTrue(commencementDate.isEqual(DateTimeUtils.localDateToLastMidnightCanberraTime(LocalDate.of(2016,6,7))));
    }

}
