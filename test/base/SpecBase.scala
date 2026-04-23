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

package base

import controllers.actions.*
import generators.Generators
import models.fileDetails.{FileDetails, FileValidationErrors}
import models.submission.{ConversationId, GiinAndElectionDBStatus}
import models.submission.fileDetails.{FileStatus, Pending}
import models.{CRS, CRSReportType, FATCA, FATCAReportType, MessageSpecData, MessageType, ReportType, UserAnswers, ValidatedFileData}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.test.{FakeRequest, Injecting}
import queries.Settable
import repositories.SessionRepository

import java.time.{LocalDate, LocalDateTime}

trait SpecBase
    extends AnyFreeSpec
    with MockitoSugar
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with Generators
    with Injecting {

  val userAnswersId: String = "FATCAID"
  val testFIName: String    = "fi-name"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def getValidatedFileData(
    msd: MessageSpecData = getMessageSpecData(FATCA, FATCAReportType.TestData)
  ): ValidatedFileData = ValidatedFileData(fileName = "testFile", messageSpecData = msd, fileSize = 100L, checksum = "testCheckSum")

  def getMessageSpecData(messageType: MessageType,
                         reportType: ReportType,
                         sendingCompanyIN: String = "testFI",
                         messageRefId: String = "testRefId",
                         reportingFIName: String = "testReportingName",
                         reportingPeriod: LocalDate = LocalDate.now(), // LocalDate.of(2000, 1, 1)
                         giin: Option[String] = None,
                         fiNameFromFim: String = "fi-name",
                         electionsRequired: Boolean = true,
                         isFiUser: Boolean = true
  ): MessageSpecData =
    MessageSpecData(
      messageType,
      reportType,
      sendingCompanyIN,
      messageRefId,
      Some(reportingFIName),
      reportingPeriod,
      giin,
      fiNameFromFim,
      electionsRequired,
      isFiUser,
      subscriptionPrimaryContactEmail = "test@email.com"
    )

  def getTestFileDetails(
    conversationId: ConversationId = ConversationId("test-conversation-id"),
    enrolmentId: String = "someenrolmentId",
    messageRefId: String = "somemessageRefId",
    reportingEntityName: Option[String] = Some("Test Entity"),
    status: FileStatus = Pending,
    name: String = "test-file.xml",
    submitted: LocalDateTime = LocalDateTime.of(2026, 1, 6, 12, 0, 0),
    lastUpdated: LocalDateTime = LocalDateTime.of(2026, 1, 6, 12, 0, 0),
    reportingPeriod: LocalDate = LocalDate.of(2026, 1, 1),
    messageType: MessageType = CRS,
    reportType: ReportType = CRSReportType.NewInformation,
    fiNameFromFim: String = "fi-name",
    isFiUser: Boolean = true,
    fiPrimaryContactEmail: Option[String] = None,
    fiSecondaryContactEmail: Option[String] = None,
    subscriptionPrimaryContactEmail: String = "test@email.com",
    subscriptionSecondaryContactEmail: Option[String] = None,
    errors: Option[FileValidationErrors] = None,
    giinAndElectionDBStatus: Option[GiinAndElectionDBStatus] = None
  ): FileDetails =
    FileDetails(
      _id = conversationId,
      enrolmentId = enrolmentId,
      messageRefId = messageRefId,
      reportingEntityName = reportingEntityName,
      status = status,
      name = name,
      submitted = submitted,
      lastUpdated = lastUpdated,
      reportingPeriod = reportingPeriod,
      messageType = messageType,
      reportType = reportType,
      fiNameFromFim = fiNameFromFim,
      isFiUser = isFiUser,
      fiPrimaryContactEmail = fiPrimaryContactEmail,
      fiSecondaryContactEmail = fiSecondaryContactEmail,
      subscriptionPrimaryContactEmail = subscriptionPrimaryContactEmail,
      subscriptionSecondaryContactEmail = subscriptionSecondaryContactEmail,
      errors = errors,
      giinAndElectionDBStatus = giinAndElectionDBStatus
    )

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  final val mockSessionRepository: SessionRepository = mock[SessionRepository]

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )

  implicit class UserAnswersExtension(userAnswers: UserAnswers) {

    def withPage[T](page: Settable[T], value: T)(implicit writes: Writes[T]): UserAnswers =
      userAnswers.set(page, value).success.value

  }
}
