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
import forms.elections.fatca.TreasuryRegulationsFormProvider
import models.{FATCA, MessageSpecData, NormalMode, UserAnswers, ValidatedFileData}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ValidXMLPage
import pages.elections.fatca.TreasuryRegulationsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.elections.fatca.TreasuryRegulationsView

import java.time.LocalDate
import scala.concurrent.Future

class TreasuryRegulationsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider            = new TreasuryRegulationsFormProvider()
  private val form                    = formProvider()
  private val reportingPeriodYear     = 2024
  private val fiName                  = "fi-name"
  private val reportingPeriod: String = reportingPeriodYear.toString
  private val fileName                = "test-file.xml"
  private val FileSize                = 100L
  private val FileChecksum            = "checksum"
  private val expectedFiName          = "fi-name"

  lazy val treasuryRegulationsRoute = controllers.elections.fatca.routes.TreasuryRegulationsController.onPageLoad(NormalMode).url

  val fatcaMessageSpec = MessageSpecData(
    messageType = FATCA,
    sendingCompanyIN = "sendingCompanyIN",
    messageRefId = "messageRefId",
    reportingFIName = "reportingFIName",
    reportingPeriod = LocalDate.of(reportingPeriodYear, 1, 1),
    giin = None,
    fiNameFromFim = fiName
  )

  val fatcaValidatedFileData        = ValidatedFileData(fileName, fatcaMessageSpec, FileSize, FileChecksum)
  val fatcaUserAnswers: UserAnswers = UserAnswers(userAnswersId).set(ValidXMLPage, fatcaValidatedFileData).success.value

  "TreasuryRegulations Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(fatcaUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, treasuryRegulationsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TreasuryRegulationsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, fiName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = fatcaUserAnswers.set(TreasuryRegulationsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, treasuryRegulationsRoute)

        val view = application.injector.instanceOf[TreasuryRegulationsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, fiName)(request, messages(application)).toString
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
          FakeRequest(POST, treasuryRegulationsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.elections.fatca.routes.ElectFatcaThresholdsController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(fatcaUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, treasuryRegulationsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[TreasuryRegulationsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, fiName)(request, messages(application)).toString
      }
    }

  }
}
