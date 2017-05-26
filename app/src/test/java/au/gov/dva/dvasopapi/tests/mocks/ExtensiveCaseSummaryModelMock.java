package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.interfaces.model.casesummary.CaseSummaryModel;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import com.google.common.collect.ImmutableSet;

public class ExtensiveCaseSummaryModelMock implements CaseSummaryModel{

    public Condition getCondition() {
        return new LumbarSpondylosisConditionMock();
    }

    public ServiceHistory getServiceHistory() {
        return new ExtensiveServiceHistoryMock();
    }

    public SoP getApplicableSop() {
        return new MockLumbarSpondylosisSopRH();
    }

    public String getThresholdProgress() {
        return "10kg/week * 10 weeks = 100kg";
    }

    public ImmutableSet<Factor> getFactorsConnectedToService() {
        return ImmutableSet.of(new Factor() {
            @Override
            public String getParagraph() {
                return "6(a)";
            }

            @Override
            public String getText() {
                return "having inflammatory joint disease in the lumbar spine " +
                        "before the clinical onset of lumbar spondylosis";
            }



            @Override
            public ImmutableSet<DefinedTerm> getDefinedTerms() {
                return ImmutableSet.of(new DefinedTerm() {
                    @Override
                    public String getTerm() {
                        return "inflammatory joint disease";
                    }

                    @Override
                    public String getDefinition() {
                        return "means rheumatoid arthritis, reactive arthritis, " +
                                "psoriatic arthropathy, ankylosing spondylitis, or " +
                                "arthritis associated with Crohn’s disease or " +
                                "ulcerative colitis";
                    }
                });
            }
        }, new Factor() {
            @Override
            public String getParagraph() {
                return "6(m)";
            }

            @Override
            public String getText() {
                return "extreme forward flexion of the lumbar spine for a cumulative " +
                        "total of at least 1 500 hours before the clinical onset of " +
                        "lumbar spondylosis";
            }


            @Override
            public ImmutableSet<DefinedTerm> getDefinedTerms() {
                return ImmutableSet.of(new DefinedTerm() {
                    @Override
                    public String getTerm() {
                        return "extreme forward flexion of the lumbar spine";
                    }

                    @Override
                    public String getDefinition() {
                        return "means being in a posture involving greater than 90 " +
                                "degrees of trunk flexion";
                    }
                });
            }
        });
    }

    public CaseTrace getCaseTrace() {
        SopSupportCaseTrace caseTrace = new SopSupportCaseTrace("mock case");
        caseTrace.setActualCftsDays(500);
        caseTrace.setRequiredCftsDays(400);
        caseTrace.setActualOperationalDays(100);
        caseTrace.setRequiredOperationalDaysForRh(50);
        caseTrace.setApplicableStandardOfProof(StandardOfProof.ReasonableHypothesis);
        return caseTrace;
    }

    public Recommendation getRecommendation() { return Recommendation.APPROVED; }
}
