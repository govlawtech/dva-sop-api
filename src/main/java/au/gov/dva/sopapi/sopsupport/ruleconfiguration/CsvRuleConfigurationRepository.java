package au.gov.dva.sopapi.sopsupport.ruleconfiguration;

import au.gov.dva.sopapi.ConfigurationError;
import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CsvRuleConfigurationRepository implements RuleConfigurationRepository {

    private static class ColumnIndices {

        public final static int CONDITION = 0;
        public final static int INSTRUMENT_ID = 1;
        public final static int FACTOR_REFS = 2;
        public final static int SERVICE_BRANCH = 3;
        public final static int RANK = 4;
        public final static int CFTS_WEESK = 5;
        public final static int ACCUMULATION_RATE_PW = 6;
        public final static int ACCUMULATION_UNIT = 7;

        public static class RH {
            public final static int REQUIRED_OPERATIONAL_SERVICE_DAYS = 8;
            public final static int OPERATIONAL_SERVICE_TEST_YEARS = 9;
        }
    }

    private final ImmutableSet<RHRuleConfigurationItem> rhRuleConfigurationItems;
    private final ImmutableSet<BoPRuleConfigurationItem> boPRuleConfigurationItems;


    public CsvRuleConfigurationRepository(byte[] rhCsv, byte[] boPCsv)
    {
        Reader rhReader = createCsvReader(rhCsv);
        rhRuleConfigurationItems = buildRhItems(rhReader);
        try {
            rhReader.close();
        } catch (IOException e) {
            throw new ConfigurationError(e);
        }

        Reader bopReader = createCsvReader(boPCsv);
        boPRuleConfigurationItems = buildBopItems(bopReader);
        try {
            bopReader.close();
        } catch (IOException e) {
            throw new ConfigurationError(e);
        }
    }

    private ImmutableSet<RHRuleConfigurationItem> buildRhItems(Reader rhItems) {
        CSVParser parser = createParser(rhItems);
        List<RHRuleConfigurationItem> acc = new ArrayList<>();
        try {
            List<CSVRecord> recordList =  parser.getRecords();
            for (CSVRecord csvRecord : recordList)
            {
                acc.add(new ParsedRhRuleConfigurationItem(
                        csvRecord.get(ColumnIndices.CONDITION),
                        csvRecord.get(ColumnIndices.INSTRUMENT_ID),
                        csvRecord.get(ColumnIndices.FACTOR_REFS),
                        csvRecord.get(ColumnIndices.SERVICE_BRANCH),
                        csvRecord.get(ColumnIndices.RANK),
                        csvRecord.get(ColumnIndices.CFTS_WEESK),
                        !csvRecord.get(ColumnIndices.ACCUMULATION_RATE_PW).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.ACCUMULATION_RATE_PW)) : Optional.empty(),
                        !csvRecord.get(ColumnIndices.ACCUMULATION_UNIT).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.ACCUMULATION_UNIT)) : Optional.empty(),
                        csvRecord.get(ColumnIndices.RH.REQUIRED_OPERATIONAL_SERVICE_DAYS),
                        csvRecord.get(ColumnIndices.RH.OPERATIONAL_SERVICE_TEST_YEARS)
                ));
            }
        } catch (IOException e) {
            throw new ConfigurationError("Error parsing RH CSV binary.",e);
        }
        try {
            parser.close();
        } catch (IOException e) {
            throw new ConfigurationError(e);
        }
        return ImmutableSet.copyOf(acc);
    }

    private Reader createCsvReader(byte[] csv) {
        try {
            return new InputStreamReader(new BOMInputStream(new ByteArrayInputStream(csv)), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ConfigurationError(e);
        }
    }

    private ImmutableSet<BoPRuleConfigurationItem> buildBopItems(Reader bopCsv) {
        CSVParser parser = createParser(bopCsv);
        List<BoPRuleConfigurationItem> acc = new ArrayList<>();
        try {
            List<CSVRecord> recordList =  parser.getRecords();
            for (CSVRecord csvRecord : recordList)
            {
                acc.add(new ParseBoPRuleConfigurationItem(
                        csvRecord.get(ColumnIndices.CONDITION),
                        csvRecord.get(ColumnIndices.INSTRUMENT_ID),
                        csvRecord.get(ColumnIndices.FACTOR_REFS),
                        csvRecord.get(ColumnIndices.SERVICE_BRANCH),
                        csvRecord.get(ColumnIndices.RANK),
                        csvRecord.get(ColumnIndices.CFTS_WEESK),
                        !csvRecord.get(ColumnIndices.ACCUMULATION_RATE_PW).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.ACCUMULATION_RATE_PW)) : Optional.empty(),
                        !csvRecord.get(ColumnIndices.ACCUMULATION_UNIT).isEmpty() ? Optional.of(csvRecord.get(ColumnIndices.ACCUMULATION_UNIT)) : Optional.empty()
                ));
            }
        } catch (IOException e) {
            throw new ConfigurationError("Error parsing BoP CSV binary.",e);
        }
        try {
            parser.close();
        } catch (IOException e) {
            throw new ConfigurationError(e);
        }
        return ImmutableSet.copyOf(acc);
    }


    private static CSVParser createParser(Reader reader)
    {
        final CSVParser parser;
        try {
            parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
        } catch (IOException e) {
            throw new ConfigurationError(e);
        }
        return parser;

    }


    @Override
    public ImmutableSet<RHRuleConfigurationItem> getRHItems() {
        return rhRuleConfigurationItems;
    }

    @Override
    public ImmutableSet<BoPRuleConfigurationItem> getBoPItems() {
        return boPRuleConfigurationItems;
    }
}
