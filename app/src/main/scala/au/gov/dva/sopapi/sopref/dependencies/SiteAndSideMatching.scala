package au.gov.dva.sopapi.sopref.dependencies

object SiteAndSideMatching {



  def discountMatch(antecedentCondition: InstantCondition, sequelaCondition: InstantCondition): Boolean = {
    // osteo and fracture
    (antecedentCondition.getSide,antecedentCondition.getSide) match {
      case (Some(x),Some(y)) if x != y => true
      case _ => {
        (antecedentCondition.getIcdCode, sequelaCondition.getIcdCode) match {
          case (Some(a), Some(s)) => {
            ???
          }
          case _ => false
        }
      }
    }

    def icdCodesAreInDiffSites(icdCode1 : String, icdCode2: String) = {
        ???
    }

    ???

  }


}
