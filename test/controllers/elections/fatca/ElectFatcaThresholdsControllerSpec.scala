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

package controllers.elections.fatca

import base.SpecBase
import controllers.routes
import forms.elections.fatca.ElectFatcaThresholdsFormProvider
import models.{FATCA, FATCAReportType, NormalMode, UserAnswers, ValidatedFileData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ValidXMLPage
import pages.elections.fatca.ElectFatcaThresholdsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.elections.fatca.ElectFatcaThresholdsView

import scala.concurrent.Future

class ElectFatcaThresholdsControllerSpec extends SpecBase with MockitoSugar {

  val formProvider            = new ElectFatcaThresholdsFormProvider()
  val form: Form[Boolean]     = formProvider()
  val fiName                  = "fi-name"
  val reportingPeriodYear     = 2024
  val reportingPeriod: String = reportingPeriodYear.toString
  val fileName                = "test-file.xml"
  val FileSize                = 100L
  val FileChecksum            = "checksum"
  val expectedFiName          = "fi-name"

  lazy val electFatcaThresholdsRoute: String = controllers.elections.fatca.routes.ElectFatcaThresholdsController.onPageLoad(NormalMode).url
  lazy val pageUnavailableUrl: String        = controllers.routes.PageUnavailableController.onPageLoad().url

  val fatcaValidatedFileData        = getValidatedFileData(getMessageSpecData(FATCA, FATCAReportType.TestData, fiNameFromFim = fiName))
  val fatcaUserAnswers: UserAnswers = UserAnswers(userAnswersId).set(ValidXMLPage, fatcaValidatedFileData).success.value

  "ElectFatcaThresholds Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(fatcaUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electFatcaThresholdsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ElectFatcaThresholdsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fiName, form, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to page unavailable when valid xml page is not present in the user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electFatcaThresholdsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual pageUnavailableUrl
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for GET" in {

      val userAnswers = fatcaUserAnswers.set(ElectFatcaThresholdsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electFatcaThresholdsRoute)

        val view = application.injector.instanceOf[ElectFatcaThresholdsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fiName, form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(fatcaUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, electFatcaThresholdsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourFileDetailsController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for a submission" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, electFatcaThresholdsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual pageUnavailableUrl
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(fatcaUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, electFatcaThresholdsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ElectFatcaThresholdsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(fiName, boundForm, NormalMode)(request, messages(application)).toString
      }
    }
  }
}
