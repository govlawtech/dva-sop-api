package au.gov.dva.dvasopapi.tests;


import au.gov.dva.sopref.data.Conversions;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class ConversionTests {

    @Test
    public void pdfToText() throws IOException {
        URL inputPdf = Resources.getResource("F2014L00933.pdf");
        byte[] pdfBytes = Resources.toByteArray(inputPdf);
        String result = Conversions.pdfToPlainText(pdfBytes);
        int lineCount = result.split("[\r\n]").length;
        System.out.print(result);
        Assert.assertTrue(lineCount > 250);
    }
}
