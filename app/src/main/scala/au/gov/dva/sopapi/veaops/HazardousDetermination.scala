package au.gov.dva.sopapi.veaops

case class HazardousDetermination(override val registerId: String, override val operations: List[VeaOperation], override val activities: List[VeaActivity]) extends VeaDetermination(registerId, operations, activities) {
  override def toString: String = s"$registerId: hazardous, ${operations.length} ops, ${activities.length} activities"
}
