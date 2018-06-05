package au.gov.dva.sopapi.veaops

case class NonWarlikeDetermination(override val registerId: String, override val operations: List[VeaOperation], override val activities: List[VeaActivity]) extends VeaDetermination(registerId, operations, activities) {

  override def toString: String = s"$registerId: non-warlike, ${operations.length} ops, ${activities.length} activities"
}
