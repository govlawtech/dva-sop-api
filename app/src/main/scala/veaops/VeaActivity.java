package veaops;

import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.util.Optional;

public interface VeaActivity {
    public LocalDate getStartDate();
    public Optional<LocalDate> getEndDate();
    public ImmutableSet<SpecifiedArea> getSpecifiedAreas();
    public ImmutableSet<Qualification> getQualifications();
}
