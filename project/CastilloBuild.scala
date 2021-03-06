import com.typesafe.sbt.packager.universal.UniversalKeys
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.less.Import._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import play._
import sbt._
import Keys._

object CastilloBuild extends Build with UniversalKeys {

  val appName = "castillo"

  /**
   * We will use this setting to direct the output of the ScalaJS compiler to the resources within the Play module.
   */
  val scalajsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")

  /**
   * We will use this setting for the shared source directory
   */
  val sharedScalaDir = file(".") / "shared" / "main" / "scala"

  /**
   * Define the root project as an aggregation of the subprojects.
   */

  lazy val root =
    project.in(file("."))
    .settings(commonSettings:_*)
    .aggregate(server)

  /**
   * Define the server project as a Play application.
   */
  lazy val server =
    project.in(file("server"))
    .settings(serverSettings:_*)
    .enablePlugins(PlayScala) aggregate client

  /**
   * Define the client project as a ScalaJS application.
   */
  lazy val client =
    project.in(file("client"))
    .enablePlugins(ScalaJSPlugin)
    .settings(clientSettings:_*)

  /**
   * The settings for the server module
   */
  lazy val serverSettings = commonSettings ++ Seq(
    name := s"$appName-server",
    scalajsOutputDir := (classDirectory in Compile).value / "public" / "javascripts",
    compile in Compile <<= (compile in Compile) dependsOn (fastOptJS in (client, Compile)) dependsOn copySourceMapsTask,
    dist <<= dist dependsOn (fullOptJS in (client, Compile)),
    stage <<= stage dependsOn (fullOptJS in (client, Compile)),
    libraryDependencies ++= Dependencies.serverDeps.value,
    includeFilter in (Assets, LessKeys.less) := "__main.less"
  ) ++ (
    // ask scalajs project to put its outputs in scalajsOutputDir
    Seq(fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in (client, Compile, packageJSKey) := scalajsOutputDir.value
    }
  ) ++ sharedDirSettings

  /**
   * The settings for the client module (ScalaJS)
   */
  lazy val clientSettings = commonSettings ++ Seq(
    name := s"$appName-client",
    libraryDependencies ++= Dependencies.clientDeps.value,
    jsDependencies += RuntimeDOM % "test"
  ) ++ sharedDirSettings

  /**
   * Here we collect the settings for the shared source directories.
   */
  lazy val sharedDirSettings = Seq(
    unmanagedSourceDirectories in Compile += baseDirectory.value / "shared" / "main" / "scala"
  )

  /**
   * The setting that will be applied to all sub projects
   */
  lazy val commonSettings = Seq(
    organization := "de.woq",
    version := Versions.app,
    name := "castillo",
    scalaVersion := Versions.scala
  )

  /**
   * We use this task to copy the source maps into the Play application
   */
  val copySourceMapsTask = Def.task {
    val scalaFiles = (Seq(sharedScalaDir, client.base) ** "*.scala").get
    for (scalaFile <- scalaFiles) {
      val target = new File((classDirectory in Compile).value, scalaFile.getPath)
      IO.copyFile(scalaFile, target)
    }
  }

  /**
   * A simple container object for the dependencies.
   */
  object Dependencies {

    lazy val serverDeps = Def.setting(Seq(
      "com.newrelic.agent.java" % "newrelic-agent" % Versions.newRelic,
      "org.webjars" % "bootstrap" % Versions.bootstrap,
      "org.webjars" % "react" % Versions.react,
      "org.scalatestplus" %% "play" % Versions.scalaTestPlus % "test"
    ))
    
    lazy val clientDeps = Def.setting(Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % Versions.scalajsReact,
      "com.github.japgolly.scalajs-react" %%% "test" % Versions.scalajsReact % "test",
      "com.github.japgolly.scalajs-react" %%% "ext-scalaz71" % Versions.scalajsReact,
      "com.lihaoyi" %%% "upickle" % Versions.upickle,
      "org.scala-js" %%% "scalajs-dom" % Versions.scalajsDom,
      "com.lihaoyi" %%% "scalatags" % Versions.scalaTags,
      "be.doeraene" %%% "scalajs-jquery" % Versions.scalajsJQuery
    ))
  }
}
