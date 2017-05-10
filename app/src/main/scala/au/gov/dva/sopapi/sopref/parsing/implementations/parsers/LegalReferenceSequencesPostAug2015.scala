package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

object LegalReferenceSequencesPostAug2015 {
  private def getMainParaLetterSequence = {
    val aToz = 'a' to 'z'
    val doubled = aToz.map(l => s"$l$l")
    val combined = aToz ++ doubled
    combined.map(i => "(" + i + ")")
  }

  private def getSubParaLetterSequence = {
    List("i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x", "xi", "xii", "xiii")
      .map(i => ("(" + i + ")"))
  }

  private val numbersSequencce: List[String] = (1 to 99) map ("(" + _ + ")") toList

  private val subparaSequence: List[String] = (1 to 99).map(intToSmallRoman).map("(" + _ + ")").toList

  private def isNextLineInSequence(sequence: List[String])(lastLegalReference: Option[String], currentLegalReference: String): Boolean = {

    if (lastLegalReference.isEmpty) return true

    assert(lastLegalReference.get.startsWith("("))
    val indexOfLast = sequence.indexOf(lastLegalReference.get)
    assert(indexOfLast != -1)
    val subsequent = sequence.drop(indexOfLast + 1)
    val firstMatchingInRemainder = subsequent.find(_.startsWith(currentLegalReference))
    firstMatchingInRemainder.isDefined
  }

  def isNextMainFactorLine(lastLegalRef: Option[String], currentLegalRef: String) = {


    isNextLineInSequence(numbersSequencce)(lastLegalRef, currentLegalRef)
  }

  def isNextSubFactorLine(lastSubfactorRef: Option[String], currentSubFactorRef: String) = {
    isNextLineInSequence(subparaSequence)(lastSubfactorRef,currentSubFactorRef)

  }

  def intToSmallRoman(i: Int) : String = {
    val intToSmallRomans = Map(
      10 -> "x",
      9 -> "ix",
      5 -> "v",
      4 -> "iv",
      1 -> "i"
    )

    val keys = intToSmallRomans.keysIterator.toList.sortBy(i => -i)

    def derive(n : Int) : String = {

      keys.find(_ <= n) match {
        case Some(key) => {
          intToSmallRomans(key) + derive(n - key)
        }
        case None => ""
      }
    }

    derive(i)

  }

}
