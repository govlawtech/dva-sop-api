package au.gov.dva.sopapi.interfaces.model;


import java.time.OffsetDateTime;
import java.util.Optional;

public interface Operation {
   String getName();
   ServiceType getServiceType();
   OffsetDateTime getStartDate();
   Optional<OffsetDateTime> getEndDate();
}


