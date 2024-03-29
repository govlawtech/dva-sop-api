package au.gov.dva.sopapi.sopref.parsing

import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers._
import au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories._
import au.gov.dva.sopapi.sopref.parsing.traits.{SoPClenser, SoPFactory}
import com.google.common.collect.ImmutableList

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
    "F2017C00768",
    "F2017C00770",
    "F2017C00072",
    "F2017C00073",
    "F2017C00872",
    "F2017C00871",
    "F2017C00748",
    "F2017C00746",
    "F2017C00751",
    "F2017C00799",
    "F2017C00791",
    "F2017C00817",
    "F2017C00802",
    "F2017C00775",
    "F2017C00796",
    "F2017C00789",
    "F2017C00806",
    "F2017C00785",
    "F2017C00759",
    "F2017C00758",
    "F2017C00823",
    "F2017C00756",
    "F2017C00750",
    "F2017C00815",
    "F2017C00753",
    "F2017C00770",
    "F2017C00772",
    "F2017C00830",
    "F2017C00828",
    "F2017C00767",
    "F2017C00761",
    "F2017C00764",
    "F2017C00809",
    "F2017C00779",
    "F2017C00836",
    "F2017C00774",
    "F2017C00771",
    "F2017C00855",
    "F2017C00792",
    "F2017C00786",
    "F2017C00832",
    "F2017C00822",
    "F2017C00783",
    "F2017C00873",
    "F2017C00800",
    "F2017C00749",
    "F2017C00747",
    "F2017C00752",
    "F2017C00798",
    "F2017C00788",
    "F2017C00818",
    "F2017C00801",
    "F2017C00797",
    "F2017C00793",
    "F2017C00804",
    "F2017C00787",
    "F2017C00760",
    "F2017C00824",
    "F2017C00757",
    "F2017C00754",
    "F2017C00813",
    "F2017C00755",
    "F2017C00768",
    "F2017C00777",
    "F2017C00839",
    "F2017C00829",
    "F2018C00329",
    "F2017C00784",
    "F2017C00763",
    "F2017C00769",
    "F2017C00811",
    "F2017C00782",
    "F2017C00803",
    "F2017C00835",
    "F2017C00795",
    "F2017C00776",
    "F2017C00773",
    "F2017C00856",
    "F2017C00794",
    "F2017C00790",
    "F2017C00827",
    "F2017C00819",
    "F2017C00778",
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
    "F2018C00655",
    "F2019C00221",
    "F2019C00222",
    "F2019C00584",
    "F2019C00583",
    "F2019C00544",
    "F2019C00538",
    "F2019C00669",
    "F2019C00787",
    "F2020C00213",
    "F2020C00856",
    "F2020C00857",
    "F2021C00081",
    "F2021C00080",
    "F2021C00459",
    "F2021C00460",
    "F2021C00904"

  )

  def isNewSopFormat(registerIdInfo: RegisterIdInfo): Boolean = {

    if (postAugust2015CompilationsOfPreAugust2015Sops.contains(registerIdInfo.registerId)) return false
    else if (registerIdInfo.isCompilation) {
      return registerIdInfo.year >= 2015
      }
    else return (registerIdInfo.year == 2015 && registerIdInfo.number > 660) || (registerIdInfo.year > 2015)
  }

  def getPreAug2015Factory() = PreAug2015SoPFactory

  def findSoPFactory(registerId: String): SoPFactory = {
    registerId match {
      // any factories for specific sops by id go here
      case "F2010L02303" => BlephartisSoPFactory
      case "F2017L00007" => HepatitsBSoPFactoryForBoP
      case "F2017L00001" => HepatitsBSoPFactoryForRH
      case "F2010L02304" => RenalStoneRHSoPFactory
      case "F2018L00010" => MalignantNeoplasmOfTheOverySoPFactory
      case "F2018L00011" => MalignantNeoplasmOfTheOverySoPFactory
      case "F2020C00856" => TrochanticBursitisSoPFactory
      case "F2020C00857" => TrochanticBursitisSoPFactory
      case "F2020C01031" => IDP_BopFactory
      case "F2021C00457" => GbSopFactory
      case "F2021C00456" => GbSopFactory
      case "F2021C00464" => IschaemicHdSopFactory
      case "F2021C00463" => IschaemicHdSopFactory
      case "F2021C00530" => NhlFactory
      case "F2021C00529" => NhlFactory
      case "F2021C00712" => new SubstituteCommencementDateFactory("27 July 2020")
      case "F2021C00711" => new SubstituteCommencementDateFactory("27 July 2020")
      case "F2021C00707" => new SubstituteCommencementDateFactory("28 January 2019")
      case "F2021C00710" => new SubstituteCommencementDateFactory("28 January 2019")
      case "F2021C00952" => new SubstituteCommencementDateFactory("20 September 2021")
      case "F2021C00904" => new SubstituteCommencementDateFactory("20 September 2021")
      case "F2022C00352" => new SubstituteCommencementDateFactory("25 July 2016")
      case "F2022C00351" => new SubstituteCommencementDateFactory("25 July 2016")
      case "F2022C00576" => new SubstituteCommencementDateFactory("27 July 2020")
      case "F2022C00578" => new SubstituteCommencementDateFactory("27 July 2020")
      case "F2022C00577" => new SubstituteCommencementDateFactory("21 June 2021")
      case "F2022C00580" => new SubstituteCommencementDateFactory("21 January 2019")
      case "F2022C00581" => new SubstituteCommencementDateFactory("21 January 2019")
      case "F2022C00588" => new SubstituteCommencementDateFactory("18 November 2018")
      case "F2022C00585" => new SubstituteCommencementDateFactory("4 December 2018")
      case "F2022C00582" => new SubstituteCommencementDateFactory("4 December 2018")
      case "F2022C00575" => new SubstituteCommencementDateFactory("5 April 2021")
      case "F2021C01296" => new SubstituteCommencementDateFactory("25 March 2019")
      case "F2022C00733" => new SubstituteCommencementDateFactory("25 July 2022")
      case "F2022C00732" => new SubstituteCommencementDateFactory("25 July 2022")
      case "F2022C00734" => new SubstituteCommencementDateFactory("25 July 2022")
      case "F2022C00686" => new SubstituteCommencementDateFactory("25 July 2022")
      case "F2022C00894" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00895" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00918" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00919" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00899" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00901" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00903" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00902" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00912" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00913" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00889" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00890" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00931" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00932" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00923" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00928" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00917" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00904" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00905" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00891" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00892" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00893" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00908" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00909" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00921" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00922" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00926" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00927" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00906" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00907" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00924" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00925" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00896" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00897" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00915" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00911" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00920" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00898" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00900" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C00914" => new SubstituteCommencementDateFactory("19 September 2022")
      case "F2022C01121" => new SubstituteCommencementDateFactory("21 November 2022")
      case "F2022C01119" => new SubstituteCommencementDateFactory("21 November 2022")
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
      case "F2017C00872" => ClenserToFixSuperscripts
      case "F2017C00871" => ClenserToFixSuperscripts
      case "F2017C00796" => ClenserToFixSuperscripts
      case "F2017C00759" => ClenserToFixSuperscripts
      case "F2017C00758" => ClenserToFixSuperscripts
      case "F2017C00750" => ClenserToFixSuperscripts
      case "F2017C00764" => ClenserToFixSuperscripts
      case "F2017C00832" => ClenserToFixSuperscripts
      case "F2017C00783" => ClenserToFixSuperscripts
      case "F2017C00800" => ClenserToFixSuperscripts
      case "F2017C00797" => ClenserToFixSuperscripts
      case "F2017C00760" => ClenserToFixSuperscripts
      case "F2017C00754" => ClenserToFixSuperscripts
      case "F2017C00827" => ClenserToFixSuperscripts
      case "F2014L00525" => ChronicMultisymptomIllnessBoPClenser
      case "F2017C00075" => SinusBarotraumaBoPClenser
      case "F2018C00189" => BaldDefinitionsSectionClenser
      case "F2018L00535" => ElectricalInjuryRHClenser
      case "F2018L01183" => AdrenalInsufficiencyBoPClenser
      case "F2019L00009" => HypopituitarismClenser
      case "F2019L00012" => HypopituitarismClenser
      case "F2019L00224" => BaldDefinitionsSectionClenser
      case "F2019L01098" => MortonMetatarsalgiaClenser
      case "F2019L01100" => MortonMetatarsalgiaClenser
      case "F2020C01031" => ClenserToRemoveEndNotesToc
      case "F2021C00710" => HypopituitarismClenser
      case "F2021C00707" => HypopituitarismClenser
      case "F2021C00952" => IHDClenser
      case "F2022C00352" => ContentsClenser
      case "F2022C00351" => ContentsClenser
      case "F2022C00576" => ContentsClenser
      case "F2022C00578" => ContentsClenser
      case "F2022C00579" => ContentsClenser
      case "F2022C00588" => ContentsClenser
      case "F2022C00587" => ContentsClenser
      case "F2022C00585" => ContentsClenser
      case "F2022C00582" => ContentsClenser
      case "F2022C00575" => ContentsClenser
      case "F2021C01296" => ContentsClenser
      case "F2022L00660" => LarynxNeoplasmTypoClenser
      case "F2022C00733" => ContentsClenser
      case "F2022C00732" => ContentsClenser
      case "F2022C00734" => ContentsClenser
      case "F2022C00686" => ContentsClenser
      case "F2022C00894" => ContentsClenser
      case "F2022C00895" => ContentsClenser
      case "F2022C00918" => ContentsClenser
      case "F2022C00919" => ContentsClenser
      case "F2022C00899" => ContentsClenser
      case "F2022C00901" => ContentsClenser
      case "F2022C00903" => ContentsClenser
      case "F2022C00902" => ContentsClenser
      case "F2022C00912" => ContentsClenser
      case "F2022C00913" => ContentsClenser
      case "F2022C00889" => ContentsClenser
      case "F2022C00890" => ContentsClenser
      case "F2022C00931" => ContentsClenser
      case "F2022C00932" => ContentsClenser
      case "F2022C00923" => ContentsClenser
      case "F2022C00928" => ContentsClenser
      case "F2022C00917" => ContentsClenser
      case "F2022C00904" => ContentsClenser
      case "F2022C00905" => ContentsClenser
      case "F2022C00891" => ContentsClenser
      case "F2022C00892" => ContentsClenser
      case "F2022C00893" => ContentsClenser
      case "F2022C00908" => ContentsClenser
      case "F2022C00909" => ContentsClenser
      case "F2022C00921" => ContentsClenser
      case "F2022C00922" => ContentsClenser
      case "F2022C00926" => ContentsClenser
      case "F2022C00927" => ContentsClenser
      case "F2022C00906" => ContentsClenser
      case "F2022C00907" => ContentsClenser
      case "F2022C00924" => ContentsClenser
      case "F2022C00925" => ContentsClenser
      case "F2022C00896" => ContentsClenser
      case "F2022C00897" => ContentsClenser
      case "F2022C00915" => ContentsClenser
      case "F2022C00911" => ContentsClenser
      case "F2022C00920" => ContentsClenser
      case "F2022C00898" => ContentsClenser
      case "F2022C00900" => ContentsClenser
      case "F2022C00914" => ContentsClenser
      case "F2022C01121" => ContentsClenser
      case "F2022C01119" => ContentsClenser

      case _ => if (isNewSopFormat(SoPExtractorUtilities.unpackRegisterId(registerId))) PostAug2015Clenser else GenericClenser
    }
  }


}
