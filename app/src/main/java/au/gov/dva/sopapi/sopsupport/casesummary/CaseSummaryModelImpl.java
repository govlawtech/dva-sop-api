package au.gov.dva.sopapi.sopsupport.casesummary;

import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.interfaces.model.casesummary.CaseSummaryModel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

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
    private CaseTrace caseTrace;
    private Recommendation recommendation;

    // Construction logic
    public CaseSummaryModelImpl(Condition condition, ServiceHistory serviceHistory, SoP applicableSop, ImmutableSet<Factor> factorsConnectedToService, CaseTrace caseTrace, Recommendation recommendation) throws IllegalArgumentException {
        this.condition = condition;
        this.serviceHistory = serviceHistory;
        this.applicableSop = applicableSop;
        this.factorsConnectedToService = factorsConnectedToService;
        this.recommendation = recommendation;
        this.caseTrace = caseTrace;
        thresholdProgress = ""; // What should this be?

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

    @Override
    public CaseTrace getCaseTrace() {
        return caseTrace;
    }

    @Override
    public Recommendation getRecommendation() { return recommendation; }
}
