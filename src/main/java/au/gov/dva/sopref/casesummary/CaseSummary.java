package au.gov.dva.sopref.casesummary;

import au.gov.dva.sopref.interfaces.model.casesummary.CaseSummaryModel;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class CaseSummary {
   public static CompletableFuture<byte[]> createCaseSummary(CaseSummaryModel caseSummaryModel) {

       XWPFDocument document = new XWPFDocument();

       try {
           FileOutputStream outputStream = new FileOutputStream(new File("Case Summary.docx"));
           document.write(outputStream);
           outputStream.close();
       } catch (FileNotFoundException exception) {
           exception.printStackTrace();
       } catch (IOException exception) {
           exception.printStackTrace();
       }

       return null;
   }
}
