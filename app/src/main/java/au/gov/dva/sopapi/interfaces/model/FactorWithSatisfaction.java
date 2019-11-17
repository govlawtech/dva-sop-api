package au.gov.dva.sopapi.interfaces.model;

import au.gov.dva.sopapi.sopsupport.processingrules.FactorWithSatisfactionImpl;

import java.util.Optional;
import java.util.function.BiFunction;

public interface FactorWithSatisfaction  {
   Factor getFactor();
   Boolean isSatisfied();

   // arg order: sub part reference, factor text
   default String getApplicablePart() {
      return getFactor().getParagraph();
   }


}


