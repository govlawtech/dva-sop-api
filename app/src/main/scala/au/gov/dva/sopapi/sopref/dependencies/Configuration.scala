package au.gov.dva.sopapi.sopref.dependencies

object Configuration {

  def shouldAccept(dependentConditionName : String, targetConditionName: String) = {
    (dependentConditionName,targetConditionName) match  {
      case ("bruxism", "posttraumatic stress disorder") => true
      case ("bruxism", "depressive disorder") => true
      case ("bruxism", "anxiety disorder") => true
      case ("alcohol use disorder","posttraumatic stress disorder") => true
      case ("alcohol use disorder","depressive disorder") => true
      case ("alcohol use disorder","adjustment disorder") => true
      case ("alcohol use disorder","anxiety disorder") => true
      case ("alcohol use disorder","substance use disorder") => true
      case ("alcohol use disorder","panic disorder") => true

      case _ => false
    }
  }

}
