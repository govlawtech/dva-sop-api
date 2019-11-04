package au.gov.dva.sopapi.sopref.dependencies



object HardCodedQualifications {

  case class Qualification(val forVeteran: String, val forDelegate: String)

//
//  def getHardCodedQualification(conditionName : String, registerId: String, para: String, instantCondition: InstantCondition)  =
//    (conditionName,registerId,para) match {
//      case ("osteoarthritis", _, _ ) => {
//        case (_, _, List("9(4)")) =>  Some( "Was there an intra-articular fracture in the same joint as the osteoarthritis ? (An 'intra-articular fracture' is a fracture involving the articular surface of the joint.)")
//        case (_, _, List("9(26)")) =>  Some( "Was there an intra-articular fracture in the same joint as the osteoarthritis ? (An 'intra-articular fracture' is a fracture involving the articular surface of the joint.)")
//
//        case _ => None
//      }
//      case ("lumbar spondylosis", _, List("6(d)", "6(s)")) => Some("Was there an intra-articular fracture of the lumbar spine? (An 'intra-articular fracture' is a fracture involving the articular surface of the joint.)")
//      case  _ => None
//
//    }


}
