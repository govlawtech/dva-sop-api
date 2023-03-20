package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.dtos.sopsupport.components.MetadataKvpDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.OperationTypeCode;

public interface CharacterisedDeployment extends Deployment {
    OperationTypeCode[] getOperationTypeCodes();

    default OperationTypeCode[] defaultGetOperationTypeCodes() {
        return new OperationTypeCode[0];
    }

    MetadataKvpDto[] getMetadata();

    default MetadataKvpDto[] defaultGetMetadata() {
        return new MetadataKvpDto[0];
    }
}
