import sbt._

class ScargProject(info: ProjectInfo) extends DefaultProject(info) {

  val scalaToolsSnapshots = ScalaToolsSnapshots

  // compile settings
  override def compileOptions = super.compileOptions ++ Seq(Unchecked)

  // deploy/dist settings
  lazy val distPath = info.projectPath / "dist"
  def distName = "%s_%s-%s.zip".format(name, buildScalaVersion, version)

  // Dependencies
  object Dependencies {
    lazy val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
  }

  // subprojects
  lazy val core = project("core", "scarg-core", new CoreProject(_))
  lazy val examples = project("examples", "scarg-examples", new ExamplesProject(_), core)

  // scarg core subproject
  class CoreProject(info: ProjectInfo) extends ScoptDefaultProject(info, distPath) {
    val scalatest = Dependencies.scalatest
  }

  // examples subproject
  class ExamplesProject(info: ProjectInfo) extends ScoptDefaultProject(info, distPath)

  abstract class ScoptDefaultProject(info: ProjectInfo, val deployPath: Path) extends DefaultProject(info) {
    lazy val dist = deployTask(jarPath, packageDocsJar, packageSrcJar, deployPath, true, true, true) dependsOn(
      `package`, packageDocs, packageSrc) describedAs("Deploying")
    def deployTask(jar: Path, docs: Path, src: Path, toDir: Path,
                   genJar: Boolean, genDocs: Boolean, genSource: Boolean) = task {
      def gen(jar: Path, toDir: Path, flag: Boolean, msg: String): Option[String] =
      if (flag) {
        log.info(msg + " " + jar)
        FileUtilities.copyFile(jar, toDir / jar.name, log)
      } else None

      gen(jar, toDir, genJar, "Deploying bits") orElse
      gen(docs, toDir, genDocs, "Deploying docs") orElse
      gen(src, toDir, genSource, "Deploying sources")
    }
  }

}
