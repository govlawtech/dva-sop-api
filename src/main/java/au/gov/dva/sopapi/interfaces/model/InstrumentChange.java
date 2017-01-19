package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.interfaces.JsonSerializable;
import au.gov.dva.sopapi.interfaces.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Function;

public interface InstrumentChange extends JsonSerializable {
     OffsetDateTime getDate();
     String getSourceInstrumentId();
     String getTargetInstrumentId();
     void apply(Repository repository, Function<String,Optional<SoP>> soPProvider);

}

