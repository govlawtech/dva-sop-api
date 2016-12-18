package au.gov.dva.sopapi.interfaces.model;


import java.time.OffsetDateTime;
import java.util.Optional;

public interface Deployment {
  String getOperationName();
  OffsetDateTime getStartDate();
  Optional<OffsetDateTime> getEndDate();
}
