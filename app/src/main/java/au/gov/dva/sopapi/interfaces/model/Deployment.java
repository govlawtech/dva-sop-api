package au.gov.dva.sopapi.interfaces.model;


public interface Deployment extends MaybeOpenEndedInterval {
  String getOperationName();
  String getEvent();


}
