package au.gov.dva.sopapi.interfaces.model;


import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface Deployment extends HasDateRange {
  String getOperationName();
  String getEvent();


}
