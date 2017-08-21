package au.gov.dva.sopapi.sopref.parsing

import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers._
import au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories._
import au.gov.dva.sopapi.sopref.parsing.traits.{SoPClenser, SoPFactory}

object ServiceLocator {

  private val postAugust2015CompilationsOfPreAugust2015Sops = Set(
    "F2015C00914",
    "F2015C00915",
    "F2016C00252",
    "F2016C00253",
    "F2016C00269",
    "F2016C00270",
    "F2016C00274",
    "F2016C00279",
    "F2016C00276",
    "F2016C00280",
    "F2016C00973",
    "F2016C00974",
    "F2016C00975",
    "F2016C00976"
   )

  def isNewSopFormat(registerIdInfo: RegisterIdInfo): Boolean = {

    if (postAugust2015CompilationsOfPreAugust2015Sops.contains(registerIdInfo.registerId)) return false
    else if (registerIdInfo.isCompilation) {
      return registerIdInfo.year >= 2015
    }
    else return (registerIdInfo.year == 2015 && registerIdInfo.number > 660) || (registerIdInfo.year > 2015)
  }


  def findSoPFactory(registerId: String): SoPFactory = {
    registerId match {
      // any factories for specific sops by id go here
      case "F2010L02303" => BlephartisSoPFactory
      case "F2017L00007" => HepatitsBSoPFactoryForBoP
      case "F2017L00001" => HepatitsBSoPFactoryForRH
      case _ =>
        if (isNewSopFormat(SoPExtractorUtilities.unpackRegisterId(registerId))) PostAug2015SoPFactory
        else PreAug2015SoPFactory
    }
  }

  def findTextCleanser(registerId: String): SoPClenser = {
    registerId match {
      case "F2011C00491" => OsteoArthritisClenser
      case "F2017L00004" => HemorrhoidsClenser
      case "F2017L00005" => HemorrhoidsClenser
      case "F2015L00255" => PagetsDiseaseClenser
      case "F2013L00411" => EssentialThrombocythamiaClenser
      case "F2013L00412" => PrimaryMyelofibrosisClenser
      case "F2014L01833" => HaemophiliaClenser
      case "F2017C00198" => SuicideRHClenser
      case _ => if (isNewSopFormat(SoPExtractorUtilities.unpackRegisterId(registerId))) PostAug2015Clenser else GenericClenser
    }
  }


}
