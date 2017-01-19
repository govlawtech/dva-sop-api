package au.gov.dva.sopapi.sopref.parsing.factories

import au.gov.dva.sopapi.sopref.parsing.implementations.{GenericCleanser, LsSoPFactory}
import au.gov.dva.sopapi.sopref.parsing.traits.{SoPCleanser, SoPFactory}

object ServiceLocator {

  def findSoPFactory(registerId : String) : SoPFactory =
    {
      registerId match  {
        case "F2014L00933" => LsSoPFactory
        case "F2014L00930" => LsSoPFactory
        case _ => null // todo: generic factory for unknown sops
      }
    }

  def findTextCleanser(registerId : String) : SoPCleanser = {
    registerId match {
      case _ => GenericCleanser
    }
  }

}
