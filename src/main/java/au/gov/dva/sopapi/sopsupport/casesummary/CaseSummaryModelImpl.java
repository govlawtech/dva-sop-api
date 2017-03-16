package au.gov.dva.sopapi.sopsupport.casesummary;

import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.interfaces.model.casesummary.CaseSummaryModel;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import au.gov.dva.sopapi.sopsupport.ConditionFactory;
import com.google.common.collect.ImmutableSet;

import java.util.function.Predicate;

/**
 * Created by michael carter on 09/03/2017.
 */
public class CaseSummaryModelImpl implements CaseSummaryModel {

    // Private data
    private Condition condition;
    private ServiceHistory serviceHistory;
    private SoP applicableSop;
    private String thresholdProgress;
    private ImmutableSet<Factor> factorsConnectedToService;

    // Construction logic
    public CaseSummaryModelImpl(SopSupportRequestDto supportRequest, ImmutableSet<SoPPair> soPPairs, Predicate<Deployment> isOperational) throws IllegalArgumentException {
        if (supportRequest == null) throw new IllegalArgumentException("parameter: supportRequest cannot be null");

        condition = ConditionFactory.create(soPPairs, supportRequest.get_conditionDto());
        serviceHistory = DtoTransformations.serviceHistoryFromDto(supportRequest.get_serviceHistoryDto());
        applicableSop = condition.getProcessingRule().getApplicableSop(condition, serviceHistory, isOperational);
        thresholdProgress = ""; // What should this be?
        factorsConnectedToService = ImmutableSet.copyOf(condition.getApplicableFactors(applicableSop));
    }

    // Implementation of CaseSummaryModel interface
    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public ServiceHistory getServiceHistory() {
        return serviceHistory;
    }

    @Override
    public SoP getApplicableSop() {
        return applicableSop;
    }

    @Override
    public String getThresholdProgress() {
        return thresholdProgress;
    }

    @Override
    public ImmutableSet<Factor> getFactorsConnectedToService() {
        return factorsConnectedToService;
    }
}
