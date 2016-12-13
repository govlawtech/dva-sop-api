package au.gov.dva.sopref.interfaces.model;

import java.time.LocalDate;
import java.util.Optional;

public interface Condition {

    SoP getSoP();

    LocalDate getOnsetStartDate();
    Optional<LocalDate> getOnsetEndDate();

}


