package au.gov.dva.sopapi.interfaces.model;


import au.gov.dva.sopapi.dtos.MilitaryActivity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface Operation {
   String getName();
   ServiceType getServiceType();
   LocalDate getStartDate();
   Optional<LocalDate> getEndDate();

   default MilitaryActivity toMilitaryActivity(String legalSource) {
      return new MilitaryActivity(this.getName(),this.getStartDate(),this.getEndDate(),this.getServiceType().toMilitaryOperationType(), legalSource);
   }
}


