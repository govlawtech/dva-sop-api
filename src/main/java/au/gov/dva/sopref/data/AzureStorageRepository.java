package au.gov.dva.sopref.data;

import au.gov.dva.sopref.interfaces.Repository;
import au.gov.dva.sopref.interfaces.model.InstrumentChange;
import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.SoP;
import com.fasterxml.jackson.databind.JsonNode;

public class AzureStorageRepository implements Repository {
    private static AzureStorageRepository ourInstance = new AzureStorageRepository();

    public static AzureStorageRepository getInstance() {
        return ourInstance;
    }

    private AzureStorageRepository() {
    }


    @Override
    public void saveSop(SoP sop) {

    }

    @Override
    public void getSop(String registerId) {

    }

    @Override
    public Iterable<SoP> getAllSops() {
        return null;
    }

    @Override
    public Iterable<InstrumentChange> getInstrumentChanges() {
        return null;
    }

    @Override
    public void setOperations(Iterable<Operation> operations) {

    }

    @Override
    public Iterable<Operation> getOperations() {
        return null;
    }
}
