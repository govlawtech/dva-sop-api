package au.gov.dva.sopapi.interfaces.model;

import java.util.Optional;

public interface FactorReference {
    String getMainFactorReference();
    Optional<String> getFactorPartReference();

}
