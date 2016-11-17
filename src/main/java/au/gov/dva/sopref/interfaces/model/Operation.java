package au.gov.dva.sopref.interfaces.model;


import java.util.Date;

public interface Operation {
   String getName();
   Date getStartDate();
   Date getEndDate();
   OperationType getOperationType();
}


