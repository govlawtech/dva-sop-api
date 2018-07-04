package au.gov.dva.sopapi;

import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.ICDCode;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopref.SoPs;
import au.gov.dva.sopapi.sopsupport.ConditionFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class Status {

    public static byte[] createStatusCsv(CacheSingleton cache, URL blobsBaseUrl) throws IOException {
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

        ImmutableSet<SoPPair> soPPairs = cache.get_allSopPairs();
        ImmutableSet<InstrumentChange> failedUpdates = cache.get_failedUpdates();

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
                    findSuperseding(soPPair.getRhSop().getRegisterId(), failedUpdates).orElse(""),
                    findSuperseding(soPPair.getBopSop().getRegisterId(), failedUpdates).orElse("")
            ));
        }

        for (ImmutableList<String> strings : records) {
            csvPrinter.printRecord(strings);
        }

        return csvPrinter.getOut().toString().getBytes("UTF-8");
    }


    public static String createStatusHtml(CacheSingleton cache, Repository repository, URL blobsBaseUrl, String version) {
        ImmutableSet<SoPPair> soPPairs = SoPs.groupSopsToPairs(cache.get_allSops(), OffsetDateTime.now());

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
        stringBuilder.append(createConditionTableHtml(soPPairs, cache.get_failedUpdates(), blobsBaseUrl));
        stringBuilder.append("</body></html>");
        return stringBuilder.toString();
    }

    private static String buildStpStatusSection(RuleConfigurationRepository ruleConfigurationRepository, ImmutableSet<SoPPair> soPPairs, URL blogStorageBaseUrl) {
        ImmutableSet<String> conditionsInSopRefService = soPPairs.stream().map(p -> p.getConditionName())
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));

        StpRulesStatus status = getConditionsWhereBothRhAndBoPRules(ruleConfigurationRepository, conditionsInSopRefService);

        StringBuilder sb = new StringBuilder();

        sb.append("<h2>Acute conditions:</h2>");
        sb.append("<ol>");
        ConditionFactory.getAcuteConditions().stream().sorted().forEach(i -> sb.append(String.format("<li>%s</li>", i)));
        sb.append("</ol>");

        sb.append("<h2>Wear and tear or exposure conditions:</h2>");

        sb.append("<h3>Configured for both RH and BoP:</h3>");
        sb.append("<ol>");
        status.getBothBopAndRh().stream().sorted().forEach(i -> sb.append(String.format("<li>%s</li>", i)));
        sb.append("</ol>");

        sb.append("<h3>Configured for RH only:</h3>");
        sb.append("<ol>");
        status.getRhOnly().stream().sorted().forEach(i -> sb.append(String.format("<li>%s</li>", i)));
        sb.append("</ol>");


        sb.append("<h3>Configured for BoP only:</h3>");
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
        private final ImmutableSet<String> rhOnly;

        public StpRulesStatus(ImmutableSet<String> bothBopAndRh, ImmutableSet<String> bopOnly, ImmutableSet<String> rhOnly) {
            this.bothBopAndRh = bothBopAndRh;
            this.bopOnly = bopOnly;

            this.rhOnly = rhOnly;
        }

        public ImmutableSet<String> getBothBopAndRh() {
            return bothBopAndRh;
        }

        public ImmutableSet<String> getBopOnly() {
            return bopOnly;
        }

        public ImmutableSet<String> getRhOnly() {
            return rhOnly;
        }
    }

    public static StpRulesStatus getConditionsWhereBothRhAndBoPRules(RuleConfigurationRepository ruleConfigurationRepository, ImmutableSet<String> conditionsAvailableInSopRefService) {
        Set<String> rhConditions = ruleConfigurationRepository.getRHItems().stream()
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
        Set<String> rhOnly = Sets.difference(rhConditions,bopConditions);

        return new StpRulesStatus(ImmutableSet.copyOf(bothRhAndBop), ImmutableSet.copyOf(bopOnly),ImmutableSet.copyOf(rhOnly));
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
