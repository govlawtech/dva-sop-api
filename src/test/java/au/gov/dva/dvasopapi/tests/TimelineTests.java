package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.ExtensiveServiceHistoryMock;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.sopsupport.casesummary.Timeline;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class TimelineTests {


    
    @Test
    public void createServiceHistory() throws IOException {
        Service testdata = new ExtensiveServiceHistoryMock().getServices().asList().get(0);

        Predicate<String> isOperational = s -> {
            if (s.contains("Peace is Our Profession"))
                return false;
            else return true;
        };

        ImmutableList<byte[]> result = Timeline.createTimelineImages(testdata, isOperational);

        // for debugging: show the images
        for (int i = 0; i < result.size(); i++) {

            Path tempFilePath = Files.createTempFile(String.format("timelineImage_%d_", i), ".png");

            try (FileOutputStream outputStream = new FileOutputStream(tempFilePath.toFile())) {
                outputStream.write(result.get(i));
                outputStream.close();
            }

            System.out.println(tempFilePath.toAbsolutePath());
        }


        Assert.assertTrue(result.size() > 0);


    }
}
