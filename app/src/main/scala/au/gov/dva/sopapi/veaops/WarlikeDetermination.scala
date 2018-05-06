package au.gov.dva.sopapi.veaops

case class WarlikeDetermination(override val registerId: String, override val operations: List[VeaOperation], override val activities: List[VeaActivity]) extends VeaDetermination(registerId, operations, activities) {
  override def toString: String = s"$registerId: warlike, ${operations.length} ops, ${activities.length} activities"
}
