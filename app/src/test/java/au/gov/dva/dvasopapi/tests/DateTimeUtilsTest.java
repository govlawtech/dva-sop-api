package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.interfaces.model.MaybeOpenEndedInterval;
import au.gov.dva.sopapi.sopsupport.processingrules.HasDateRangeImpl;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by mc on 27/06/17.
 */
public class DateTimeUtilsTest {

    private MaybeOpenEndedInterval make(int beginYear, int beginMonth, int beginDay) {
        return new HasDateRangeImpl(LocalDate.of(beginYear, beginMonth, beginDay), Optional.empty());
    }

    private MaybeOpenEndedInterval make(int beginYear, int beginMonth, int beginDay, int endYear, int endMonth, int endDay) {
        return new HasDateRangeImpl(LocalDate.of(beginYear, beginMonth, beginDay)
                , Optional.of(LocalDate.of(endYear, endMonth, endDay)));
    }

    @Test
    public void testFlattenDateRanges() {
        // Empty
        List<MaybeOpenEndedInterval> result = DateTimeUtils.flattenDateRanges(new ArrayList<>());
        Assert.assertTrue(result.size() == 0);

        // Single
        MaybeOpenEndedInterval single = make(2011, 5, 4, 2011, 6, 7);
        result = DateTimeUtils.flattenDateRanges(Arrays.asList(single));
        Assert.assertTrue(result.size() == 1);
        Assert.assertTrue(result.get(0).getStartDate().equals(LocalDate.of(2011, 5, 4)));
        Assert.assertTrue(result.get(0).getEndDate().get().equals(LocalDate.of(2011, 6, 7)));

        // No overlap
        MaybeOpenEndedInterval disconnected1 = make(2011, 5, 4, 2011, 6, 7);
        MaybeOpenEndedInterval disconnected2 = make(2011, 7, 4, 2011, 8, 7);
        MaybeOpenEndedInterval disconnected3 = make(2012, 7, 4, 2012, 8, 7);
        MaybeOpenEndedInterval disconnected4 = make(2012, 11, 4);
        List<MaybeOpenEndedInterval> disconnected = Arrays.asList(disconnected1, disconnected2, disconnected3, disconnected4);
        result = DateTimeUtils.flattenDateRanges(disconnected);
        Assert.assertTrue(result.size() == 4);

        // Some overlap
        MaybeOpenEndedInterval partialOverlap1 = make(2011, 5, 4, 2011, 6, 7);
        MaybeOpenEndedInterval partialOverlap2 = make(2011, 5, 14, 2011, 8, 7);
        MaybeOpenEndedInterval partialOverlap3  = make(2012, 7, 4, 2012, 8, 7);
        MaybeOpenEndedInterval partialOverlap4 = make(2012, 8, 1);
        List<MaybeOpenEndedInterval> partialOverlap = Arrays.asList(partialOverlap1, partialOverlap2, partialOverlap3, partialOverlap4);
        result = DateTimeUtils.flattenDateRanges(partialOverlap);
        Assert.assertTrue(result.size() == 2);
        Assert.assertTrue(result.get(0).getStartDate().equals(LocalDate.of(2011, 5, 4)));
        Assert.assertTrue(result.get(0).getEndDate().get().equals(LocalDate.of(2011, 8, 7)));
        Assert.assertTrue(result.get(1).getStartDate().equals(LocalDate.of(2012, 7, 4)));
        Assert.assertFalse(result.get(1).getEndDate().isPresent());

        // Total overlap
        MaybeOpenEndedInterval totalOverlap1 = make(2011, 5, 4, 2011, 6, 7);
        MaybeOpenEndedInterval totalOverlap2 = make(2011, 5, 8, 2011, 6, 11);
        MaybeOpenEndedInterval totalOverlap3 = make(2011, 6, 11, 2011, 7, 7);
        List<MaybeOpenEndedInterval> totalOverlap = Arrays.asList(totalOverlap1, totalOverlap2, totalOverlap3);
        result = DateTimeUtils.flattenDateRanges(totalOverlap);
        Assert.assertTrue(result.size() == 1);
        Assert.assertTrue(result.get(0).getStartDate().equals(LocalDate.of(2011, 5, 4)));
        Assert.assertTrue(result.get(0).getEndDate().get().equals(LocalDate.of(2011, 7, 7)));

    }
}
