package au.gov.dva.sopapi.sopref.parsing

import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.{GenericCleanser, HaemorrhoidsCleanser, OsteoArthritisCleanser, PostAug2015Cleanser}
import au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories._
import au.gov.dva.sopapi.sopref.parsing.traits.{SoPCleanser, SoPFactory}

object ServiceLocator {

  def isNewSopFormat(registerIdInfo: RegisterIdInfo) = {
    registerIdInfo match {
      case RegisterIdInfo(year, true, _) => year >= 2015
      case RegisterIdInfo(year, false, number) => (year == 2015 && number > 660) || (year > 2015)
    }
  }

  def findSoPFactory(registerId : String) : SoPFactory =
    {
      registerId match  {
        // any factories for specific sops by id go here
        case _ =>
           if (isNewSopFormat(SoPExtractorUtilities.unpackRegisterId(registerId))) PostAug2015SoPFactory
          else PreAug2015SoPFactory

      }
    }

  def findTextCleanser(registerId : String) : SoPCleanser = {
    registerId match {

      case "F2011C00491" => OsteoArthritisCleanser
      case "F2017L00004" => HaemorrhoidsCleanser
      case "F2017L00005" => HaemorrhoidsCleanser
      case _ => if (isNewSopFormat(SoPExtractorUtilities.unpackRegisterId(registerId))) PostAug2015Cleanser else GenericCleanser
    }
  }

}
