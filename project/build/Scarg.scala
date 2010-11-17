import sbt._

class ScargProject(info: ProjectInfo) extends DefaultProject(info) {

  val scalaToolsSnapshots = ScalaToolsSnapshots

  // compile settings
  override def compileOptions = super.compileOptions ++ Seq(Unchecked)

  // publish settings ("publish" to local gh-pages checkout)
  override def managedStyle = ManagedStyle.Maven
  lazy val publishTo = Resolver.file("github-pages-repo", new java.io.File("/home/fire/work/src/private/scala/scarg-gh-pages/maven-repo"))

  // subprojects
  lazy val core = project("core", "scarg-core", new CoreProject(_))
  lazy val examples = project("examples", "scarg-examples", new ExamplesProject(_), core)

  // scarg core subproject
  class CoreProject(info: ProjectInfo) extends BaseProject(info) {
    lazy val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
  }

  // examples subproject
  class ExamplesProject(info: ProjectInfo) extends BaseProject(info)

  // base project which defines docs and sources packages
  class BaseProject(info: ProjectInfo) extends DefaultProject(info) {
    lazy val sourceArtifact = Artifact(this.artifactID, "source", "jar", Some("sources"), Nil, None)
    lazy val docsArtifact = Artifact(this.artifactID, "doc", "jar", Some("docs"), Nil, None)
    override def packageDocsJar = this.defaultJarPath("-docs.jar")
    override def packageSrcJar  = this.defaultJarPath("-sources.jar")
    override def packageToPublishActions = super.packageToPublishActions ++ Seq(this.packageDocs, this.packageSrc)
  }
}
