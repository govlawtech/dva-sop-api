package au.gov.dva.dvasopapi.tests.mocks;

import au.gov.dva.sopref.data.sops.BasicICDCode;
import au.gov.dva.sopref.interfaces.model.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;

public class MockLumbarSpondylosisSop implements SoP {
    @Override
    public String getRegisterId() {
        return "F2014L0933";
    }

    @Override
    public InstrumentNumber getInstrumentNumber() {
        return new InstrumentNumber() {
            @Override
            public int getNumber() {
                return 62;
            }

            @Override
            public int getYear() {
                return 2014;
            }
        };
    }

    @Override
    public String getCitation() {
        return "Statement of Principles concerning lumbar spondylosis No. 62 of 2014";
    }

    @Override
    public ImmutableList<Factor> getAggravationFactors() {
        return ImmutableList.of(new Factor() {
                                   @Override
                                   public String getParagraph() {
                                       return "6(ee)";
                                   }

                                   @Override
                                   public String getText() {
                                       return "inability to obtain appropriate clinical management for lumbar spondylosis";
                                   }

                                   @Override
                                   public ImmutableSet<DefinedTerm> getDefinedTerms() {
                                       return ImmutableSet.of();
                                   }
                               },
                new Factor() {
                    @Override
                    public String getParagraph() {
                        return "6(p)";
                    }

                    @Override
                    public String getText() {
                        return "having inflammatory joint disease in the lumbar spine before the clinical worsening of lumbar spondylosis";
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
                                return "means rheumatoid arthritis, reactive arthritis, sporiatic arthropathy, ankylosing spondylitis, or arthritis associated with Crohn's disease or ulcerative colitis";
                            }
                        });
                    }
                });
    }

    @Override
    public ImmutableList<Factor> getOnsetFactors() {
        return ImmutableList.of();
    }

    @Override
    public LocalDate getEffectiveFromDate() {
        return LocalDate.of(2014,7,2);
    }

    @Override
    public StandardOfProof getStandardOfProof() {
        return StandardOfProof.ReasonableHypothesis;
    }

    @Override
    public ImmutableList<ICDCode> getICDCodes() {
    return ImmutableList.of(new BasicICDCode("ICD-10-AM","M47.16"), new BasicICDCode("ICD-10-AM", "M47.17,"));
    }

}
