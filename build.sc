// format: off
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.2.0`
import $ivy.`io.github.bbstilson::mill-aws-lambda:0.2.2`

import mill._, scalalib._
import io.github.bbstilson.AwsLambdaModule
import io.github.davidgregory084.TpolecatModule

object meal_planner extends TpolecatModule with AwsLambdaModule {
  def scalaVersion = "3.0.0"

  def scalacOptions = super
    .scalacOptions()
    .filterNot(Set("-Xsource:3", "-migration"))

  def s3Bucket = "brandons-dev"
  def s3KeyPrefix = "meal-planner/suggest-counts"
  def lambdaName = "meal-planner"
  def lambdaHandler = "bbstilson.MealPlanner::run"
  def lambdaMemory = Some(512)
  def lambdaRoleArn = Some("arn:aws:lambda:us-west-2:968410040515:function:meal_planner")

  def ivyDeps = Agg(
    ivy"io.circe::circe-core:0.14.1",
    ivy"io.circe::circe-generic:0.14.1",
    ivy"io.circe::circe-parser:0.14.1",
    ivy"com.github.pureconfig::pureconfig-core:0.16.0",
    ivy"com.softwaremill.sttp.client3::core:3.3.6".withDottyCompat(scalaVersion()),
    ivy"com.softwaremill.sttp.client3::circe:3.3.6".withDottyCompat(scalaVersion()),
    ivy"com.github.pathikrit::better-files:3.9.1".withDottyCompat(scalaVersion()),
    ivy"software.amazon.awssdk:s3:2.16.79",
    ivy"software.amazon.awssdk:ses:2.16.79",
    ivy"com.amazonaws:aws-lambda-java-core:1.2.1",
    ivy"com.amazonaws:aws-lambda-java-events:3.9.0"
  )

  object test extends Tests with TestModule.Utest {

    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.7.10"
    )
  }
}
