package au.gov.dva.sopref.casesummary;

import au.gov.dva.sopref.interfaces.model.Condition;
import java.time.LocalDate;

public class CaseSummaryCondition implements Condition {

    private String _name;
    private String _icdCode;
    private String _conditionType;
    private LocalDate _onsetStartDate;
    private LocalDate _onsetEndDate;
    private LocalDate _aggravationStartDate;
    private LocalDate _aggravationEndDate;

    public CaseSummaryCondition() {

    }

    @Override
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @Override
    public String getICDCode() {
        return _icdCode;
    }

    public void setICDCode(String icdCode) {
        _icdCode = icdCode;
    }

    @Override
    public String getType() {
        return _conditionType;
    }

    public void setType(String conditionType) {
        _conditionType = conditionType;
    }

    @Override
    public LocalDate getOnsetStartDate() {
        return _onsetStartDate;
    }

    public void setOnsetStartDate(LocalDate onsetStartDate) {
        _onsetStartDate = onsetStartDate;
    }

    @Override
    public LocalDate getOnsetEndDate() {
        return _onsetEndDate;
    }

    public void setOnsetEndDate(LocalDate onsetEndDate) {
        _onsetEndDate = onsetEndDate;
    }

    @Override
    public LocalDate getAggravationStartDate() {
        return _aggravationStartDate;
    }

    public void setAggravationStartDate(LocalDate aggravationStartDate) {
        _aggravationStartDate = aggravationStartDate;
    }

    @Override
    public LocalDate getAggravationEndDate() {
        return _aggravationEndDate;
    }

    public void setAggravationEndDate(LocalDate aggravationEndDate) {
        _aggravationEndDate = aggravationEndDate;
    }
}
