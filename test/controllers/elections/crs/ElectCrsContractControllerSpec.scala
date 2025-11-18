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
import forms.ElectCrsContractFormProvider
import models.{CRS, MessageSpecData, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ValidXMLPage
import pages.elections.crs.ElectCrsContractPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.elections.crs.ElectCrsContractView

import java.time.LocalDate
import scala.concurrent.Future

class ElectCrsContractControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ElectCrsContractFormProvider()
  val form         = formProvider()
  val fiNameFM     = "testFIFromFM"

  val messageSpecData = MessageSpecData(CRS, "testFI", "testRefId", "testReportingName", LocalDate.of(2000, 1, 1), giin = None, fiNameFM)

  lazy val electCrsContractRoute = controllers.elections.crs.routes.ElectCrsContractController.onPageLoad(NormalMode).url
  lazy val pageUnavailableUrl    = controllers.routes.PageUnavailableController.onPageLoad().url
  "ElectCrsContract Controller" - {

    "must return SEE_OTHER and the correct view for a GET When message spec data not available" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electCrsContractRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ElectCrsContractView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual pageUnavailableUrl
      }
    }

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(messageSpecData))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electCrsContractRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ElectCrsContractView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fiNameFM, form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .withPage(ValidXMLPage, getValidatedFileData(messageSpecData))
        .withPage(ElectCrsContractPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electCrsContractRoute)

        val view = application.injector.instanceOf[ElectCrsContractView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fiNameFM, form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .withPage(ValidXMLPage, getValidatedFileData(messageSpecData))
        .withPage(ElectCrsContractPage, true)

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, electCrsContractRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to page unavailable page when message spec data not available" in {

      val userAnswers = emptyUserAnswers
        .withPage(ElectCrsContractPage, true)

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, electCrsContractRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual pageUnavailableUrl
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .withPage(ValidXMLPage, getValidatedFileData(messageSpecData))
        .withPage(ElectCrsContractPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, electCrsContractRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ElectCrsContractView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(fiNameFM, boundForm, NormalMode)(request, messages(application)).toString
      }
    }
  }
}
