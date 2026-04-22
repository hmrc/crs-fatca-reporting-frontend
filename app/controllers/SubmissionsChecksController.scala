/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers

import controllers.actions.*
import models.fileDetails.{FileDetails, FileDetailsResult, FileValidationErrors}
import models.submission.ConversationId
import models.submission.fileDetails.*
import models.{CRS, CRSReportType}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FileDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.SubmissionChecksTableViewModel
import views.html.SubmissionsChecksView

import java.time.{LocalDate, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionsChecksController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  fileDetailsService: FileDetailsService,
  val controllerComponents: MessagesControllerComponents,
  view: SubmissionsChecksView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(page: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      fileDetailsService.getAllFileDetails(request.fatcaId, page) map {
        fileDetailsResult =>
          fileDetailsResult.fileDetailsList match {
            case Nil =>
              Redirect(controllers.routes.PageUnavailableController.onPageLoad().url)
            case _ =>
              Ok(view(SubmissionChecksTableViewModel(fileDetailsResult)))
          }
      }
  }

  def result = {
    val submittedTime  = LocalDateTime.of(2026, 1, 6, 12, 13, 54)
    val reportingDate  = LocalDate.of(2027, 1, 1)
    val conversationId = ConversationId("conversation-123")

    FileDetailsResult(
      Seq(
        FileDetails(
          _id = conversationId,
          enrolmentId = "XACBC0000123456",
          messageRefId = "GBXACBC12345678",
          reportingEntityName = Some("Test Entity"),
          status = models.submission.fileDetails.Accepted,
          name = "test-file.xml",
          submitted = submittedTime,
          lastUpdated = submittedTime,
          reportingPeriod = reportingDate,
          messageType = CRS,
          reportType = CRSReportType.TestData,
          isFiUser = true,
          fiNameFromFim = "Test FI Name",
          fiPrimaryContactEmail = Some("fiPrimary@email.com"),
          fiSecondaryContactEmail = Some("fiSecondary@email.com"),
          subscriptionPrimaryContactEmail = "test@email.com",
          subscriptionSecondaryContactEmail = Some("secondarySub@email.com")
        ),
        FileDetails(
          _id = conversationId,
          enrolmentId = "XACBC0000123456",
          messageRefId = "GBXACBC12345678",
          reportingEntityName = Some("Test Entity"),
          status = Pending,
          name = "test-file.xml",
          submitted = submittedTime,
          lastUpdated = submittedTime,
          reportingPeriod = reportingDate,
          messageType = CRS,
          reportType = CRSReportType.TestData,
          isFiUser = true,
          fiNameFromFim = "Test FI Name",
          fiPrimaryContactEmail = Some("fiPrimary@email.com"),
          fiSecondaryContactEmail = Some("fiSecondary@email.com"),
          subscriptionPrimaryContactEmail = "test@email.com",
          subscriptionSecondaryContactEmail = Some("secondarySub@email.com")
        ),
        FileDetails(
          _id = conversationId,
          enrolmentId = "XACBC0000123456",
          messageRefId = "GBXACBC12345678",
          reportingEntityName = Some("Test Entity"),
          status = RejectedSDES,
          name = "test-file.xml",
          submitted = submittedTime,
          lastUpdated = submittedTime,
          reportingPeriod = reportingDate,
          messageType = CRS,
          reportType = CRSReportType.TestData,
          isFiUser = true,
          fiNameFromFim = "Test FI Name",
          fiPrimaryContactEmail = Some("fiPrimary@email.com"),
          fiSecondaryContactEmail = Some("fiSecondary@email.com"),
          subscriptionPrimaryContactEmail = "test@email.com",
          subscriptionSecondaryContactEmail = Some("secondarySub@email.com")
        ),
        FileDetails(
          _id = conversationId,
          enrolmentId = "XACBC0000123456",
          messageRefId = "GBXACBC12345678",
          reportingEntityName = Some("Test Entity"),
          status = RejectedSDESVirus,
          name = "test-file.xml",
          submitted = submittedTime,
          lastUpdated = submittedTime,
          reportingPeriod = reportingDate,
          messageType = CRS,
          reportType = CRSReportType.TestData,
          isFiUser = true,
          fiNameFromFim = "Test FI Name",
          fiPrimaryContactEmail = Some("fiPrimary@email.com"),
          fiSecondaryContactEmail = Some("fiSecondary@email.com"),
          subscriptionPrimaryContactEmail = "test@email.com",
          subscriptionSecondaryContactEmail = Some("secondarySub@email.com")
        ),
        FileDetails(
          _id = conversationId,
          enrolmentId = "XACBC0000123456",
          messageRefId = "GBXACBC12345678",
          reportingEntityName = Some("Test Entity"),
          status = NotAccepted,
          name = "test-file.xml",
          submitted = submittedTime,
          lastUpdated = submittedTime,
          reportingPeriod = reportingDate,
          messageType = CRS,
          reportType = CRSReportType.TestData,
          isFiUser = true,
          fiNameFromFim = "Test FI Name",
          fiPrimaryContactEmail = Some("fiPrimary@email.com"),
          fiSecondaryContactEmail = Some("fiSecondary@email.com"),
          subscriptionPrimaryContactEmail = "test@email.com",
          subscriptionSecondaryContactEmail = Some("secondarySub@email.com")
        ),
        FileDetails(
          _id = conversationId,
          enrolmentId = "XACBC0000123456",
          messageRefId = "GBXACBC12345678",
          reportingEntityName = Some("Test Entity"),
          status = Rejected(FileValidationErrors(None, None)),
          name = "test-file.xml",
          submitted = submittedTime,
          lastUpdated = submittedTime,
          reportingPeriod = reportingDate,
          messageType = CRS,
          reportType = CRSReportType.TestData,
          isFiUser = true,
          fiNameFromFim = "Test FI Name",
          fiPrimaryContactEmail = Some("fiPrimary@email.com"),
          fiSecondaryContactEmail = Some("fiSecondary@email.com"),
          subscriptionPrimaryContactEmail = "test@email.com",
          subscriptionSecondaryContactEmail = Some("secondarySub@email.com")
        )
      ),
      totalSize = 2,
      pages = 1
    )
  }

}
