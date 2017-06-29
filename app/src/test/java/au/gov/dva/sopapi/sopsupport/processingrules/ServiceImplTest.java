package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.ServiceHistoryDto;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Created by work on 26/06/17.
 */
public class ServiceImplTest {
    @Test
    public void TestFiltering() throws IOException {
        URL requestUrl = Resources.getResource("exampleRequests/request.json");
        String jsonString = Resources.toString(requestUrl, Charsets.UTF_8);
        SopSupportRequestDto requestDto = SopSupportRequestDto.fromJsonString(jsonString);
        ServiceHistory toTest = DtoTransformations.serviceHistoryFromDto(requestDto.get_serviceHistoryDto());

        Assert.assertTrue(toTest instanceof ServiceHistoryImpl);
        Assert.assertTrue(toTest.getServices().size() == 2);
        List<Service> services = toTest.getServices().asList();
        Assert.assertTrue(services.get(0).getDeployments().size() == 7);
        Assert.assertTrue(services.get(1).getDeployments().size() == 0);

        ServiceHistory filteredHistory = toTest.filterServiceHistoryByEvents(Arrays.asList("within specified area"));

        Assert.assertTrue(filteredHistory instanceof ServiceHistoryImpl);
        Assert.assertTrue(filteredHistory.getServices().size() == 2);
        List<Service> filteredServices = filteredHistory.getServices().asList();
        Assert.assertTrue(filteredServices.get(0).getDeployments().size() == 5);
        Assert.assertTrue(filteredServices.get(1).getDeployments().size() == 0);
    }
}
