package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.model.Deployment;

import java.time.OffsetDateTime;
import java.util.Optional;

public class DeploymentImpl implements Deployment {

    private final String operationName;
    private final OffsetDateTime startdate;
    private final Optional<OffsetDateTime> endDate;
    private final String event;

    public DeploymentImpl(String operationName, OffsetDateTime startdate, Optional<OffsetDateTime> endDate, String event) {
        this.operationName = operationName;
        this.startdate = startdate;
        this.endDate = endDate;
        this.event = event;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DeploymentImpl{");
        sb.append("operationName='").append(operationName).append('\'');
        sb.append(", startdate=").append(startdate);
        sb.append(", endDate=").append(endDate);
        sb.append(", event=").append(event);
        sb.append('}');
        return sb.toString();
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

    @Override
    public String getEvent() {
        return event;
    }
}
