package au.gov.dva.sopapi.sopsupport.casesummary;

import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import com.google.common.collect.ImmutableList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedString;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by michael carter on 23/02/2017.
 */
public class ServiceTypePie {
    // This version calculates the war days and peace days from the service history before calling the
    // actual pie creator.
    public static ImmutableList<byte[]> createPieImages(Service service, Predicate<Deployment> isOperational) throws IOException {

        int warDays = 0;
        int peaceDays = 0;
        for (Deployment deployment : service.getDeployments()) {
            Optional<LocalDate> optEndDate = deployment.getEndDate();
            LocalDate endDate;

            if (optEndDate.isPresent()) {
                endDate = optEndDate.get();
            } else {
                endDate = LocalDate.now();
            }

            long numberOfDays = ChronoUnit.DAYS.between(deployment.getStartDate(), endDate) + 1; // +1 to make the dates inclusive

            if (isOperational.test(deployment))
            {
                warDays += numberOfDays;
            }
            else {
                peaceDays += numberOfDays;
            }
        }

        return createPieImages(warDays, peaceDays);
    }

    public static ImmutableList<byte[]> createPieImages(int warDays, int peaceDays) throws IOException {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Warlike or Non-warlike Service (days)", warDays);
        dataset.setValue("Peacetime Service (days)", peaceDays);

        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());

        JFreeChart chart = ChartFactory.createPieChart("Types of service", dataset, true, false, false);
        PiePlot plot = (PiePlot)chart.getPlot();
        plot.setLabelGenerator(new PieSectionLabelGenerator() {
            @Override
            public String generateSectionLabel(PieDataset dataset, Comparable key) {
                return key.toString() + ": " + dataset.getValue(key).intValue();
            }

            @Override
            public AttributedString generateAttributedSectionLabel(PieDataset dataset, Comparable key) {
                return new AttributedString(this.generateSectionLabel(dataset, key));
            }
        });
        chart.setBackgroundPaint(Color.white);

        ChartUtilities.applyCurrentTheme(chart);

        BufferedImage image = chart.createBufferedImage(600, 500);
        byte[] imageBytes = ChartUtilities.encodeAsPNG(image, true, 9);

        return ImmutableList.of(imageBytes);

    }
}
