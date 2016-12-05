import scala.reflect.runtime.{universe => ru}
case class Person(name: String)
val m = ru.runtimeMirror(getClass().getClassLoader)
val classPerson = ru.typeOf[Person].typeSymbol.asClass
// classPerson is the class symbol
val cm = m.reflectClass(classPerson)


