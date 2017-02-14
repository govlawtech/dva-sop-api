package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.Optional;

public class ServiceImpl implements Service {

    private final ServiceBranch branch;
    private final EmploymentType employmentType;
    private final Rank rank;
    private final OffsetDateTime startDate;
    private final Optional<OffsetDateTime> endDate;
    private final ImmutableSet<Deployment> deployments;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ServiceImpl{");
        sb.append("branch=").append(branch);
        sb.append(", employmentType=").append(employmentType);
        sb.append(", rank=").append(rank);
        sb.append(", startDate=").append(startDate);
        sb.append(", endDate=").append(endDate);
        sb.append('}');
        return sb.toString();
    }

    public ServiceImpl(ServiceBranch branch, EmploymentType employmentType, Rank rank, OffsetDateTime startDate, Optional<OffsetDateTime> endDate, ImmutableSet<Deployment> deployments)
    {

        this.branch = branch;
        this.employmentType = employmentType;
        this.rank = rank;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deployments = deployments;
    }


    public ServiceBranch getBranch() {
        return branch;
    }

    @Override
    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    @Override
    public Rank getRank() {
        return rank;
    }

    @Override
    public OffsetDateTime getStartDate() {
        return startDate;
    }

    @Override
    public Optional<OffsetDateTime> getEndDate() {
        return endDate;
    }

    @Override
    public ImmutableSet<Deployment> getDeployments() {
        return deployments;
    }
}
