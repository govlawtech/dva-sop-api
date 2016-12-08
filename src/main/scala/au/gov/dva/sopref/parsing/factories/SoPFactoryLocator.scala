package au.gov.dva.sopref.parsing.factories

import au.gov.dva.sopref.parsing.implementations.LsSoPFactory
import au.gov.dva.sopref.parsing.traits.SoPFactory

object SoPFactoryLocator {

  def findFactory(registerId : String) : SoPFactory =
    {
      registerId match  {
        case "F2014L00933" => LsSoPFactory
        case _ => null // todo: generic factory for unknown sops
      }
    }
}
