package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.StoredServiceDetermination;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestUtils {
    public static String prettyPrint(JsonNode jsonNode) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    public static LocalDate odtOf(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    public static OffsetDateTime actOdtOf(int year, int month, int day) {
        return DateTimeUtils.localDateToLastMidnightCanberraTime(LocalDate.of(year, month, day));
    }

    public static List<Operation> getAllDeclaredOperations() throws IOException {

        URL warlikeDetJson = Resources.getResource("serviceDeterminations/F2016L00994.json");
        String warlikeString = Resources.toString(warlikeDetJson, Charsets.UTF_8);

        URL nonWarlikeDetJson = Resources.getResource("serviceDeterminations/F2016L00995.json");
        String nonWarlikeString = Resources.toString(nonWarlikeDetJson, Charsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        ServiceDetermination warlikeDetermination = StoredServiceDetermination.fromJson(objectMapper.readTree(warlikeString));
        ServiceDetermination nonWarlikeDetermination = StoredServiceDetermination.fromJson(objectMapper.readTree(nonWarlikeString));

        List<Operation> allOpsOrderedByStartDateAscending = ImmutableList.copyOf(Iterables.concat(warlikeDetermination.getOperations(), nonWarlikeDetermination.getOperations())).stream()
                .sorted((o1, o2) -> o1.getStartDate().compareTo(o2.getStartDate()))
                .collect(Collectors.toList());

        return allOpsOrderedByStartDateAscending;


    }

    public static ServiceDetermination getWarlikeDetermination() throws IOException {
        URL warlikeDetJson = Resources.getResource("serviceDeterminations/F2017L01422.json");
        String warlikeString = Resources.toString(warlikeDetJson, Charsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        return StoredServiceDetermination.fromJson(objectMapper.readTree(warlikeString));
    }

    public static ServiceDetermination getNonWarlikeDetermination() throws IOException {
        URL nonWarlikeDetJson = Resources.getResource("serviceDeterminations/F2017L01411.json");
        String nonWarlikeString = Resources.toString(nonWarlikeDetJson, Charsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        return StoredServiceDetermination.fromJson(objectMapper.readTree(nonWarlikeString));
    }

    public static ImmutableList<Deployment> getTestDeployments() throws IOException {
        List<Operation> allOpsAscendingByStarDate = getAllDeclaredOperations();
        List<Deployment> acc = new ArrayList<>();
        long maxDays = 200;
        return getMaxDeploymentsRec(acc, allOpsAscendingByStarDate, maxDays);
    }


    public static ImmutableList<Deployment> getMaxDeploymentsRec(List<Deployment> acc, List<Operation> remainingOpsSortedAscendingByStartDate, long maxDays) {
        if (remainingOpsSortedAscendingByStartDate.isEmpty())
            return ImmutableList.copyOf(acc);

        if (acc.isEmpty()) {
            Operation firstOp = remainingOpsSortedAscendingByStartDate.get(0);
            Deployment deployment = createDeploymentFromOp(firstOp.getStartDate(), firstOp, maxDays);
            acc.add(deployment);
            remainingOpsSortedAscendingByStartDate.remove(0);
            return getMaxDeploymentsRec(acc, remainingOpsSortedAscendingByStartDate, maxDays);
        } else {
            Deployment lastDeployment = acc.get(acc.size() - 1);
            Operation nextOp = remainingOpsSortedAscendingByStartDate.get(0);
            if (nextOp.getStartDate().isBefore(lastDeployment.getEndDate().get())) {
                if (!nextOp.getEndDate().isPresent() || nextOp.getEndDate().get().isAfter(lastDeployment.getEndDate().get())) {
                    Deployment nextDeployment = createDeploymentFromOp(lastDeployment.getEndDate().get().plusDays(1), nextOp, maxDays);
                    acc.add(nextDeployment);
                    remainingOpsSortedAscendingByStartDate.remove(0);
                    return getMaxDeploymentsRec(acc, remainingOpsSortedAscendingByStartDate, maxDays);
                } else {
                    remainingOpsSortedAscendingByStartDate.remove(0); // missed this op
                    return getMaxDeploymentsRec(acc, remainingOpsSortedAscendingByStartDate, maxDays);
                }
            } else {

                Deployment filler = createFillerPeactimeOp(lastDeployment.getEndDate().get().plusDays(1),
                        lastDeployment.getEndDate().get().plusDays(maxDays));

                acc.add(filler);

                return getMaxDeploymentsRec(acc, remainingOpsSortedAscendingByStartDate, maxDays);
            }

        }
    }

    private static Deployment createFillerPeactimeOp(LocalDate startDate, LocalDate endDate) {
        return new Deployment() {
            @Override
            public String getOperationName() {
                return "Peace is Our Profession";
            }

            @Override
            public LocalDate getStartDate() {
                return startDate;
            }

            @Override
            public Optional<LocalDate> getEndDate() {
                return Optional.of(endDate);
            }

            @Override
            public String getEvent() {
                return "Within Specified Area";
            }
        };
    }

    private static Deployment createDeploymentFromOp(LocalDate deploymentStartDate, Operation operation, long maxDays) {
        assert (deploymentStartDate.isAfter(operation.getStartDate()) || deploymentStartDate.isEqual(operation.getStartDate()));
        return new Deployment() {
            @Override
            public String getOperationName() {
                return operation.getName();
            }

            @Override
            public LocalDate getStartDate() {
                return deploymentStartDate;
            }

            @Override
            public Optional<LocalDate> getEndDate() {
                if (operation.getEndDate().isPresent()) {
                    return operation.getEndDate();
                } else {
                    return Optional.of(deploymentStartDate.plusDays(maxDays));
                }
            }

            @Override
            public String getEvent() {
                return "Within Specified Area";
            }

        };
    }


    //http://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory



}


