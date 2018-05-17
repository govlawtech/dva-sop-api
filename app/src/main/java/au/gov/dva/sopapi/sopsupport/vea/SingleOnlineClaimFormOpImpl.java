package au.gov.dva.sopapi.sopsupport.vea;

import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.SingleOnlineClaimFormVeaOp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SingleOnlineClaimFormOpImpl implements SingleOnlineClaimFormVeaOp {

    private String operation;
    private LocalDate startDate;
    private Optional<LocalDate> endDate;
    private Boolean isHazardous;
    private Boolean isMcraNonWarlike;
    private Boolean isMcraWarlike;
    private Boolean isOperational;
    private Boolean isPeacekeeping;
    private Boolean isWarlike;

    public SingleOnlineClaimFormOpImpl(String operation, LocalDate startDate, Optional<LocalDate> endDate, Boolean isHazardous, Boolean isMrcaNonWarlike, Boolean isMrcaWarlike, Boolean isOperational, Boolean isPeacekeeping, Boolean isWarlike)
    {
        this.operation = operation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isHazardous = isHazardous;
        this.isMcraNonWarlike = isMrcaNonWarlike;
        this.isMcraWarlike = isMrcaWarlike;
        this.isOperational = isOperational;
        this.isPeacekeeping = isPeacekeeping;
        this.isWarlike = isWarlike;
    }

    @Override
    public String getOperation() {
        return operation;
    }

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public Optional<LocalDate> getEndDate() {
        return endDate;
    }

    @Override
    public Boolean getIsHazardous() {
        return isHazardous;
    }

    @Override
    public Boolean getIsMcraNonWarlike() {
        return isMcraNonWarlike;
    }

    @Override
    public Boolean getIsMrcaWarlike() {
        return isMcraWarlike;
    }

    @Override
    public Boolean getIsOperational() {
        return isOperational;
    }

    @Override
    public Boolean getIsPeacekeeping() {
        return isPeacekeeping;
    }

    @Override
    public Boolean getIsWarlike() {
        return isWarlike;
    }


    public static ServiceRegion toServiceRegion(SingleOnlineClaimFormVeaOp singleOnlineClaimFormVeaOp)
    {
        ServiceRegion sr = new ServiceRegion();
        sr.setOperationName(singleOnlineClaimFormVeaOp.getOperation());
        sr.setStartDate(DateTimeFormatter.ISO_LOCAL_DATE.format(singleOnlineClaimFormVeaOp.getStartDate()));
        if (singleOnlineClaimFormVeaOp.getEndDate().isPresent()) sr.setEndDate(DateTimeFormatter.ISO_LOCAL_DATE.format(singleOnlineClaimFormVeaOp.getEndDate().get()));
        sr.setHazardous(singleOnlineClaimFormVeaOp.getIsHazardous());
        sr.setMrcaNonWarlike(singleOnlineClaimFormVeaOp.getIsMcraNonWarlike());
        sr.setMrcaWarlike(singleOnlineClaimFormVeaOp.getIsMrcaWarlike());
        sr.setOperational(singleOnlineClaimFormVeaOp.getIsOperational());
        sr.setPeacekeeping(singleOnlineClaimFormVeaOp.getIsPeacekeeping());
        sr.setWarlike(singleOnlineClaimFormVeaOp.getIsWarlike());

        return sr;


    }



}
