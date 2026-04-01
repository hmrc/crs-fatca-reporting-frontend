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
import models.fileDetails.FileDetails
import models.submission.ConversationId
import models.submission.fileDetails.{Pending, Rejected}
import models.{CRS, CRSReportType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{ConversationIdPage, ValidXMLPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.FileDetailsService
import uk.gov.hmrc.http.HeaderCarrier
import utils.RulesErrorHelper
import views.html.RulesErrorView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

class RulesErrorControllerSpec extends SpecBase with RulesErrorHelper {
  val mockFileDetailsService = mock[FileDetailsService]
  val conversationId         = ConversationId("some-conversation-id")
  val submittedTime          = LocalDateTime.parse("2025-09-12T12:01:00")
  val reportingDate          = LocalDate.of(2026, 1, 1)

  val fileDetails = FileDetails(
    _id = conversationId,
    enrolmentId = "XACBC0000123456",
    messageRefId = "c-8-new-f-va",
    reportingEntityName = Some("Some-fi-name"),
    status = Rejected(validationErrors),
    name = "name.xml",
    submitted = submittedTime,
    lastUpdated = submittedTime,
    reportingPeriod = reportingDate,
    messageType = CRS,
    reportType = CRSReportType.NewInformation,
    isFiUser = true,
    fiNameFromFim = "Some-fi-name",
    fiPrimaryContactEmail = None,
    fiSecondaryContactEmail = None,
    subscriptionPrimaryContactEmail = "test@email.com",
    subscriptionSecondaryContactEmail = None
  )

  "RulesError Controller" - {

    "must return OK and the correct view for a GET" in {
      val messageSpecData = getMessageSpecData(CRS, CRSReportType.TestData)
      val userAnswers = emptyUserAnswers
        .withPage(ValidXMLPage, getValidatedFileData(messageSpecData))

      when(mockFileDetailsService.getFileDetails(any[ConversationId])(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Some(fileDetails)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileDetailsService].toInstance(mockFileDetailsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RulesErrorController.onPageLoad(conversationId.value).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RulesErrorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("testFile", "CRS", errorLength = 1200, createFileRejectedViewModel())(request, messages(application)).toString
      }
    }

    "must redirect to page unavailable when valid-xml is not present" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      when(mockFileDetailsService.getFileDetails(any[ConversationId])(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Some(fileDetails)))

      running(application) {
        val request = FakeRequest(GET, routes.RulesErrorController.onPageLoad(conversationId.value).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }

    "must redirect to page unavailable when file detail service returns a none" in {
      val messageSpecData = getMessageSpecData(CRS, CRSReportType.TestData)
      val userAnswers = emptyUserAnswers
        .withPage(ValidXMLPage, getValidatedFileData(messageSpecData))

      when(mockFileDetailsService.getFileDetails(any[ConversationId])(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileDetailsService].toInstance(mockFileDetailsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RulesErrorController.onPageLoad(conversationId.value).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }

    "must redirect to page unavailable when file detail service returns file details with status not rejected" in {
      val notRejectedFileDetails = fileDetails.copy(status = Pending)
      val messageSpecData        = getMessageSpecData(CRS, CRSReportType.TestData)
      val userAnswers = emptyUserAnswers
        .withPage(ValidXMLPage, getValidatedFileData(messageSpecData))

      when(mockFileDetailsService.getFileDetails(any[ConversationId])(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Some(notRejectedFileDetails)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileDetailsService].toInstance(mockFileDetailsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RulesErrorController.onPageLoad(conversationId.value).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }
  }

}
