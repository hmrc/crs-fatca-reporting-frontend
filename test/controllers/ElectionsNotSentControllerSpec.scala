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
import models.CRSReportType.TestData
import models.submission.{ConversationId, GiinAndElectionDBStatus, SubmissionDetails}
import models.upscan.{Reference, UploadId}
import models.{CRS, ValidatedFileData}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import pages.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ElectionsNotSentView

import scala.concurrent.{ExecutionContext, Future}

class ElectionsNotSentControllerSpec extends SpecBase {

  private val mockSubmissionService = mock[SubmissionService]
  private val mockSessionRepository = mock[SessionRepository]

  "ElectionsNotSent Controller" - {
    "onPageLoad" - {
      val answers = emptyUserAnswers.withPage(GiinAndElectionStatusPage, GiinAndElectionDBStatus(true, true))
      "must return OK and the correct view for a GET when No GIIN sent" in {

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.ElectionsNotSentController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ElectionsNotSentView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(false)(request, messages(application)).toString
        }
      }
      "must return OK and the correct view for a GET when GIIN sent" in {

        val application = applicationBuilder(userAnswers = Some(answers.withPage(RequiredGiinPage, "98096B.00000.LE.350"))).build()

        running(application) {
          val request = FakeRequest(GET, routes.ElectionsNotSentController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ElectionsNotSentView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(true)(request, messages(application)).toString
        }
      }
      "must return REDIRECT and the correct view for a GET When no required data" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.withPage(RequiredGiinPage, "98096B.00000.LE.350"))).build()

        running(application) {
          val request = FakeRequest(GET, routes.ElectionsNotSentController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ElectionsNotSentView]

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "finishSendingFile" - {

      val fileData = getValidatedFileData(getMessageSpecData(CRS, TestData))
      val answersWithFile = emptyUserAnswers
        .withPage(ValidXMLPage, fileData)
        .withPage(URLPage, "file.url")
        .withPage(UploadIDPage, UploadId("uploadId"))
        .withPage(FileReferencePage, Reference("fileRef"))

      "must redirect submit file and redirect to StillCheckingYourFile" in {

        when(mockSubmissionService.submitDocument(any[SubmissionDetails]())(using any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(ConversationId("conversationId"))))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(answersWithFile))
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ElectionsNotSentController.finishSendingFile().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.StillCheckingYourFileController.onPageLoad().url

          verify(mockSessionRepository).set(any())
        }
      }

      "must return INTERNAL_SERVER_ERROR when submission fails" in {

        when(mockSubmissionService.submitDocument(any[SubmissionDetails]())(using any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(None))
        val application = applicationBuilder(userAnswers = Some(answersWithFile))
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ElectionsNotSentController.finishSendingFile().url)
          val result  = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "must return INTERNAL_SERVER_ERROR when data is missing" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ElectionsNotSentController.finishSendingFile().url)
          val result  = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
