package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopref.data.ServiceDeterminations;
import au.gov.dva.sopref.data.servicedeterminations.StoredOperation;
import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.ServiceType;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;

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
        byte[] pdfBytes = Resources.toByteArray(inputDocx);
        ImmutableList<Operation> results = ServiceDeterminations.extractOperations(pdfBytes);

        Operation opEnduringFreedom = new StoredOperation("Enduring Freedom—Afghanistan",
                LocalDate.of(2001, 10, 7), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opSlipper = new StoredOperation("Slipper",
                LocalDate.of(2001, 10, 11), Optional.of(LocalDate.of(2009, 7, 30)),
                ServiceType.WARLIKE);

        Operation opAriki = new StoredOperation("Ariki",
                LocalDate.of(2001, 12, 1), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opPalate = new StoredOperation("Palate",
                LocalDate.of(2003, 4, 18), Optional.of(LocalDate.of(2004, 7, 5)),
                ServiceType.WARLIKE);

        Operation opCatalyst = new StoredOperation("Catalyst",
                LocalDate.of(2003, 7, 16), Optional.of(LocalDate.of(2009, 7, 31)),
                ServiceType.WARLIKE);

        Operation opAthena = new StoredOperation("Athena",
                LocalDate.of(2003, 7, 17), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opISAF = new StoredOperation("International Security Assistance Force",
                LocalDate.of(2003, 8, 11), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opHerrick = new StoredOperation("Herrick",
                LocalDate.of(2004, 9, 1), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opPalateII = new StoredOperation("Palate II",
                LocalDate.of(2005, 6, 27), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opPaladin = new StoredOperation("Paladin",
                LocalDate.of(2006, 7, 12), Optional.of(LocalDate.of(2006, 8, 14)),
                ServiceType.WARLIKE);

        Operation opRiverbank = new StoredOperation("Riverbank",
                LocalDate.of(2008, 7, 21), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opKruger = new StoredOperation("Kruger",
                LocalDate.of(2009, 1, 1), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opSlipperII = new StoredOperation("Slipper",
                LocalDate.of(2009, 7, 31), Optional.of(LocalDate.of(2012, 2, 19)),
                ServiceType.WARLIKE);

        Operation opNNMEOAL = new StoredOperation("NNMEOAL",
                LocalDate.of(2011, 3, 31), Optional.of(LocalDate.of(2011, 10, 31)),
                ServiceType.WARLIKE);

        Operation opSlipperIII = new StoredOperation("Slipper",
                LocalDate.of(2012, 2, 20), Optional.of(LocalDate.of(2014, 6, 30)),
                ServiceType.WARLIKE);

        Operation opSlipperIV = new StoredOperation("Slipper",
                LocalDate.of(2014, 7, 1), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opOkra = new StoredOperation("Okra",
                LocalDate.of(2014, 8, 9), Optional.of(LocalDate.of(2015, 9, 8)),
                ServiceType.WARLIKE);

        Operation opHighroad = new StoredOperation("Highroad",
                LocalDate.of(2015, 1, 1), Optional.empty(),
                ServiceType.WARLIKE);

        Operation opOkraII = new StoredOperation("Okra",
                LocalDate.of(2015, 9, 9), Optional.empty(),
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
}
