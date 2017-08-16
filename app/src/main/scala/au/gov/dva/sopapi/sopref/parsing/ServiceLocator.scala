package au.gov.dva.sopapi.sopref.parsing

import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers._
import au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories._
import au.gov.dva.sopapi.sopref.parsing.traits.{SoPClenser, SoPFactory}

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
        case "F2016C00973" => PreAug2015SoPFactory
        case "F2016C00974" => PreAug2015SoPFactory
        case "F2016C00975" => PreAug2015SoPFactory
        case "F2016C00976" => PreAug2015SoPFactory
        case "F2010L02303" => BlephartisSoPFactory
        case _ =>
           if (isNewSopFormat(SoPExtractorUtilities.unpackRegisterId(registerId))) PostAug2015SoPFactory
          else PreAug2015SoPFactory

      }
    }

  def findTextCleanser(registerId : String) : SoPClenser = {
    registerId match {

      case "F2011C00491" => OsteoArthritisClenser
      case "F2017L00004" => HemorrhoidsClenser
      case "F2017L00005" => HemorrhoidsClenser
      case "F2015L00255" => PagetsDiseaseClenser
      case "F2013L00411" => EssentialThrombocythamiaClenser
      case "F2013L00412" => PrimaryMyelofibrosisClenser
      case "F2014L01833" => HaemophiliaClenser
      case _ => if (isNewSopFormat(SoPExtractorUtilities.unpackRegisterId(registerId))) PostAug2015Clenser else GenericClenser
    }
  }


}
