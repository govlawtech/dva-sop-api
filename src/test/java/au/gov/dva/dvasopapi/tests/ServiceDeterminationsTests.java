package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopref.data.Conversions;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class ServiceDeterminationsTests {

    @Test
    public void convertWarlikeDeterminationToText() throws IOException {
        URL inputPdf = Resources.getResource("F2016L00994.pdf");
        byte[] pdfBytes = Resources.toByteArray(inputPdf);
        String result = Conversions.pdfToPlainText(pdfBytes);
        System.out.print(result);
    }
}
