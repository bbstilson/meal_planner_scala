import $ivy.`io.github.davidgregory084::mill-tpolecat:0.1.3`

import mill._, scalalib._
import io.github.davidgregory084.TpolecatModule

object meal_planner_scala extends TpolecatModule {
  def scalaVersion = "2.13.2"

  def ivyDeps = Agg(
    ivy"com.github.pureconfig::pureconfig:0.12.3",
    ivy"io.argonaut::argonaut:6.3.0",
    ivy"com.github.pathikrit::better-files:3.8.0",
    ivy"dev.zio::zio:1.0.0-RC19-2",
    ivy"com.softwaremill.sttp.client::core:2.1.4",
    ivy"io.github.neurodyne::zio-aws-s3:0.4.12"
  )

  object test extends Tests {

    def ivyDeps = Agg(
      ivy"org.scalactic::scalactic:3.1.1",
      ivy"org.scalatest::scalatest:3.1.1"
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}
