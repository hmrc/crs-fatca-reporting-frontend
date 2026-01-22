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
import forms.ElectCrsCarfGrossProceedsFormProvider
import models.*
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ValidXMLPage
import pages.elections.crs.ElectCrsCarfGrossProceedsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.elections.crs.ElectCrsCarfGrossProceedsView

import java.time.LocalDate
import scala.concurrent.Future

class ElectCrsCarfGrossProceedsControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private val mockFiName: String     = "Test Financial Institution"
  private val mockReportingYear: Int = LocalDate.now().getYear

  private val mockMessageSpecData: MessageSpecData = getMessageSpecData(CRS, CRSReportType.TestData, fiNameFromFim = mockFiName)
  private val requiredUserAnswers: UserAnswers     = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(mockMessageSpecData))

  private val formProvider = new ElectCrsCarfGrossProceedsFormProvider()
  private val form         = formProvider(mockReportingYear)

  private lazy val electCrsCarfGrossProceedsRoute = controllers.elections.crs.routes.ElectCrsCarfGrossProceedsController.onPageLoad(NormalMode).url

  "ElectCrsCarfGrossProceeds Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(requiredUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electCrsCarfGrossProceedsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ElectCrsCarfGrossProceedsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(mockFiName, mockReportingYear, form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = requiredUserAnswers.set(ElectCrsCarfGrossProceedsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electCrsCarfGrossProceedsRoute)

        val view = application.injector.instanceOf[ElectCrsCarfGrossProceedsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(mockFiName, mockReportingYear, form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(requiredUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, electCrsCarfGrossProceedsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(requiredUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, electCrsCarfGrossProceedsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ElectCrsCarfGrossProceedsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(mockFiName, mockReportingYear, boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Page Unavailable for a GET if no ValidXMLPage data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, electCrsCarfGrossProceedsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }

    "must redirect to Page Unavailable for a POST if no ValidXMLPage data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, electCrsCarfGrossProceedsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }
  }
}
