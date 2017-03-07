package au.gov.dva.sopapi.sopref.parsing

import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.{GenericCleanser, OsteoArthritisCleanser}
import au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories.{CartilageTearSoPFactory, LsSoPFactory, OsteoarthritisSoPFactory, PreAug2015SoPFactory}
import au.gov.dva.sopapi.sopref.parsing.traits.{SoPCleanser, SoPFactory}

object ServiceLocator {


  def findSoPFactory(registerId : String) : SoPFactory =
    {
      registerId match  {
        case "F2014L00933" => PreAug2015SoPFactory
        case "F2014L00930" => PreAug2015SoPFactory
        case "F2011C00491" => PreAug2015SoPFactory
        case "F2011C00492" => PreAug2015SoPFactory
        case "F2010L00553" => PreAug2015SoPFactory
        case "F2010L00554" => PreAug2015SoPFactory
        case "F2010L01040" => PreAug2015SoPFactory
        case "F2010L01041" => PreAug2015SoPFactory
        case "F2010L01666" => PreAug2015SoPFactory
        case "F2010L01667" => PreAug2015SoPFactory
        case "F2010L01668" => PreAug2015SoPFactory
        case "F2010L01669" => PreAug2015SoPFactory
        case "F2010L02850" => PreAug2015SoPFactory
        case "F2010L02851" => PreAug2015SoPFactory
        case "F2012L01789" => PreAug2015SoPFactory
        case "F2012L01790" => PreAug2015SoPFactory
        case _ => PreAug2015SoPFactory // todo switch between pre aug and post
      }
    }

  def findTextCleanser(registerId : String) : SoPCleanser = {
    registerId match {
      case "F2011C00491" => OsteoArthritisCleanser
      case _ => GenericCleanser
    }
  }

}
