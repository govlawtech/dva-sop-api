package au.gov.dva.sopapi;

import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopref.SoPs;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import scala.App;
import scala.math.Ordering;
import scala.sys.process.ProcessBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public class Status {


    public static String createStatusHtml(Cache cache, Repository repository, URL blobsBaseUrl) {
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

        stringBuilder.append("<h1>Straight Through Processing</h1>");
        stringBuilder.append(buildStpStatusSection(cache.get_ruleConfigurationRepository(), soPPairs, blobsBaseUrl));

        stringBuilder.append("<h1>SoP Reference Service</h1>");
        stringBuilder.append(String.format("<p>Number of conditions available in SoP Reference Service: %s </p>", soPPairs.size()));
        stringBuilder.append(String.format("<p>Last polled Federal Register of Legislation for updated SoPs and Service Determinations: %s </p>", lastUpdateTime));

        stringBuilder.append(createConditionTableHtml(soPPairs, blobsBaseUrl));

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


        sb.append("<p>Active rule configuration: ");
        sb.append(String.format("<a href=%s>RH</a>", buildConfigLink("rh.csv", blogStorageBaseUrl)));
        sb.append(", ");
        sb.append(String.format("<a href=%s>BoP</a>", buildConfigLink("bop.csv", blogStorageBaseUrl)));
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


    public static String createConditionTableHtml(ImmutableSet<SoPPair> soPPairs, URL azureBlobBaseUrl) {
        StringBuilder sb = new StringBuilder();
        // condition name
        // commencement date
        // RH, BoP links
        // azure links
        sb.append("<table align=\"left\">");

        sb.append("<tr><th>Condition</th> <th>Effective From (Canberra time)</th> <th>Source</th> <th>Parsed</th> <th>ICD Codes</th> <tr>");

        soPPairs.stream()
                .sorted(Comparator.comparing(SoPPair::getEffectiveFromDate).reversed())
                .forEach(soPPair -> {
                    sb.append("<tr>");
                    sb.append(String.format("<td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td> <td>%s</td>",
                            soPPair.getConditionName(),
                            soPPair.getEffectiveFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            String.format("<a href=%s>RH</a> | <a href=%s>BoP</a>",
                                    buildFrlDetailsLink(soPPair.getRhSop().getRegisterId()),
                                    buildFrlDetailsLink(soPPair.getBopSop().getRegisterId())),
                            String.format("<a href=%s>RH</a> | <a href=%s>BoP</a>",
                                    buildAzureLink(soPPair.getRhSop().getRegisterId(), azureBlobBaseUrl),
                                    buildAzureLink(soPPair.getBopSop().getRegisterId(), azureBlobBaseUrl)),
                            buildIcdCodeList(soPPair)
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


}
