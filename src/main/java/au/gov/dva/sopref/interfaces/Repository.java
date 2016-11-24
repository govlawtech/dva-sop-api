package au.gov.dva.sopref.interfaces;

import au.gov.dva.sopref.interfaces.model.InstrumentChange;
import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.SoP;

import java.util.Optional;

public interface Repository {
     void saveSop(SoP sop);
     Optional<SoP> getSop(String registerId);
     Iterable<SoP> getAllSops();
     Iterable<InstrumentChange> getInstrumentChanges();
     void setOperations(Iterable<Operation> operations);
     Iterable<Operation> getOperations();

}
