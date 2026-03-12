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

package controllers

import base.SpecBase
import models.CRSReportType.NewInformation
import models.fileDetails.FileDetails
import models.submission.*
import models.submission.fileDetails.Accepted
import models.{submission, CRS, CRSReportType, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.*
import pages.{GiinAndElectionStatusPage, ValidXMLPage}
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.FileDetailsService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

class FileConfirmationControllerSpec extends SpecBase {

  val messageSpecData = getMessageSpecData(CRS, fiNameFromFim = "Some-fi-name", reportType = NewInformation)

  val ua: UserAnswers = emptyUserAnswers
    .withPage(ValidXMLPage, getValidatedFileData(messageSpecData))
    .withPage(GiinAndElectionStatusPage, GiinAndElectionDBStatus(giinStatus = true, electionStatus = false))
  val mockFileDetailsService: FileDetailsService = mock[FileDetailsService]
  val submittedTime                              = LocalDateTime.parse("2025-09-12T12:01:00")
  val reportingDate                              = LocalDate.of(2026, 1, 1)
  val conversationId                             = ConversationId("conversation-123")

  "FileConfirmation Controller" - {
    val fileDetails = FileDetails(
      _id = conversationId,
      enrolmentId = "XACBC0000123456",
      messageRefId = "c-8-new-f-va",
      reportingEntityName = Some("EFG Bank plc"),
      status = Accepted,
      name = "name.xml",
      submitted = submittedTime,
      lastUpdated = submittedTime,
      reportingPeriod = reportingDate,
      messageType = CRS,
      reportType = CRSReportType.NewInformation,
      isFiUser = true,
      fiNameFromFim = "EFG Bank plc",
      fiPrimaryContactEmail = None,
      fiSecondaryContactEmail = None,
      subscriptionPrimaryContactEmail = "test@email.com",
      subscriptionSecondaryContactEmail = None
    )

    "must return OK and the correct view for a GET" in {
      when(mockFileDetailsService.getFileDetails(any[ConversationId])(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Some(fileDetails)))

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[FileDetailsService].toInstance(mockFileDetailsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.FileConfirmationController.onPageLoad(conversationId.value).url)

        val result = route(application, request).value

        status(result) mustEqual OK
        val resultHtml = contentAsString(result)
        resultHtml must include("c-8-new-f-va")
        resultHtml must include("CRS")
        resultHtml must include("EFG Bank plc")
        resultHtml must include("New information")
        resultHtml must include("12 September 2025")
        resultHtml must include("12:01pm")
        resultHtml must include("test@email.com")
        resultHtml must include("make any elections for EFG Bank plc in the service")
      }
    }

    "must redirect to page unavailable when invalid conversation id is used" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.FileConfirmationController.onPageLoad(conversationId.value).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }
  }
}
