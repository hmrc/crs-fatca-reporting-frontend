import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

import scala.collection.Seq

lazy val appName: String = "crs-fatca-reporting-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.5"

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings) *)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._",
      "viewmodels.govuk.all._"
    ),
    PlayKeys.playDefaultPort := 10038,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*components.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*Routes.*;.*viewmodels.*;.*ViewUtils.*;.*GuiceInjector;" +
      ".*ControllerConfiguration;.*LanguageSwitchController",
    ScoverageKeys.coverageMinimumStmtTotal := 60, //todo : increase back to 78 when we have developed some pages
    ScoverageKeys.coverageFailOnMinimum    := true,
    ScoverageKeys.coverageHighlighting     := true,
    scalacOptions                          := scalacOptions.value.distinct,
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(
          Seq(
            "javascripts/app.js",
            "javascripts/jquery-3.6.0.min.js",
            "javascripts/upload-spinner.js",
            "javascripts/second-spinner.js"
          )
        )
    ),
    uglifyOps := UglifyOps.singleFile,
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions   := Seq("unused=false", "dead_code=false"),
    pipelineStages          := Seq(digest),
    Assets / pipelineStages := Seq(concat, uglify),
    uglify / includeFilter  := GlobFilter("application.js")
  )
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:msg=unused&src=html/.*:s"
    )
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  parallelExecution := false,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")
