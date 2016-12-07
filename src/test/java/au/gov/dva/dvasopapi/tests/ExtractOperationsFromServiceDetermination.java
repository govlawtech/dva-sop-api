package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopref.data.ServiceDeterminations;
import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.ServiceDetermination;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class ExtractOperationsFromServiceDetermination {

    @Test
    public void getWarlikeOps() throws IOException {
        URL inputDocx = Resources.getResource("F2016L00994.docx");
        byte[] pdfBytes = Resources.toByteArray(inputDocx);
        ImmutableList<Operation> results = ServiceDeterminations.extractOperations(pdfBytes);
        Assert.assertTrue(results.size() == 19);
    }

    @Test
    public void getNonWarlikeOps() throws IOException {
        URL inputDocx = Resources.getResource("F2016L00995.docx");
        byte[] pdfBytes = Resources.toByteArray(inputDocx);
        ImmutableList<Operation> results = ServiceDeterminations.extractOperations(pdfBytes);
        Assert.assertTrue(results.size() == 26);
    }
}
