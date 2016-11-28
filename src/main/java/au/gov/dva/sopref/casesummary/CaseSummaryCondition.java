package au.gov.dva.sopref.casesummary;

import au.gov.dva.sopref.interfaces.model.Condition;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.time.LocalDate;
import java.util.Optional;

public class CaseSummaryCondition extends CaseSummaryComponent implements Condition {

    private String _name;
    private String _icdCode;
    private String _conditionType;
    private LocalDate _onsetStartDate;
    private Optional<LocalDate> _onsetEndDate;
    private Optional<LocalDate> _aggravationStartDate;
    private Optional<LocalDate> _aggravationEndDate;

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
    public Optional<LocalDate> getOnsetEndDate() {
        return _onsetEndDate;
    }

    public void setOnsetEndDate(LocalDate onsetEndDate) {
        _onsetEndDate = Optional.of(onsetEndDate);
    }

    @Override
    public Optional<LocalDate> getAggravationStartDate() {
        return _aggravationStartDate;
    }

    public void setAggravationStartDate(LocalDate aggravationStartDate) {
        _aggravationStartDate = Optional.of(aggravationStartDate);
    }

    @Override
    public Optional<LocalDate> getAggravationEndDate() {
        return _aggravationEndDate;
    }

    public void setAggravationEndDate(LocalDate aggravationEndDate) {
        _aggravationEndDate = Optional.of(aggravationEndDate);
    }

    @Override
    public void addToDocument(XWPFDocument document) {

    }
}
