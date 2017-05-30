package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import scala.collection.immutable

object LegalReferenceSequences {


  private def getMainParaLetterSequence = {
    val aToz = 'a' to 'z'
    val doubled = aToz.map(l => s"$l$l")
    val combined = aToz ++ doubled
    combined.map(i => "(" + i + ")")
  }

  val sequenceOfSmallLetters: List[String] = {

    val aToz = 'a' to 'z'

    def makeExcelStyle(letter : Char) = for (secondLetter <- aToz) yield s"$letter$secondLetter"

    def firstLittleLetterThenExcelStyle =  aToz  flatMap (c => c+: makeExcelStyle(c))

    def doubleLetterStyle = aToz map (c => s"$c$c")

    (firstLittleLetterThenExcelStyle ++ doubleLetterStyle) map ("(" + _ + ")") toList
  }

  private val numbersSequence: List[String] = (1 to 99) map ("(" + _ + ")") toList

  val smallRomanSequence: List[String] = (1 to 99).map(intToSmallRoman).map("(" + _ + ")").toList

  private def isNextLineInSequence(sequence: List[String])(lastLegalReference: Option[String], currentLegalReference: String): Boolean = {
    if (lastLegalReference.isEmpty) return true
    assert(lastLegalReference.get.startsWith("("))
    val indexOfLast = sequence.indexOf(lastLegalReference.get)
    assert(indexOfLast != -1)
    val subsequent = sequence.drop(indexOfLast + 1)
    val firstMatchingInRemainder = subsequent.find(_.startsWith(currentLegalReference))
    firstMatchingInRemainder.isDefined
  }

  def isNextLittleLetterPara(lastLegalRef: Option[String], currentLegalRef: String) : Boolean =  {
      isNextLineInSequence(sequenceOfSmallLetters)(lastLegalRef,currentLegalRef)
  }

  def isNextMainFactorLine(lastLegalRef: Option[String], currentLegalRef: String): Boolean = {
    isNextLineInSequence(numbersSequence)(lastLegalRef, currentLegalRef)
  }

  def isNextSubFactorLine(lastSubfactorRef: Option[String], currentSubFactorRef: String): Boolean = {
    isNextLineInSequence(smallRomanSequence)(lastSubfactorRef,currentSubFactorRef)

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
