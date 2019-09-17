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
    "F2016C00976",
    "F2017C00059",
    "F2017C00072",
    "F2017C00073",
    "F2017C00074",
    "F2017C00075",
    "F2017C00076",
    "F2018C00478",
    "F2018C00480",
    "F2018C00481",
    "F2018C00482",
    "F2018C00664",
    "F2018C00666",
    "F2018C00658",
    "F2018C00667",
    "F2018C00669",
    "F2018C00636",
    "F2018C00637",
    "F2018C00633",
    "F2018C00638",
    "F2018C00639",
    "F2018C00645",
    "F2018C00671",
    "F2018C00670",
    "F2018C00660",
    "F2018C00672",
    "F2018C00791",
    "F2019C00221",
    "F2019C00222",
    "F2019C00584",
    "F2019C00583",
    "F2019C00544",
    "F2019C00538",
    "F2019C00669"

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
      case "F2010L02304" => RenalStoneRHSoPFactory
      case "F2018L00010" => MalignantNeoplasmOfTheOverySoPFactory
      case "F2018L00011" => MalignantNeoplasmOfTheOverySoPFactory
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
      case "F2013L00411" => EssentialThrombocythamiaClenser
      case "F2013L00412" => PrimaryMyelofibrosisClenser
      case "F2014L01833" => HaemophiliaClenser
      case "F2017C00198" => BaldDefinitionsSectionClenser
      case "F2013L00720" => ClenserToFixSuperscripts
      case "F2013L00728" => ClenserToFixSuperscripts
      case "F2013L00722" => ClenserToFixSuperscripts
      case "F2013L00730" => ClenserToFixSuperscripts
      case "F2013L00731" => ClenserToFixSuperscripts
      case "F2013L00736" => ClenserToFixSuperscripts
      case "F2013L00737" => ClenserToFixSuperscripts
      case "F2013L01640" => ClenserToFixSuperscripts
      case "F2015L00657" => ClenserToFixSuperscripts
      case "F2015L00658" => ClenserToFixSuperscripts
      case "F2018C00645" => ClenserToFixSuperscripts
      case "F2014L00525" => ChronicMultisymptomIllnessBoPClenser
      case "F2017C00075" => SinusBarotraumaBoPClenser
      case "F2018C00189" => BaldDefinitionsSectionClenser
      case "F2018L00535" => ElectricalInjuryRHClenser
      case "F2018L01183" => AdrenalInsufficiencyBoPClenser
      case "F2019L00009" => HypopituitarismClenser
      case "F2019L00012" => HypopituitarismClenser
      case "F2019L00224" => BaldDefinitionsSectionClenser


      case _ => if (isNewSopFormat(SoPExtractorUtilities.unpackRegisterId(registerId))) PostAug2015Clenser else GenericClenser
    }
  }


}
