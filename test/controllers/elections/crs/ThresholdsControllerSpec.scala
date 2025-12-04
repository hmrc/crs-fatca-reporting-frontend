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

package controllers.elections.crs

import base.SpecBase
import controllers.elections.crs.routes.*
import controllers.routes
import forms.elections.crs.ThresholdsFormProvider
import models.{CRS, MessageSpecData, Mode, NormalMode, UserAnswers, ValidatedFileData}
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ValidXMLPage
import pages.elections.crs.ThresholdsPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.elections.crs.ThresholdsView

import java.time.LocalDate
import scala.concurrent.Future

class ThresholdsControllerSpec extends SpecBase with MockitoSugar {

  val mockNavigator = mock[Navigator]
  val formProvider  = new ThresholdsFormProvider()
  val form          = formProvider()

  private val messageSpecData2025: MessageSpecData = getMessageSpecData(messageType = CRS, reportingPeriod = LocalDate.of(2025, 12, 31))
  val validatedFileData2025                        = getValidatedFileData(messageSpecData2025)

  val userAnswers2025: UserAnswers = emptyUserAnswers
    .set(ValidXMLPage, validatedFileData2025)
    .success
    .value

  private val messageSpecData2026: MessageSpecData = getMessageSpecData(messageType = CRS, reportingPeriod = LocalDate.of(2026, 1, 1))
  private val validatedFileData2026                = getValidatedFileData(messageSpecData2026)

  val userAnswers2026: UserAnswers = emptyUserAnswers
    .set(ValidXMLPage, validatedFileData2026)
    .success
    .value

  lazy val electionsCRSThresholdsRoute = controllers.elections.crs.routes.ThresholdsController.onPageLoad(NormalMode).url

  lazy val routeFor2025OrEarlier = controllers.routes.CheckYourFileDetailsController.onPageLoad().url
  lazy val routeFor2026OrLater   = ElectCrsCarfGrossProceedsController.onPageLoad(NormalMode).url

  "Thresholds Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers2025)).build()

      running(application) {
        val request = FakeRequest(GET, electionsCRSThresholdsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThresholdsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testFIName, form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswers2025.set(ThresholdsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electionsCRSThresholdsRoute)

        val view = application.injector.instanceOf[ThresholdsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testFIName, form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "on submit with valid data" - {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      "must redirect to CheckYourFileDetailsController when reporting period is 2025 or earlier" in {

        when(mockNavigator.nextPage(any(), any[Mode], any()))
          .thenReturn(routes.CheckYourFileDetailsController.onPageLoad())

        val application =
          applicationBuilder(userAnswers = Some(userAnswers2025))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[Navigator].toInstance(mockNavigator)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, electionsCRSThresholdsRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routeFor2025OrEarlier
        }
        reset(mockNavigator)
      }

      "must redirect to ElectCrsCarfGrossProceedsController when reporting period is 2026 or later" in {

        when(mockNavigator.nextPage(any(), any[Mode], any()))
          .thenReturn(ElectCrsCarfGrossProceedsController.onPageLoad(NormalMode))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers2026))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[Navigator].toInstance(mockNavigator)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, electionsCRSThresholdsRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routeFor2026OrLater
        }
        reset(mockNavigator)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers2025)).build()

      running(application) {
        val request =
          FakeRequest(POST, electionsCRSThresholdsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ThresholdsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(testFIName, boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, electionsCRSThresholdsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, electionsCRSThresholdsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Page Unavailable if ValidXMLPage data (containing message spec) is missing on GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electionsCRSThresholdsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PageUnavailableController.onPageLoad().url
      }
    }

    "must redirect to Page Unavailable if ValidXMLPage data (containing message spec) is missing on POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, electionsCRSThresholdsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PageUnavailableController.onPageLoad().url
      }
    }
  }
}
