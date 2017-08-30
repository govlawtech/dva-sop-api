package au.gov.dva.sopapi;

import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.ICDCode;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopref.SoPs;
import com.google.common.collect.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class Status {

    public static byte[] createStatusCsv(Cache cache, URL blobsBaseUrl) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
        csvPrinter.printRecord(ImmutableList.of(
                "Condition Name",
                "Effective From",
                "Source FRL Link RH",
                "Source FRL Link BoP",
                "Parsed RH",
                "Parsed BoP",
                "ICD codes",
                "RH Superseded By",
                "BoP Superseded By"
        ));

        ImmutableSet<SoPPair> soPPairs = cache.get_allCurrentSopPairs();

        // condition name, icd codes, FRL RH link, FRL BoP link, Azure RH link, Azure BoP link, Updated by

        List<ImmutableList<String>> records = new ArrayList<>();

        for (SoPPair soPPair : soPPairs) {
            records.add(ImmutableList.of(
                    soPPair.getConditionName(),
                    soPPair.getEffectiveFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    buildFrlDetailsLink(soPPair.getRhSop().getRegisterId()),
                    buildFrlDetailsLink(soPPair.getBopSop().getRegisterId()),
                    buildAzureLink(soPPair.getRhSop().getRegisterId(), blobsBaseUrl),
                    buildAzureLink(soPPair.getBopSop().getRegisterId(), blobsBaseUrl),
                    buildIcdCodeCell(soPPair.getICDCodes()),
                    "",
                    ""
            ));
        }

        for (ImmutableList<String> strings : records) {
            csvPrinter.printRecord(strings);
        }

        return csvPrinter.getOut().toString().getBytes("UTF-8");
    }

    public static byte[] createSopStatsCsv(Cache cache) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
        csvPrinter.printRecord(ImmutableList.of(
                "Condition name",
                "Effective from date",
                "Number of factors onset (RH)",
                "Number of aggravation factors (RH)",
                "Total words of factor text (RH)",
                "Total number of definition references in factor text (RH)",
                "Total number of words in definitions (RH)"
        ));

        ImmutableSet<SoPPair> soPPairs = cache.get_allCurrentSopPairs();

        List<ImmutableList<String>> records = new ArrayList<>();

        for (SoPPair soPPair : soPPairs) {
            records.add(ImmutableList.of(
                    soPPair.getConditionName(),
                    soPPair.getEffectiveFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    Integer.toString(soPPair.getRhSop().getOnsetFactors().size()),
                    Integer.toString(soPPair.getRhSop().getAggravationFactors().size()),
                    Long.toString(countFactorTextWords(soPPair.getRhSop())),
                    Long.toString(countNumberOfEmbeddedDefinitions(soPPair.getRhSop())),
                    Long.toString(countDefinitionsWords(soPPair.getRhSop()))
            ));
        }

        for (ImmutableList<String> strings : records) {
            csvPrinter.printRecord(strings);
        }

        return csvPrinter.getOut().toString().getBytes("UTF-8");
    }

    public static byte[] createConditionAdjacencyList(Cache cache)
    {
        return null;
    }


    private static long countFactorTextWords(SoP sop)
    {
        long count = StreamSupport.stream(Iterables.concat(sop.getOnsetFactors(), sop.getAggravationFactors()).spliterator(), false)
                .map(factor -> factor.getText().split("\\s").length)
                .mapToLong(value -> value)
                .sum();
        return  count;
    }

    private static long countDefinitionsWords(SoP sop)
    {
        long count = StreamSupport.stream(Iterables.concat(sop.getOnsetFactors(), sop.getAggravationFactors()).spliterator(), false)
                .flatMap(factor -> factor.getDefinedTerms().stream())
                .map(definedTerm -> definedTerm.getDefinition())
                .distinct()
                .map(words -> words.split("\\s").length)
                .mapToLong(value -> value)
                .sum();
        return  count;
    }

    private static long countNumberOfEmbeddedDefinitions(SoP sop)
    {
        long count = StreamSupport.stream(Iterables.concat(sop.getOnsetFactors(), sop.getAggravationFactors()).spliterator(), false)
                .flatMap(factor -> factor.getDefinedTerms().stream())
                .map(definedTerm -> definedTerm.getTerm())
                .distinct()
                .count();
        return count;
    }

    public static String createStatusHtml(Cache cache, Repository repository, URL blobsBaseUrl, String version) {
        ImmutableSet<SoPPair> soPPairs = SoPs.groupSopsToPairs(cache.get_allSops(), OffsetDateTime.now());

        Optional<OffsetDateTime> lastUpdated = repository.getLastUpdated();
        String lastUpdateTime = lastUpdated.isPresent() ? lastUpdated.get().toString() : "Unknown";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html><body>");

        stringBuilder.append("<style>\n" +
                "table, th, td {\n" +
                "    border: 1px solid black;\n" +
                "}\n" +
                "</style>");

        stringBuilder.append("<span>SoP API version: " + version + "</span>");
        stringBuilder.append("<h1>Straight Through Processing</h1>");
        stringBuilder.append(buildStpStatusSection(cache.get_ruleConfigurationRepository(), soPPairs, blobsBaseUrl));

        stringBuilder.append("<h1>SoP Reference Service</h1>");
        stringBuilder.append(String.format("<p>Number of conditions available in SoP Reference Service: %s </p>", soPPairs.size()));
        stringBuilder.append(String.format("<p>Last polled Federal Register of Legislation for updated SoPs and Service Determinations: %s </p>", lastUpdateTime));
        stringBuilder.append(createConditionTableHtml(soPPairs, cache.get_failedUpdates(), blobsBaseUrl));
        stringBuilder.append("</body></html>");
        return stringBuilder.toString();
    }

    private static String buildStpStatusSection(RuleConfigurationRepository ruleConfigurationRepository, ImmutableSet<SoPPair> soPPairs, URL blogStorageBaseUrl) {
        ImmutableSet<String> conditionsInSopRefService = soPPairs.stream().map(p -> p.getConditionName())
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));

        StpRulesStatus status = getConditionsWhereBothRhAndBoPRules(ruleConfigurationRepository, conditionsInSopRefService);

        StringBuilder sb = new StringBuilder();
        sb.append("<p>Configured for both RH and BoP standards:</p>");


        sb.append("<ol>");
        status.getBothBopAndRh().stream().sorted().forEach(i -> sb.append(String.format("<li>%s</li>", i)));
        sb.append("</ol>");

        sb.append("<p>Configured for BoP only:");
        if (status.getBopOnly().isEmpty()) {
            sb.append(" None");
        } else {
            sb.append("<ol>");
            status.bopOnly.stream().sorted().forEach(i -> sb.append(String.format("<li>%s</li>", i)));
            sb.append("</ol>");
        }

        sb.append("</p>");


        return sb.toString();
    }

    private static class StpRulesStatus {

        private final ImmutableSet<String> bothBopAndRh;
        private final ImmutableSet<String> bopOnly;

        public StpRulesStatus(ImmutableSet<String> bothBopAndRh, ImmutableSet<String> bopOnly) {
            this.bothBopAndRh = bothBopAndRh;
            this.bopOnly = bopOnly;
        }

        public ImmutableSet<String> getBothBopAndRh() {
            return bothBopAndRh;
        }

        public ImmutableSet<String> getBopOnly() {
            return bopOnly;
        }
    }

    public static StpRulesStatus getConditionsWhereBothRhAndBoPRules(RuleConfigurationRepository ruleConfigurationRepository, ImmutableSet<String> conditionsAvailableInSopRefService) {
        Set<String> rhConditions = ruleConfigurationRepository.getBoPItems().stream()
                .map(r -> r.getConditionName())
                .distinct()
                .filter(r -> conditionsAvailableInSopRefService.contains(r))
                .collect(Collectors.toSet());

        Set<String> bopConditions = ruleConfigurationRepository.getBoPItems()
                .stream()
                .map(i -> i.getConditionName())
                .distinct()
                .filter(r -> conditionsAvailableInSopRefService.contains(r))
                .collect(Collectors.toSet());

        Set<String> bothRhAndBop = Sets.intersection(rhConditions, bopConditions);

        Set<String> bopOnly = Sets.difference(bopConditions, rhConditions);

        return new StpRulesStatus(ImmutableSet.copyOf(bothRhAndBop), ImmutableSet.copyOf(bopOnly));
    }


    public static String createConditionTableHtml(ImmutableSet<SoPPair> soPPairs, ImmutableSet<InstrumentChange> failedUpdates, URL azureBlobBaseUrl) {
        StringBuilder sb = new StringBuilder();
        // condition name
        // commencement date
        // RH, BoP links
        // azure links
        sb.append("<table align=\"left\">");

        sb.append("<tr><th>Condition</th> <th>Effective From (Canberra time)</th> <th>Source</th> <th>Parsed</th> <th>ICD Codes</th> <th>Unapplied updates</th> <tr>");

        soPPairs.stream()
                .sorted(Comparator.comparing(SoPPair::getEffectiveFromDate).reversed())
                .forEach(soPPair -> {
                    sb.append("<tr>");
                    sb.append(String.format("<td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td>",
                            soPPair.getConditionName(),
                            soPPair.getEffectiveFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            String.format("<a href=%s>RH</a> | <a href=%s>BoP</a>",
                                    buildFrlDetailsLink(soPPair.getRhSop().getRegisterId()),
                                    buildFrlDetailsLink(soPPair.getBopSop().getRegisterId())),
                            String.format("<a href=%s>RH</a> | <a href=%s>BoP</a>",
                                    buildAzureLink(soPPair.getRhSop().getRegisterId(), azureBlobBaseUrl),
                                    buildAzureLink(soPPair.getBopSop().getRegisterId(), azureBlobBaseUrl)),
                            buildIcdCodeList(soPPair),
                            buildUnappliedUpdatesHtml(
                                    ImmutableList.of(soPPair.getRhSop().getRegisterId(), soPPair.getBopSop().getRegisterId()),
                                    failedUpdates
                            )


                            )

                    );
                    sb.append("</tr>");
                });

        sb.append("</table>");

        return sb.toString();
    }

    private static String buildFrlDetailsLink(String registerId) {
        return "https://legislation.gov.au/Details/" + registerId;
    }

    private static String buildAzureLink(String registerId, URL baseUrl) {

//https://dvasopapistoragedevtest.blob.core.windows.net/sops/F2008L04143
        try {
            return (new URL(baseUrl, "sops/" + registerId)).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String buildConfigLink(String csvName, URL baseUrl) {
        try {
            return (new URL(baseUrl, "ruleconfiguration/" + csvName)).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }


    private static String buildIcdCodeList(SoPPair soPPair) {
        List<String> codes = soPPair.getICDCodes().stream().map(c -> String.format("%s %s<br>", c.getVersion(), c.getCode()))
                .sorted()
                .collect(Collectors.toList());
        return String.join("<br>", codes);
    }


    private static String buildIcdCodeCell(ImmutableSet<ICDCode> icdCodes) {
        List<String> codes = icdCodes.stream().map(c -> String.format("%s %s", c.getVersion(), c.getCode()))
                .sorted()
                .collect(Collectors.toList());
        return String.join(", ", codes);
    }

    private static Optional<String> findSuperseding(String registerId, ImmutableSet<InstrumentChange> failedUpdates) {
        // where source is register id, return target
        Optional<String> superseding = failedUpdates.stream()
                .filter(instrumentChange -> instrumentChange.getSourceInstrumentId().contentEquals(registerId) && !instrumentChange.getTargetInstrumentId().contentEquals(registerId))
                .map(c -> c.getTargetInstrumentId())
                .findFirst();
        return superseding;
    }

    private static String buildUnappliedUpdatesHtml(ImmutableList<String> registerIds, ImmutableSet<InstrumentChange> failedUpdates) {

        List<String> links = registerIds.stream()
                .map(r -> findSuperseding(r, failedUpdates))
                .filter(r -> r.isPresent())
                .map(i -> String.format("<a href=%s>%s</a>", buildFrlDetailsLink(i.get()),i.get())).collect(Collectors.toList());
        return String.join("<br>", links);
    }


}
