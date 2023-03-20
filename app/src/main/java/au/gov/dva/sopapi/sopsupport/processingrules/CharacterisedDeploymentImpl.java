package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.sopsupport.components.MetadataKvpDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.OperationTypeCode;
import au.gov.dva.sopapi.interfaces.model.CharacterisedDeployment;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Deployment;

import java.time.LocalDate;
import java.util.Optional;

public class CharacterisedDeploymentImpl implements CharacterisedDeployment {
    private Deployment toDecorate;
    private OperationTypeCode[] operationTypeCodes;
    private MetadataKvpDto[] metadata;

    public CharacterisedDeploymentImpl(Deployment toDecorate, OperationTypeCode[] operationTypeCodes, MetadataKvpDto[] metadata) {
        this.toDecorate = toDecorate;


        this.operationTypeCodes = operationTypeCodes;
        this.metadata = metadata;
    }

    @Override
    public OperationTypeCode[] getOperationTypeCodes() {
        return operationTypeCodes;
    }

    @Override
    public MetadataKvpDto[] getMetadata() {
        return metadata;
    }

    @Override
    public String getOperationName() {
        return toDecorate.getOperationName();
    }

    @Override
    public String getEvent() {
        return toDecorate.getEvent();
    }

    @Override
    public LocalDate getStartDate() {
        return toDecorate.getStartDate();
    }

    @Override
    public Optional<LocalDate> getEndDate() {
        return toDecorate.getEndDate();
    }
}
