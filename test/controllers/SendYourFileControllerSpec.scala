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
import models.FATCAReportType.TestData
import models.requests.DataRequest
import models.submission.*
import models.{CRS, CRSReportType, FATCA, FATCAReportType, SendYourFileAdditionalText, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import pages.{ConversationIdPage, GiinAndElectionStatusPage, ReportElectionsPage, ValidXMLPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.SendYourFileView

import scala.concurrent.{ExecutionContext, Future}

class SendYourFileControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockSubmissionService: SubmissionService = mock[SubmissionService]
  lazy val pageUnavailableUrl: String          = controllers.routes.PageUnavailableController.onPageLoad().url
  lazy val sendYourFileUrl: String             = routes.SendYourFileController.onPageLoad().url
  val hardcodedFiName                          = "testFiName"
  val exampleGiin                              = "8Q298C.00000.LE.340"
  val conversationId: ConversationId           = ConversationId("conversationId")

  val ua: UserAnswers =
    emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(getMessageSpecData(FATCA, fiNameFromFim = hardcodedFiName, reportType = TestData)))

  "SendYourFile Controller" - {

    "onPageLoad" - {
      "must return OK and the correct view for a GET for CRS and election not required" in {
        val uaa: UserAnswers = emptyUserAnswers
          .withPage(
            ValidXMLPage,
            getValidatedFileData(
              getMessageSpecData(electionsRequired = false, messageType = CRS, fiNameFromFim = hardcodedFiName, reportType = CRSReportType.TestData)
            )
          )
          .withPage(ReportElectionsPage, false)

        val application = applicationBuilder(userAnswers = Some(uaa)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(SendYourFileAdditionalText.NONE)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET for FATCA and election not required and giin present" in {
        val uaa: UserAnswers = emptyUserAnswers
          .withPage(
            ValidXMLPage,
            getValidatedFileData(
              getMessageSpecData(giin = Some("some-giin"),
                                 electionsRequired = false,
                                 messageType = FATCA,
                                 fiNameFromFim = hardcodedFiName,
                                 reportType = FATCAReportType.TestData
              )
            )
          )
          .withPage(ReportElectionsPage, false)

        val application = applicationBuilder(userAnswers = Some(uaa)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(SendYourFileAdditionalText.NONE)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET for CRS and election required" in {
        val uaa: UserAnswers = emptyUserAnswers
          .withPage(ValidXMLPage,
                    getValidatedFileData(getMessageSpecData(messageType = CRS, fiNameFromFim = hardcodedFiName, reportType = CRSReportType.TestData))
          )
          .withPage(ReportElectionsPage, true)

        val application = applicationBuilder(userAnswers = Some(uaa)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(SendYourFileAdditionalText.ELECTIONS)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET for FATCA and election required and giin present" in {
        val uaa: UserAnswers = emptyUserAnswers
          .withPage(
            ValidXMLPage,
            getValidatedFileData(
              getMessageSpecData(giin = Some("some-giin"), messageType = FATCA, fiNameFromFim = hardcodedFiName, reportType = FATCAReportType.TestData)
            )
          )
          .withPage(ReportElectionsPage, true)

        val application = applicationBuilder(userAnswers = Some(uaa)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(SendYourFileAdditionalText.ELECTIONS)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET for FATCA and election required and giin needs to be updated" in {
        val uaa: UserAnswers = emptyUserAnswers
          .withPage(
            ValidXMLPage,
            getValidatedFileData(getMessageSpecData(giin = None, messageType = FATCA, fiNameFromFim = hardcodedFiName, reportType = FATCAReportType.TestData))
          )
          .withPage(ReportElectionsPage, true)

        val application = applicationBuilder(userAnswers = Some(uaa)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(SendYourFileAdditionalText.BOTH)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET for FATCA and election not required and giin needs to be updated" in {
        val uaa: UserAnswers = emptyUserAnswers
          .withPage(
            ValidXMLPage,
            getValidatedFileData(getMessageSpecData(giin = None, messageType = FATCA, fiNameFromFim = hardcodedFiName, reportType = FATCAReportType.TestData))
          )
          .withPage(ReportElectionsPage, false)

        val application = applicationBuilder(userAnswers = Some(uaa)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(SendYourFileAdditionalText.GIIN)(request, messages(application)).toString
        }
      }

      "must redirect to page unavailable when xml valid page is not present in user answers for a GET" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileUrl)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual pageUnavailableUrl
        }
      }
    }

    "onSubmit" - {
      "must redirect to PageUnavailableController when validXmlPage is missing" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, routes.SendYourFileController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad().url
        }
      }

      "must redirect to giin not sent when the giin update fails" in {
        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService)
          )
          .build()

        when(mockSubmissionService.submitElectionsAndGiin(any[UserAnswers])(using any[DataRequest[_]], any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(GiinUpdateFailed(false, true)))

        running(application) {
          val request = FakeRequest(POST, routes.SendYourFileController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.GiinNotSentController.onPageLoad().url
        }
      }

      "must redirect to election  not sent when the election submission fails" in {
        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService)
          )
          .build()

        when(mockSubmissionService.submitElectionsAndGiin(any[UserAnswers])(using any[DataRequest[_]], any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(ElectionsSubmitFailed(true, false)))

        running(application) {
          val request = FakeRequest(POST, routes.SendYourFileController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ElectionsNotSentController.onPageLoad().url
        }
      }

      // This test will be changed during the implementation of DAC6-3829
      "must redirect to StillCheckingYourFileController for a successful submission" in {
        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService)
          )
          .build()

        when(mockSubmissionService.submitElectionsAndGiin(any[UserAnswers])(using any[DataRequest[_]], any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(GiinAndElectionSubmittedSuccessful))

        running(application) {
          val request = FakeRequest(POST, routes.SendYourFileController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.StillCheckingYourFileController.onPageLoad().url
        }
      }
    }

    "getStatus" - {
      // This test title and content will be changed during the implementation of DAC6-3829
      "must return OK and load the page Still checking page" in {

        val userAnswers = UserAnswers("Id")
          .withPage(ConversationIdPage, conversationId)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsJson(result).toString mustEqual "{\"url\":\"/report-for-crs-and-fatca/report/still-checking-your-file\"}"
        }
      }

      "must return json pointing to giin not sent to url when missing a conversation Id and contains GiinAndElectionStatusPage with value has giinstatus false" in {
        val giinAndElectionStatus = GiinAndElectionDBStatus(false, true)

        val userAnswers = UserAnswers("Id")
          .withPage(GiinAndElectionStatusPage, giinAndElectionStatus)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsJson(result).toString mustEqual "{\"url\":\"/report-for-crs-and-fatca/report/problem/giin-not-sent\"}"
        }
      }

      "must return json pointing to election not sent to url when missing a conversation Id and contains GiinAndElectionStatusPage with value has giinstatus true " +
        "and election status false" in {
          val giinAndElectionStatus = GiinAndElectionDBStatus(true, false)

          val userAnswers = UserAnswers("Id")
            .withPage(GiinAndElectionStatusPage, giinAndElectionStatus)

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsJson(result).toString mustEqual "{\"url\":\"/report-for-crs-and-fatca/report/problem/elections-not-sent\"}"
          }
        }

      "must return internal server error when conversation Id is missing" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
