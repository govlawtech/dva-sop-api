import au.gov.dva.sopref.interfaces.model.Condition;

import java.time.LocalDate;

public class ConditionMock implements Condition {

    private String _name;
    private String _icdCode;
    private String _type;
    private LocalDate _onsetStartDate;
    private LocalDate _onsetEndDate;
    private LocalDate _aggravationStartDate;
    private LocalDate _aggravationEndDate;

    public ConditionMock() {

    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getICDCode() {
        return _icdCode;
    }

    public void setICDCode(String icdCode) {
        _icdCode = icdCode;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public LocalDate getOnsetStartDate() {
        return _onsetStartDate;
    }

    public void setOnsetStartDate(LocalDate onsetStartDate) {
        _onsetStartDate = onsetStartDate;
    }

    public LocalDate getOnsetEndDate() {
        return _onsetEndDate;
    }

    public void setOnsetEndDate(LocalDate onsetEndDate) {
        _onsetEndDate = onsetEndDate;
    }

    public LocalDate getAggravationStartDate() {
        return _aggravationStartDate;
    }

    public void setAggravationStartDate(LocalDate aggravationStartDate) {
        _aggravationStartDate = aggravationStartDate;
    }

    public LocalDate getAggravationEndDate() {
        return _aggravationEndDate;
    }

    public void setAggravationEndDate(LocalDate aggravationEndDate) {
        _aggravationEndDate = aggravationEndDate;
    }
}
