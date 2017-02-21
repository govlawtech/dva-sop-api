package au.gov.dva.sopapi.sopsupport.processingrules;

public class RankSpec {

    private final Integer requiredWeeksCfts;
    private final Integer accumPerWeek;
    private final Integer rhDaysOfOpServiceInLast10Years;

    public RankSpec(Integer requiredWeeksCfts, Integer accumPerWeek, Integer rhDaysOfOpServiceInLast10Years) {
        this.requiredWeeksCfts = requiredWeeksCfts;
        this.accumPerWeek = accumPerWeek;

        this.rhDaysOfOpServiceInLast10Years = rhDaysOfOpServiceInLast10Years;
    }


    public Integer getRequiredWeeksCfts() {
        return requiredWeeksCfts;
    }

    public Integer getAccumPerWeek() {
        return accumPerWeek;
    }

    public Integer getRhDaysOfOpServiceInLast10Years() {
        return rhDaysOfOpServiceInLast10Years;
    }
}
