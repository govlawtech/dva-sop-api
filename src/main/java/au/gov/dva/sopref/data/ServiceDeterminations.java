package au.gov.dva.sopref.data;

import au.gov.dva.sopref.data.servicedeterminations.StoredOperation;
import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.ServiceType;
import com.google.common.collect.ImmutableList;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceDeterminations {

    public static ImmutableList<Operation> extractOperations(byte[] determinationDocx) {

        final String nonWarlike = "nonwarlike service";
        final String warlike = "warlike service";
        List<Operation> operations = new ArrayList<>();

        InputStream determinationStream = new ByteArrayInputStream(determinationDocx);
        try (XWPFDocument determinationDoc = new XWPFDocument(determinationStream);) {

            for (XWPFTable table : determinationDoc.getTables()) {

                // First cell in the first row indicates if it's the right table
                XWPFTableCell firstCell = table.getRow(0).getCell(0);
                String firstCellText = firstCell.getText().toLowerCase();

                if ((firstCellText.equals(nonWarlike) || (firstCellText.equals(warlike)))) {

                    ServiceType opServiceType;

                    switch (firstCellText) {
                        case nonWarlike:
                            opServiceType = ServiceType.NON_WARLIKE;
                            break;
                        case warlike:
                            opServiceType = ServiceType.WARLIKE;
                            break;
                        default:
                            opServiceType = ServiceType.NON_WARLIKE;
                    }

                    for (XWPFTableRow row : table.getRows()) {
                        // Only interested in rows where the first cell is a number
                        Pattern itemPattern = Pattern.compile("\\d+");
                        Matcher matcher = itemPattern.matcher(row.getCell(0).getText());

                        if (matcher.matches()) {
                            String opName = row.getCell(1).getText();

                            // Handle case where operation name isn't present
                            if (opName.isEmpty()) {
                                opName = "NNMEOAL";
                            }

                            // Replace horizontal white space with space to make parsing dates easier
                            String operationPeriod = row.getCell(4).getText().replaceAll("\\h", " ");
                            Pattern periodPattern = Pattern.compile("\\d{1,3}\\h[a-zA-Z]+\\h\\d{4}");
                            matcher = periodPattern.matcher(operationPeriod);

                            List<LocalDate> datesFound = new ArrayList<>();

                            while (matcher.find()) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
                                LocalDate date = LocalDate.parse(matcher.group(), formatter);
                                datesFound.add(date);
                            }

                            LocalDate opStartDate = LocalDate.MIN;
                            Optional<LocalDate> opEndDate = Optional.empty();

                            if (!datesFound.isEmpty()) {
                                opStartDate = datesFound.get(0);

                                if (datesFound.size() > 1) {
                                    opEndDate = Optional.of(datesFound.get(1));
                                }
                            }

                            Operation operation = new StoredOperation(opName, opStartDate, opEndDate, opServiceType);
                            operations.add(operation);
                        }
                    }
                }
            }

        } catch (IOException e) {
            // TODO: wrap exception
            System.out.println(e);
        }

        return ImmutableList.copyOf(operations);
    }

}
