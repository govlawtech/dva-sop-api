package au.gov.dva.sopref.interfaces;

import au.gov.dva.sopref.interfaces.model.InstrumentChange;
import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.SoP;

public interface Repository {
     void saveSop(SoP sop);
     SoP getSop(String registerId);
     Iterable<SoP> getAllSops();
     Iterable<InstrumentChange> getInstrumentChanges();
     void setOperations(Iterable<Operation> operations);
     Iterable<Operation> getOperations();

}
