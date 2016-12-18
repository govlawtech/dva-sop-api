package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.sopsupport.RequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.ResponseDto;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import com.google.common.collect.ImmutableSet;

public class SopSupport {
    public static ResponseDto applyRules(RequestDto requestDto, ImmutableSet<SoP> sops,ImmutableSet<ServiceDetermination> serviceDeterminations)
    {
        // make condition
        // run rule on condition
        // determine applicable sop factors
        return null;

    }
}
