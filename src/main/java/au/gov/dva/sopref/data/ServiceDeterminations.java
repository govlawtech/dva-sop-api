package au.gov.dva.sopref.data;

import au.gov.dva.sopref.data.servicedeterminations.StoredOperation;
import au.gov.dva.sopref.exceptions.OperationParserError;
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
        String operationPeriod = "";

        Pattern itemPattern = Pattern.compile("\\d+.*");
        Pattern periodPattern = Pattern.compile("\\d{1,3}\\h[a-zA-Z]+\\h\\d{4}");
        Matcher rowMatcher;
        Matcher periodMatcher;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        LocalDate date;
        List<LocalDate> datesFound = new ArrayList<>();

        List<Operation> operations = new ArrayList<>();
        Operation operation;
        String opName;
        LocalDate opStartDate;
        Optional<LocalDate> opEndDate;
        ServiceType opServiceType;

        InputStream determinationStream = new ByteArrayInputStream(determinationDocx);
        try (XWPFDocument determinationDoc = new XWPFDocument(determinationStream);) {

            for (XWPFTable table : determinationDoc.getTables()) {

                // First cell in the first row indicates if it's the right table
                XWPFTableCell firstCell = table.getRow(0).getCell(0);
                String firstCellText = firstCell.getText().toLowerCase();

                if ((firstCellText.equals(nonWarlike) || (firstCellText.equals(warlike)))) {

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
                        // Reset dates found
                        datesFound.clear();

                        // Only interested in rows where the first cell is a number
                        rowMatcher = itemPattern.matcher(row.getCell(0).getText());

                        if (rowMatcher.matches()) {
                            opName = row.getCell(1).getText();

                            // Handle case where operation name isn't present
                            if (opName.isEmpty()) {
                                opName = "NNMEOAL";
                            }

                            // Replace horizontal white space with space to make parsing dates easier
                            operationPeriod = row.getCell(4).getText().replaceAll("\\h", " ");
                            periodMatcher = periodPattern.matcher(operationPeriod);

                            while (periodMatcher.find()) {
                                date = LocalDate.parse(periodMatcher.group(), formatter);
                                datesFound.add(date);
                            }

                            opStartDate = LocalDate.MIN;
                            opEndDate = Optional.empty();

                            if (!datesFound.isEmpty()) {
                                opStartDate = datesFound.get(0);

                                if (datesFound.size() > 1) {
                                    opEndDate = Optional.of(datesFound.get(1));
                                }
                            }

                            operation = new StoredOperation(opName, opStartDate, opEndDate, opServiceType);
                            operations.add(operation);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new OperationParserError(e);
        }

        return ImmutableList.copyOf(operations);
    }

}
