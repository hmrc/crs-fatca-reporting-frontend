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
import controllers.actions.*
import forms.ChangeFileFormProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatest.PrivateMethodTester
import org.scalatestplus.mockito.MockitoSugar
import pages.{ReportElectionsPage, RequiredGiinPage}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.{Call, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ChangeFileView

import scala.concurrent.Future

class ChangeFileControllerSpec extends SpecBase with MockitoSugar with PrivateMethodTester {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new ChangeFileFormProvider()
  private val form         = formProvider()

  private lazy val changeFileRoute       = routes.ChangeFileController.onPageLoad().url
  private lazy val changeFileSubmitRoute = routes.ChangeFileController.onSubmit().url

  "ChangeFile Controller" - {
    "onPageLoad" - {
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
      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, changeFileRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
    "onSubmit" - {
      "must redirect to upload-file page when answer is Yes" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

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
      "must also clear UserAnswers when the user submits Yes" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

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

          val idCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
          verify(mockSessionRepository).clear(idCaptor.capture())
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
    "giinOrElectionsProvided" - {
      val controller = new ChangeFileController(
        mock[MessagesApi],
        mockSessionRepository,
        mock[IdentifierAction],
        mock[DataRetrievalAction],
        mock[DataRequiredAction],
        formProvider,
        mock[MessagesControllerComponents],
        mock[ChangeFileView]
      )(scala.concurrent.ExecutionContext.global)
      val giinOrElectionsProvided = PrivateMethod[Boolean](Symbol("giinOrElectionsProvided"))

      "must return true if RequiredGiinPage is defined" in {
        val userAnswers = emptyUserAnswers.withPage(RequiredGiinPage, "test-giin")
        val result      = controller invokePrivate giinOrElectionsProvided(userAnswers)
        result mustEqual true
      }
      "must return true if ReportElectionsPage is true" in {
        val userAnswers = emptyUserAnswers.withPage(ReportElectionsPage, true)
        val result      = controller invokePrivate giinOrElectionsProvided(userAnswers)
        result mustEqual true
      }
      "must return false if neither RequiredGiinPage nor ReportElectionsPage is defined" in {
        val userAnswers = emptyUserAnswers
        val result      = controller invokePrivate giinOrElectionsProvided(userAnswers)
        result mustEqual false
      }
      "must return false if RequiredGiinPage is not defined and ReportElectionsPage is false" in {
        val userAnswers = emptyUserAnswers.withPage(ReportElectionsPage, false)
        val result      = controller invokePrivate giinOrElectionsProvided(userAnswers)
        result mustEqual false
      }

    }
  }
}
