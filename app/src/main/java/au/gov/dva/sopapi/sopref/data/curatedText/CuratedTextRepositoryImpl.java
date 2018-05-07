package au.gov.dva.sopapi.sopref.data.curatedText;

import au.gov.dva.sopapi.ConfigurationRuntimeException;
import au.gov.dva.sopapi.interfaces.CuratedTextRepository;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.uwyn.jhighlight.fastutil.Hash;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.input.BOMInputStream;
import org.xlsx4j.sml.Col;
import scala.util.Properties;
import scala.util.matching.Regex;

import javax.crypto.spec.OAEPParameterSpec;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CuratedTextRepositoryImpl implements CuratedTextRepository {


    private final byte[] factorCsv;
    private final byte[] definitionCsv;

    private static class ColumnIndices {
        public static class FactorText {
            public final static int RegisterId = 0;
            public final static int LegalReference = 1;
            public final static int Text = 2;
        }

        public static class DefinitionText {
            public final static int DefinedTerm = 0;
            public final static int Definition = 1;
        }
    }

    private ImmutableMap<String, ImmutableMap<String, String>> _factorTextMap;
    private ImmutableMap<String, String> _definitionMap;

    public CuratedTextRepositoryImpl(byte[] factorCsv, byte[] definitionCsv) {
        this.factorCsv = factorCsv.clone();
        this.definitionCsv = definitionCsv.clone();
        _factorTextMap = buildFactorTextMap(factorCsv);
        _definitionMap = buildDefinitionMap(definitionCsv);

    }

    private static Reader createCsvReader(byte[] csv) {
        try {
            return new InputStreamReader(new BOMInputStream(new ByteArrayInputStream(csv)), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ConfigurationRuntimeException(e);
        }
    }


    private static CSVParser createParser(Reader reader) {
        final CSVParser parser;
        try {
            parser = new CSVParser(reader, CSVFormat.EXCEL
                    .withHeader()
                    .withTrim(true)
                    .withIgnoreEmptyLines(true)
                    .withIgnoreSurroundingSpaces(true)
            );
        } catch (IOException e) {
            throw new ConfigurationRuntimeException(e);
        }
        return parser;

    }


    private static Pattern registerIdPattern = Pattern.compile("[A-Z][0-9]{4}[A-Z][0-9]{5}");

    private static ImmutableMap<String, ImmutableMap<String, String>> buildFactorTextMap(byte[] factorCsv) {
        HashMap<String, HashMap<String, String>> map = new HashMap<>();
        Reader rhReader = createCsvReader(factorCsv);
        CSVParser parser = createParser(rhReader);
        try {
            List<CSVRecord> recordList = parser.getRecords();
            for (CSVRecord csvRecord : recordList) {
                if (csvRecord.size() != 3) {
                    throw new ConfigurationRuntimeException("Error in curated factor text configuration: row must have three columns: " + csvRecord.toString());
                }

                String registerId = stripQuotes(csvRecord.get(ColumnIndices.FactorText.RegisterId).trim());
                Matcher m = registerIdPattern.matcher(registerId);
                if (!m.matches()) {
                    throw new ConfigurationRuntimeException("Register ID in curated factor text configuration looks wrong: " + registerId);
                }

                String legalReference = stripQuotes(csvRecord.get(ColumnIndices.FactorText.LegalReference).trim());
                String factorText = stripQuotes(csvRecord.get(ColumnIndices.FactorText.Text).trim());

                if (map.get(registerId) == null) {
                    map.put(registerId, new HashMap<>());
                }

                if (map.get(registerId).containsKey(legalReference)) {
                    throw new ConfigurationRuntimeException(String.format("Duplicate factor text configuration for Register ID %s and legal reference %s.", registerId, legalReference));
                } else {

                    map.get(registerId).put(legalReference, factorText);
                }
            }

            parser.close();

            ImmutableMap.Builder<String, ImmutableMap<String, String>> builder = new ImmutableMap.Builder<>();
            map.forEach((k, v) -> builder.put(k, ImmutableMap.copyOf(v)));
            return builder.build();

        } catch (IOException e) {
            throw new ConfigurationRuntimeException("Error parsing Factor text CSV binary.", e);
        }
    }

    private static String stripQuotes(String s)
    {
       return s.replaceAll("^\"","").replaceAll("\"$","");
    }

    private static ImmutableMap<String, String> buildDefinitionMap(byte[] definitionCsv) {

        HashMap<String, String> map = new HashMap<>();
        Reader rhReader = createCsvReader(definitionCsv);
        CSVParser parser = createParser(rhReader);


        try {
            List<CSVRecord> recordList = parser.getRecords().stream().filter(r -> r.size() == 2).collect(Collectors.toList());
            for (CSVRecord csvRecord : recordList) {

                String definedTerm = stripQuotes(csvRecord.get(ColumnIndices.DefinitionText.DefinedTerm).trim());
                String definition = stripQuotes(csvRecord.get(ColumnIndices.DefinitionText.Definition).trim());

                if (definedTerm != null) {
                    if (map.containsKey(definedTerm)) {
                        throw new ConfigurationRuntimeException(String.format("Error in curated definition text config: duplicate definition for %s.", definedTerm));
                    }
                }

                map.put(definedTerm, definition);
            }

            parser.close();

        } catch (IOException e) {
            throw new ConfigurationRuntimeException("Error parsing Factor text CSV binary.", e);

        }
        return ImmutableMap.copyOf(map);
    }


    @Override
    public Optional<String> getDefinitionFor(String definedTerm) {
        if (_definitionMap.containsKey(definedTerm)) return Optional.of( _definitionMap.get(definedTerm));
        else return Optional.empty();
    }


    @Override
    public Optional<String> getFactorTextFor(String registerId, String legalReference) {

        if (!_factorTextMap.containsKey(registerId))
        {
            return Optional.empty();
        }
        else {
            if (!_factorTextMap.get(registerId).containsKey(legalReference))
            {
                return Optional.empty();
            }
            else {
                return Optional.of(_factorTextMap.get(registerId).get(legalReference));
            }
        }
    }
}
