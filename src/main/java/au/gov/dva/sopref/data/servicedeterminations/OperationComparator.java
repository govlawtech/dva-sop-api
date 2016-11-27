package au.gov.dva.sopref.data.servicedeterminations;

import au.gov.dva.sopref.interfaces.model.Operation;

import java.io.Serializable;
import java.util.Comparator;

public class OperationComparator implements Comparator<Operation>, Serializable {

    static final long serialVersionUID = 42L;

    @Override
    public int compare(Operation o1, Operation o2) {
        if (o1.getEndDate().isPresent() && !o2.getEndDate().isPresent())
            return -1;

        if (!o1.getEndDate().isPresent() && o2.getEndDate().isPresent())
            return 1;

        return o1.getStartDate().compareTo(o1.getStartDate());

    }
}
