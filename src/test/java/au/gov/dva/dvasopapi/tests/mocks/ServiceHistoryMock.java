import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.Service;
import au.gov.dva.sopref.interfaces.model.ServiceHistory;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;

public class ServiceHistoryMock implements ServiceHistory {

    private LocalDate _enlistmentDate;
    private LocalDate _separationDate;
    private LocalDate _hireDate;
    private ImmutableSet<Service> _services;
    private ImmutableSet<Operation> _operations;

    public ServiceHistoryMock() {

    }

    public LocalDate getEnlistmentDate() {
        return _enlistmentDate;
    }

    public void setEnlistmentDate(LocalDate enlistmentDate) {
        _enlistmentDate = enlistmentDate;
    }

    public LocalDate getSeparationDate() {
        return _separationDate;
    }

    public void setSeparationDate(LocalDate separationDate) {
        _separationDate = separationDate;
    }

    public LocalDate getHireDate() {
        return _hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        _hireDate = hireDate;
    }

    public ImmutableSet<Service> getServices() {
        return _services;
    }

    public void setServices(ImmutableSet<Service> services) {
        _services = services;
    }

    public ImmutableSet<Operation> getOperations() {
        return _operations;
    }

    public void setOperations(ImmutableSet<Operation> operations) {
        _operations = operations;
    }
}
