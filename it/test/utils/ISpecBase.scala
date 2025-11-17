/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import generators.Generators
import models.{CRS, MessageSpecData, UserAnswers, ValidatedFileData}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.libs.ws.{WSClient, WSRequest}
import queries.Settable
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.LocalDate

trait ISpecBase extends GuiceOneServerPerSuite with DefaultPlayMongoRepositorySupport[UserAnswers] with ScalaFutures with WireMockHelper with Generators {

  val userAnswersId: String = "internalId"
  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  val repository: SessionRepository = app.injector.instanceOf[SessionRepository]
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  def config: Map[String, String] = Map(
    "microservice.services.upscan.port" -> WireMockConstants.stubPort.toString,
    "microservice.services.crs-fatca-reporting.port" -> WireMockConstants.stubPort.toString,
    "microservice.services.auth.host" -> WireMockConstants.stubHost,
    "microservice.services.auth.port" -> WireMockConstants.stubPort.toString,
    "mongodb.uri"                     -> mongoUri,
    "play.filters.csrf.header.bypassHeaders.Csrf-Token"       -> "nocheck"
    //    "logger.root"                                             -> "INFO",
    //    "logger.controllers"                                      -> "DEBUG"
  )

  def buildClient(path: String): WSRequest =
    app.injector.instanceOf[WSClient].url(s"http://localhost:$port/report-for-crs-and-fatca$path")

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(20, Seconds)))

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build()

  implicit class UserAnswersExtension(userAnswers: UserAnswers) {

    def withPage[T](page: Settable[T], value: T)(implicit writes: Writes[T]): UserAnswers =
      userAnswers.set(page, value).success.value

  }

  val testMessageSpecData: MessageSpecData = MessageSpecData(CRS, "testFI", "testRefId", "testReportingName", LocalDate.now(), giin = None, "testFiName")
  def getValidatedFileData(
                            msd: MessageSpecData = testMessageSpecData
                          ): ValidatedFileData = ValidatedFileData(fileName = "testFile", messageSpecData = msd, fileSize = 100L, checksum = "testCheckSum")

}