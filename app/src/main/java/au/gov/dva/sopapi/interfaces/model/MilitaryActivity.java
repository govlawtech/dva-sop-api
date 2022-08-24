package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.JustifiedMilitaryActivityDto;
import au.gov.dva.sopapi.dtos.sopsupport.MilitaryOperationType;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;


public class MilitaryActivity {

    private final String _name;
    public String getName() {
        return _name;
    }

    private final LocalDate _startDate;
    public LocalDate getStartDate(){
        return _startDate;
    }

    private Optional<LocalDate> _endDate;
    public Optional<LocalDate> getEndDate()
    {
        return _endDate;
    }

    private final MilitaryOperationType _type;
    public MilitaryOperationType getMilitaryOperationType() {
        return _type;
    }

    private final String _legalSource;
    public String getLegalSource()
    {
        return _legalSource;
    }

    public MilitaryActivity(
            String name,
            LocalDate startDate,
            Optional<LocalDate> endDate,
            MilitaryOperationType type,
            String legalSource
    ){
       _name = name;
       _startDate = startDate;
       _endDate = endDate;
       _type = type;
       _legalSource = legalSource;
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MilitaryActivity that = (MilitaryActivity) o;
        return Objects.equals(_name, that._name) && Objects.equals(_startDate, that._startDate) && Objects.equals(_endDate, that._endDate) && _type == that._type && Objects.equals(_legalSource, that._legalSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _startDate, _endDate, _type, _legalSource);
    }
}
