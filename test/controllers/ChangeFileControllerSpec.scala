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
import forms.ChangeFileFormProvider
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{ReportElectionsPage, RequiredGiinPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ChangeFileView

import scala.concurrent.Future

class ChangeFileControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new ChangeFileFormProvider()
  private val form         = formProvider()

  private lazy val changeFileRoute       = routes.ChangeFileController.onPageLoad().url
  private lazy val changeFileSubmitRoute = routes.ChangeFileController.onSubmit().url

  "ChangeFile Controller" - {

    "must return OK and the correct view for a GET when giin/elections provided" in {

      val ua = emptyUserAnswers
        .withPage(RequiredGiinPage, "test-giin")
        .withPage(ReportElectionsPage, true)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, changeFileRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeFileView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, true)(request, messages(application)).toString
      }
    }
    "must return OK and the correct view for a GET when giin/elections are not provided" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, changeFileRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeFileView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, false)(request, messages(application)).toString
      }
    }
    "must redirect to upload-file page when answer is Yes" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, changeFileSubmitRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
      }

    }

    "must reset UserAnswers when the user selects 'Yes'" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val existingAnswers = emptyUserAnswers
        .withPage(RequiredGiinPage, "test-giin")
        .withPage(ReportElectionsPage, true)

      val application = applicationBuilder(userAnswers = Some(existingAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.ChangeFileController.onSubmit().url)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        val savedAnswers = captor.getValue

        savedAnswers.get(RequiredGiinPage) must be(empty)
        savedAnswers.get(ReportElectionsPage) must be(empty)
      }
    }
    "must redirect to check-your-file-details page when answer is No" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, changeFileSubmitRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourFileDetailsController.onPageLoad().url
        verifyNoInteractions(mockSessionRepository)
      }
    }
    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, changeFileSubmitRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ChangeFileView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, false)(request, messages(application)).toString
      }
    }
    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, changeFileRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, changeFileSubmitRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
