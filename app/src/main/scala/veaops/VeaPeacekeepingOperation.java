package veaops;

import java.time.LocalDate;

public interface VeaPeacekeepingOperation {
    String getDescription();
    String getVeaReference();
    LocalDate getInitialDateAsPeaceKeepingForce();
}
