package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.MockLumbarSpondylosisSop;
import au.gov.dva.sopref.GetSopFactors;
import au.gov.dva.sopref.dtos.SoPRefDto;
import au.gov.dva.sopref.interfaces.model.IncidentType;
import au.gov.dva.sopref.interfaces.model.SoP;
import au.gov.dva.sopref.interfaces.model.StandardOfProof;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

public class SoPReferenceServiceTests {

    @Test
    public void testSerializationOfDto() {

        SoP mockSop = new MockLumbarSpondylosisSop();
        StandardOfProof standardOfProof = StandardOfProof.ReasonableHypothesis;
        IncidentType incidentType = IncidentType.Aggravation;

        String json = GetSopFactors.buildSopRefJsonResponse(ImmutableSet.of(mockSop),incidentType,standardOfProof);
        System.out.print(json);
        Assert.assertTrue(!json.isEmpty());
    }

}
