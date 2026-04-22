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

import base.SpecBase
import models.fileDetails.{FileDetails, FileDetailsResult}
import models.submission.ConversationId
import models.{CRS, CRSReportType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.FileDetailsService
import viewmodels.SubmissionChecksTableViewModel
import views.html.SubmissionsChecksView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class SubmissionsChecksControllerSpec extends SpecBase {
  val mockFileDetailsService: FileDetailsService = mock[FileDetailsService]
  "SubmissionsChecks Controller" - {

    "must return OK and the correct view for a GET" in new TestContext {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[FileDetailsService].toInstance(mockFileDetailsService)
        )
        .build()

      when(mockFileDetailsService.getAllFileDetails(any[String](), any[Int]())(any(), any()))
        .thenReturn(Future.successful(fileDetailsResult))
      running(application) {
        val request = FakeRequest(GET, routes.SubmissionsChecksController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionsChecksView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(SubmissionChecksTableViewModel(fileDetailsResult))(request, messages(application)).toString
      }
    }

    "must redirect to Page Unavailable if no file details are found" in new TestContext {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[FileDetailsService].toInstance(mockFileDetailsService)
        )
        .build()

      when(mockFileDetailsService.getAllFileDetails(any[String](), any[Int]())(any(), any()))
        .thenReturn(Future.successful(FileDetailsResult(Seq.empty, totalSize = 0, pages = 0)))

      running(application) {
        val request = FakeRequest(GET, routes.SubmissionsChecksController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }
  }
}

trait TestContext {

  def fileDetailsResult = {
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
        )
      ),
      totalSize = 1,
      pages = 1
    )
  }
}
