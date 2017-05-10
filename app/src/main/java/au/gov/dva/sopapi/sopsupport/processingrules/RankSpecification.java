package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import com.google.common.collect.ImmutableSet;

public interface RankSpecification {
    Integer getRequiredWeeksCfts();

    Integer getAccumPerWeek();

    Integer getRhDaysOfOpServiceInLast10Years();

    Rank getRank();
}

