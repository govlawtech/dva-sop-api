package au.gov.dva.sopapi.sopsupport.vea;


import java.util.List;

public class OperationJsonResponse {

    private String operationName;
    private String startDate;
    private String endDate;
    private boolean isMrcaNonWarlike;
    private boolean isMrcaWarlike;
    private boolean isOperational;
    private boolean isPeacekeeping;
    private boolean isWarlike;
    private boolean isHazardous;

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isMrcaNonWarlike() {
        return isMrcaNonWarlike;
    }

    public void setMrcaNonWarlike(boolean mrcaNonWarlike) {
        isMrcaNonWarlike = mrcaNonWarlike;
    }

    public boolean isMrcaWarlike() {
        return isMrcaWarlike;
    }

    public void setMrcaWarlike(boolean mrcaWarlike) {
        isMrcaWarlike = mrcaWarlike;
    }

    public boolean isOperational() {
        return isOperational;
    }

    public void setOperational(boolean operational) {
        isOperational = operational;
    }

    public boolean isPeacekeeping() {
        return isPeacekeeping;
    }

    public void setPeacekeeping(boolean peacekeeping) {
        isPeacekeeping = peacekeeping;
    }

    public boolean isWarlike() {
        return isWarlike;
    }

    public void setWarlike(boolean warlike) {
        isWarlike = warlike;
    }

    public boolean isHazardous() {
        return isHazardous;
    }

    public void setHazardous(boolean hazardous) {
        isHazardous = hazardous;
    }

    @Override
    public String toString() {
        return "OperationJsonResponse{" +
                "operationName='" + operationName + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", isMrcaNonWarlike=" + isMrcaNonWarlike +
                ", isMrcaWarlike=" + isMrcaWarlike +
                ", isOperational=" + isOperational +
                ", isPeacekeeping=" + isPeacekeeping +
                ", isWarlike=" + isWarlike +
                ", isHazardous=" + isHazardous +
                '}';
    }
}

