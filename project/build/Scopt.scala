import sbt._

class ScoptProject(info: ProjectInfo) extends DefaultProject(info) {

  val scalaToolsSnapshots = ScalaToolsSnapshots

  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"

  override def compileOptions = super.compileOptions ++ Seq(Unchecked)

}
