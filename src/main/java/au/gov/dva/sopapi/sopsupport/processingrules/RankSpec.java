package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.Rank;

public class RankSpec implements RankSpecification {

    private Rank rank;
    private final Integer requiredWeeksCfts;
    private final Integer accumPerWeek;
    private final Integer rhDaysOfOpServiceInLast10Years;

    public RankSpec(Rank rank, Integer requiredWeeksCfts, Integer accumPerWeek, Integer rhDaysOfOpServiceInLast10Years) {
        this.rank = rank;
        this.requiredWeeksCfts = requiredWeeksCfts;
        this.accumPerWeek = accumPerWeek;

        this.rhDaysOfOpServiceInLast10Years = rhDaysOfOpServiceInLast10Years;
    }


    @Override
    public Integer getRequiredWeeksCfts() {
        return requiredWeeksCfts;
    }

    @Override
    public Integer getAccumPerWeek() {
        return accumPerWeek;
    }

    @Override
    public Integer getRhDaysOfOpServiceInLast10Years() {
        return rhDaysOfOpServiceInLast10Years;
    }

    @Override
    public Rank getRank() {
        return rank;
    }
}


