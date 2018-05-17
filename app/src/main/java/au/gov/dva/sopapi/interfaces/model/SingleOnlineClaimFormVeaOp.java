package au.gov.dva.sopapi.interfaces.model;

import java.time.LocalDate;
import java.util.Optional;

public interface SingleOnlineClaimFormVeaOp {
    public String getOperation();

    public LocalDate getStartDate();

    public Optional<LocalDate> getEndDate();

    public Boolean getIsHazardous();

    public Boolean getIsMcraNonWarlike();

    public Boolean getIsMrcaWarlike();

    public Boolean getIsOperational();

    public Boolean getIsPeacekeeping();

    public Boolean getIsWarlike();
}

