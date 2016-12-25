package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.model.Deployment;

import java.time.OffsetDateTime;
import java.util.Optional;

public class DeploymentImpl implements Deployment {

    private final String operationName;
    private final OffsetDateTime startdate;
    private final Optional<OffsetDateTime> endDate;

    public DeploymentImpl(String operationName, OffsetDateTime startdate, Optional<OffsetDateTime> endDate) {
        this.operationName = operationName;
        this.startdate = startdate;
        this.endDate = endDate;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    @Override
    public OffsetDateTime getStartDate() {
        return startdate;
    }

    @Override
    public Optional<OffsetDateTime> getEndDate() {
        return endDate;
    }
}
