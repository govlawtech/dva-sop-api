package au.gov.dva.sopapi.sopsupport.casesummary;


import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import com.google.common.collect.ImmutableList;
import org.imgscalr.Scalr;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.labels.IntervalXYItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.gantt.XYTaskDataset;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.TextAnchor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.function.Predicate;


public class Timeline {


    public static ImmutableList<byte[]> createTimelineImages(Service service, Predicate<Deployment> isOperational) throws IOException {

        TaskSeriesCollection tasks = new TaskSeriesCollection();
        TaskSeries operationalService = new TaskSeries("Operational Service");
        TaskSeries peacetimeService = new TaskSeries("Peacetime Service");

        for (Deployment deployment : service.getDeployments()) {
            Date startDate = convertOdtToActDate(deployment.getStartDate());

            Optional<OffsetDateTime> optEndDate = deployment.getEndDate();
            Date endDate;

            if (optEndDate.isPresent()) {
                endDate = convertOdtToActDate(optEndDate.get());
            } else {
                endDate = new Date();
            }

            if (isOperational.test(deployment))
            {
                operationalService.add(new Task(deployment.getOperationName(), new SimpleTimePeriod(startDate, endDate)));

            }
            else {
                peacetimeService.add(new Task(deployment.getOperationName(), new SimpleTimePeriod(startDate, endDate)));
            }
        }

        tasks.add(operationalService);
        tasks.add(peacetimeService);

        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());

        JFreeChart chart = ChartFactory.createXYBarChart("Service History","Resource", false, "Timing", new XYTaskDataset(tasks), PlotOrientation.HORIZONTAL, true, false, false);

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRangePannable(true);
        SymbolAxis xAxis = new SymbolAxis("Service Type", new String[] {"Operational",
                "Peacetime"});
        xAxis.setGridBandsVisible(false);
        plot.setDomainAxis(xAxis);
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();

        renderer.setMargin(0.6);
        renderer.setUseYInterval(true);
        renderer.setBaseItemLabelGenerator(new CustomLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        renderer.setDrawBarOutline(true);
        renderer.setShadowVisible(false);

        renderer.setGradientPaintTransformer(null);
        renderer.setBarPainter(new StandardXYBarPainter());

        ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER);
        renderer.setSeriesPositiveItemLabelPosition(0, p);
        renderer.setSeriesPositiveItemLabelPosition(1, p);

        ItemLabelPosition p2 = new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER_LEFT,
                TextAnchor.CENTER_LEFT, -Math.PI / 4.0);
        renderer.setPositiveItemLabelPositionFallback(p2);

        plot.setRangeAxis(new DateAxis("Date"));
        ChartUtilities.applyCurrentTheme(chart);

        BufferedImage image = chart.createBufferedImage(1000, 400);
        image = Scalr.rotate(image, Scalr.Rotation.CW_90);
        byte[] imageBytes = ChartUtilities.encodeAsPNG(image, true, 9);


        return ImmutableList.of(imageBytes);
    }

    private static Date convertOdtToActDate(OffsetDateTime offsetDateTime) {
        // return Date at midnight AM in ACT
        LocalDate actLocalDate = DateTimeUtils.odtToActLocalDate(offsetDateTime);
        return Date.from(actLocalDate.atStartOfDay().atZone(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE)).toInstant());
    }



    /**
     * A custom label generator.
     */
    static class CustomLabelGenerator extends IntervalXYItemLabelGenerator {
        private static final long serialVersionUID = 1L;

        @Override
        public String generateLabel(XYDataset dataset, int series, int item) {
            XYTaskDataset taskDataset = (XYTaskDataset) dataset;
            TaskSeriesCollection tasks = taskDataset.getTasks();
            TaskSeries taskSeries = tasks.getSeries(series);
            Task task = taskSeries.get(item);
            return task.getDescription();
        }
    }
}
