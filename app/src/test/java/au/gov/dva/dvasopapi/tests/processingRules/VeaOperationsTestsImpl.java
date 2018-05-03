package au.gov.dva.dvasopapi.tests.processingRules;

import au.gov.dva.sopapi.interfaces.model.SingleOnlineClaimFormVeaOp;
import au.gov.dva.sopapi.sopsupport.vea.ReferenceData;
import au.gov.dva.sopapi.sopsupport.vea.ReferenceDataRoot;
import au.gov.dva.sopapi.sopsupport.vea.SingleOnlineClaimFormVeaOps;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

public class VeaOperationsTests {

    @Test
    public void deserialiseSOCFData() throws IOException {
        URL url = Resources.getResource("socfVeaOps.yaml");
        String yaml = Resources.toString(url, Charset.forName("UTF-8"));
        ImmutableList<SingleOnlineClaimFormVeaOp> result = SingleOnlineClaimFormVeaOps.fromYaml(yaml);







    }
}
