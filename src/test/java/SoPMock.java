import au.gov.dva.sopref.interfaces.model.Factor;
import au.gov.dva.sopref.interfaces.model.InstrumentNumber;
import au.gov.dva.sopref.interfaces.model.SoP;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;

public class SoPMock implements SoP {

    private String _registerId;
    private InstrumentNumber _instrumentNumber;
    private String _citation;
    private ImmutableSet<Factor> _aggravationFactors;
    private ImmutableSet<Factor> _onsetFactors;
    private LocalDate _commencementDate;

    public SoPMock() {

    }

    public String getRegisterId() {
        return _registerId;
    }

    public void setRegisterId(String registerId) {
        _registerId = registerId;
    }

    public InstrumentNumber getInstrumentNumber() {
        return _instrumentNumber;
    }

    public void setInstrumentNumber(InstrumentNumber instrumentNumber) {
        _instrumentNumber = instrumentNumber;
    }

    public String getCitation() {
        return _citation;
    }

    public void setCitation(String citation) {
        _citation = citation;
    }

    public ImmutableSet<Factor> getAggravationFactors() {
        return _aggravationFactors;
    }

    public void setAggravationFactors(ImmutableSet<Factor> aggravationFactors) {
        _aggravationFactors = aggravationFactors;
    }

    public ImmutableSet<Factor> getOnsetFactors() {
        return _onsetFactors;
    }

    public void setOnsetFactors(ImmutableSet<Factor> onsetFactors) {
        _onsetFactors = onsetFactors;
    }

    public LocalDate getCommencementDate() {
        return _commencementDate;
    }

    public void setCommencementDate(LocalDate commencementDate) {
        _commencementDate = commencementDate;
    }
}
