package au.gov.dva.sopapi.sopref.data;

import au.gov.dva.sopapi.sopref.data.servicedeterminations.StoredOperation;
import au.gov.dva.sopapi.exceptions.ServiceDeterminationParserError;
import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.interfaces.model.ServiceType;
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
import java.util.stream.Collectors;

public class ServiceDeterminations {

    public static Optional<LocalDate> extractCommencementDateFromDocx(byte[] determinationDocx) {

        InputStream determinationStream = new ByteArrayInputStream(determinationDocx);
        try (XWPFDocument doc = new XWPFDocument(determinationStream)) {

            final String commencementTableHeading = "Commencement information";

            List<XWPFTable> tables = getTablesWithHeading(commencementTableHeading,doc);
            if (tables.isEmpty())
            {
                throw new ServiceDeterminationParserError("Could not identify Commencement table with heading: 'Commencement'.");
            }
            else {
                XWPFTable commencementTable = tables.get(0);

                final int row = 3;
                final int col = 2;

                String commencementText = commencementTable.getRow(row) != null ?
                        commencementTable.getRow(row).getCell(col) != null ?
                                commencementTable.getRow(row).getCell(col).getText() != null ?
                                    commencementTable.getRow(row).getCell(col).getText() : null : null : null;

                if (commencementText == null || commencementText.isEmpty()) {
                    return Optional.empty();
                }
                else {
                    return Optional.of(LocalDate.parse(commencementText.trim(),DateTimeFormatter.ofPattern("d MMMM yyyy")));
                }

            }

        } catch (IOException e) {
            throw new ServiceDeterminationParserError(e);
        }




    }


    private static  List<XWPFTable> getTablesWithHeading(String heading, XWPFDocument xwpfDocument)
    {
        return xwpfDocument.getTables().stream().filter(
                table ->  table.getRow(0) != null &&
                        table.getRow(0).getCell(0) != null   &&
                        table.getRow(0).getCell(0).getText().contentEquals(heading))
                .collect(Collectors.toList());
    }


    public static ImmutableList<Operation> extractOperations(byte[] determinationDocx) {
        final String nonWarlike = "nonwarlike service";
        final String warlike = "warlike service";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

        List<Operation> operations = new ArrayList<>();

        InputStream determinationStream = new ByteArrayInputStream(determinationDocx);
        try (XWPFDocument determinationDoc = new XWPFDocument(determinationStream)) {

            Pattern periodPattern = Pattern.compile("\\d{1,3}\\h[a-zA-Z]+\\h\\d{4}");
            Pattern itemPattern = Pattern.compile("\\d+.*");
            List<LocalDate> datesFound = new ArrayList<>();
            for (XWPFTable table : determinationDoc.getTables()) {

                // First cell in the first row indicates if it's the right table
                XWPFTableCell firstCell = table.getRow(0).getCell(0);
                String firstCellText = firstCell.getText().toLowerCase(Locale.ENGLISH);

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
                        // Reset dates found
                        datesFound.clear();

                        // Only interested in rows where the first cell is a number
                        Matcher rowMatcher = itemPattern.matcher(row.getCell(0).getText());

                        if (rowMatcher.matches()) {
                            String opName = row.getCell(1).getText();

                            String natureOfOperation = row.getCell(2).getText();
                            // Handle case where operation name isn't present
                            if (opName.isEmpty())
                            {
                                if (isNNMEOL(natureOfOperation)) {
                                    opName = "NNMEOAL";
                                }
                                else {
                                    throw new ServiceDeterminationParserError(String.format("Empty operation name for: %s", natureOfOperation));
                                }
                            }

                            // Replace horizontal white space with space to make parsing dates easier
                            String operationPeriod = row.getCell(4).getText().replaceAll("\\h", " ");
                            Matcher periodMatcher = periodPattern.matcher(operationPeriod);

                            while (periodMatcher.find()) {
                                LocalDate date = LocalDate.parse(periodMatcher.group(), formatter);
                                datesFound.add(date);
                            }


                            if (datesFound.isEmpty()) {
                                throw new ServiceDeterminationParserError("Cannot determine operation start date from: " + operationPeriod);
                            }
                            else {
                                LocalDate opStartDate = datesFound.get(0);
                                Optional<LocalDate> opEndDate = datesFound.size() > 1 ? Optional.of(datesFound.get(1)) : Optional.empty();
                                Operation operation = new StoredOperation(opName, opStartDate, opEndDate, opServiceType);
                                operations.add(operation);
                            }

                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ServiceDeterminationParserError(e);
        }

        return ImmutableList.copyOf(operations);
    }

    private static boolean isNNMEOL(String description)
    {
        return description.contentEquals("ADF contribution to the NATO nofly zone and maritime enforcement operation against Libya");
    }

}
