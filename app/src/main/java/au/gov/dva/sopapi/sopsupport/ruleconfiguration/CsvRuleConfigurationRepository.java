package au.gov.dva.sopapi.sopsupport.ruleconfiguration;

import au.gov.dva.sopapi.ConfigurationRuntimeException;
import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import scala.util.Properties;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvRuleConfigurationRepository implements RuleConfigurationRepository {

    public static final String regexForFactorRef = "[0-9a-z]+\\([0-9a-z]+\\)";

    private final byte[] _rhCsv;
    private final byte[] _boPCsv;
    private final ImmutableSet<BoPRuleConfigurationItem> bopRuleConfigurationItems;
    private final ImmutableSet<RHRuleConfigurationItem> rhRuleConfigurationItems;

    private static class ColumnIndices {

        public final static int CONDITION = 0;
        public final static int INSTRUMENT_ID = 1;
        public final static int FACTOR_REFS = 2;
        public final static int SERVICE_BRANCH = 3;
        public final static int RANK = 4;
        public final static int CFTS_DAYS = 5;
        public final static int ACCUMULATION_RATE_PW = 6;
        public final static int ACCUMULATION_UNIT = 7;
        public final static int HARD_WINDOW_FROM_DATE_OF_ONSET = 8;

        public static class RH {
            public final static int REQUIRED_OPERATIONAL_SERVICE_DAYS = 9;
            public final static int OPERATIONAL_SERVICE_TEST_YEARS = 10;
        }
    }


    public CsvRuleConfigurationRepository(byte[] rhCsv, byte[] boPCsv) {
        this._rhCsv = rhCsv.clone();
        this._boPCsv = boPCsv.clone();

        try {
            Optional<List<String>> validationErrors = getValidationErrors(rhCsv,boPCsv);
            if (validationErrors.isPresent())
            {
                throw new ConfigurationRuntimeException(String.join(Properties.lineSeparator(),validationErrors.get()));
            }

            this.rhRuleConfigurationItems = buildRhItems();
            this.bopRuleConfigurationItems = buildBopItems();

        } catch (IOException e) {
            throw new ConfigurationRuntimeException(e);
        }

    }

    private Optional<List<String>> getValidationErrors(byte[] rhCsv, byte[] bopCsv) throws IOException {
        List<String> rhErrors = getErrorsForSheet(rhCsv, "RH");
        List<String> bopErrors = getErrorsForSheet(bopCsv, "BoP");
        List<String> combined = Stream.concat(rhErrors.stream(),bopErrors.stream()).collect(Collectors.toList());
        if (!combined.isEmpty())
        {
            return Optional.of(combined);
        }
        return Optional.empty();
    }

    private List<String> getErrorsForSheet(byte[] csv, String sheetName) throws IOException {
        Reader reader = createCsvReader(csv);


        CSVParser parser = createParser(reader);
        List<CSVRecord> recordList = parser.getRecords();
        List<String> errors = new ArrayList<>();
        for (CSVRecord csvRecord : recordList)
        {
            int rowNum = recordList.indexOf(csvRecord) + 1;
            List<Integer> emptyIndexesThatShouldNotBeEmpty =   ImmutableList
                    .of(ColumnIndices.CONDITION,
                            ColumnIndices.INSTRUMENT_ID,
                            ColumnIndices.FACTOR_REFS,
                            ColumnIndices.SERVICE_BRANCH,
                            ColumnIndices.RANK,
                            ColumnIndices.CFTS_DAYS)
                    .stream()
                    .filter(i -> csvRecord.get(i).trim().isEmpty())
                    .collect(Collectors.toList());

            if (!emptyIndexesThatShouldNotBeEmpty.isEmpty() && emptyIndexesThatShouldNotBeEmpty.size() < 6)
            {
                errors.add(String.format("Sheet %s, row %s has empty required cells: row(s) %s", sheetName, rowNum, String.join(",",emptyIndexesThatShouldNotBeEmpty.stream().map(Object::toString).collect(Collectors.toList()))));
            }

            Stream<String> failingFactorRefs = Arrays.stream(csvRecord.get(ColumnIndices.FACTOR_REFS).split("[,;]"))
                    .map(i -> i.trim())
                    .filter(i -> !Pattern.matches(this.regexForFactorRef,i));

            if (failingFactorRefs.findAny().isPresent())
            {
                errors.add(String.format("Sheet %s, row %s: factor references must match regex %s", sheetName, rowNum,this.regexForFactorRef));
            }
        }

        try {
            reader.close();
        } catch (IOException e) {
            throw new ConfigurationRuntimeException(e);
        }

        return errors;
    }

    private ImmutableSet<RHRuleConfigurationItem> buildRhItems() {

        Reader rhReader = createCsvReader(_rhCsv);

        CSVParser parser = createParser(rhReader);
        List<RHRuleConfigurationItem> acc = new ArrayList<>();
        try {
            List<CSVRecord> recordList = parser.getRecords();
            for (CSVRecord csvRecord : recordList) {
                if (!csvRecord.get(ColumnIndices.RH.REQUIRED_OPERATIONAL_SERVICE_DAYS).isEmpty()) {

                    acc.add(new ParsedRhRuleConfigurationItem(
                            csvRecord.get(ColumnIndices.CONDITION),
                            csvRecord.get(ColumnIndices.INSTRUMENT_ID),
                            csvRecord.get(ColumnIndices.FACTOR_REFS),
                            csvRecord.get(ColumnIndices.SERVICE_BRANCH),
                            csvRecord.get(ColumnIndices.RANK),
                            csvRecord.get(ColumnIndices.CFTS_DAYS),
                            !csvRecord.get(ColumnIndices.ACCUMULATION_RATE_PW).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.ACCUMULATION_RATE_PW)) : Optional.empty(),
                            !csvRecord.get(ColumnIndices.ACCUMULATION_UNIT).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.ACCUMULATION_UNIT)) : Optional.empty(),
                            !csvRecord.get(ColumnIndices.HARD_WINDOW_FROM_DATE_OF_ONSET).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.HARD_WINDOW_FROM_DATE_OF_ONSET)) : Optional.empty(),
                            csvRecord.get(ColumnIndices.RH.REQUIRED_OPERATIONAL_SERVICE_DAYS),
                            !csvRecord.get(ColumnIndices.RH.OPERATIONAL_SERVICE_TEST_YEARS).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.RH.OPERATIONAL_SERVICE_TEST_YEARS)) : Optional.empty()
                    ));
                }
            }


        } catch (IOException e) {
            throw new ConfigurationRuntimeException("Error parsing RH CSV binary.", e);
        }
        try {
            parser.close();
        } catch (IOException e) {
            throw new ConfigurationRuntimeException(e);
        }
        return ImmutableSet.copyOf(acc);
    }

    private static Reader createCsvReader(byte[] csv) {
        try {
            return new InputStreamReader(new BOMInputStream(new ByteArrayInputStream(csv)), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ConfigurationRuntimeException(e);
        }
    }

    private ImmutableSet<BoPRuleConfigurationItem> buildBopItems() {

        Reader bopReader = createCsvReader(_boPCsv);

        CSVParser parser = createParser(bopReader);
        List<BoPRuleConfigurationItem> acc = new ArrayList<>();
        try {
            List<CSVRecord> recordList = parser.getRecords();
            for (CSVRecord csvRecord : recordList) {
                acc.add(new ParseBoPRuleConfigurationItem(
                        csvRecord.get(ColumnIndices.CONDITION),
                        csvRecord.get(ColumnIndices.INSTRUMENT_ID),
                        csvRecord.get(ColumnIndices.FACTOR_REFS),
                        csvRecord.get(ColumnIndices.SERVICE_BRANCH),
                        csvRecord.get(ColumnIndices.RANK),
                        csvRecord.get(ColumnIndices.CFTS_DAYS),
                        !csvRecord.get(ColumnIndices.ACCUMULATION_RATE_PW).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.ACCUMULATION_RATE_PW)) : Optional.empty(),
                        !csvRecord.get(ColumnIndices.ACCUMULATION_UNIT).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.ACCUMULATION_UNIT)) : Optional.empty(),
                        !csvRecord.get(ColumnIndices.HARD_WINDOW_FROM_DATE_OF_ONSET).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.HARD_WINDOW_FROM_DATE_OF_ONSET)) : Optional.empty()
                ));
            }
        } catch (IOException e) {
            throw new ConfigurationRuntimeException("Error parsing BoP CSV binary.", e);
        }
        try {
            parser.close();
        } catch (IOException e) {
            throw new ConfigurationRuntimeException(e);
        }
        return ImmutableSet.copyOf(acc);
    }


    private static CSVParser createParser(Reader reader) {
        final CSVParser parser;
        try {
            parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
        } catch (IOException e) {
            throw new ConfigurationRuntimeException(e);
        }
        return parser;

    }


    @Override
    public ImmutableSet<RHRuleConfigurationItem> getRHItems() {
        return rhRuleConfigurationItems;
    }

    @Override
    public ImmutableSet<BoPRuleConfigurationItem> getBoPItems() {
        return bopRuleConfigurationItems;
    }
}
