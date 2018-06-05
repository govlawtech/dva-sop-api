package au.gov.dva.sopapi.exceptions;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;

public class ActDeterminationServiceException extends DvaSopApiDtoRuntimeException {
    public ActDeterminationServiceException(String msg, Throwable e) {
        super(msg, e);
    }

    public ActDeterminationServiceException(String msg) {
        super(msg);
    }

    public ActDeterminationServiceException(Throwable e) {
        super(e);
    }
}
