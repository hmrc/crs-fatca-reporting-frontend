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

package controllers.elections

import base.SpecBase
import controllers.routes
import forms.elections.ReportElectionsFormProvider
import models.{CRS, CheckMode, FATCA, NormalMode, UserAnswers, ValidatedFileData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{ReportElectionsPage, ValidXMLPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.elections.ReportElectionsView

import java.time.LocalDate
import scala.concurrent.Future

class ReportElectionsControllerSpec extends SpecBase with MockitoSugar {

  val reportingPeriodYear = 2024
  val reportingPeriod     = reportingPeriodYear.toString
  val fileName            = "test-file.xml"
  val FileSize            = 100L
  val FileChecksum        = "checksum"
  val expectedFiName      = "fi-name"

  val crsMessageSpec = getMessageSpecData(CRS, reportingPeriod = LocalDate.of(reportingPeriodYear, 1, 1))

  val crsValidatedFileData = getValidatedFileData(crsMessageSpec)
  val crsUserAnswers       = UserAnswers(userAnswersId).set(ValidXMLPage, crsValidatedFileData).success.value
  val crsRegime            = crsMessageSpec.messageType.toString

  val fatcaMessageSpec       = crsMessageSpec.copy(messageType = FATCA)
  val fatcaValidatedFileData = ValidatedFileData(fileName, fatcaMessageSpec, FileSize, FileChecksum)
  val fatcaUserAnswers       = UserAnswers(userAnswersId).set(ValidXMLPage, fatcaValidatedFileData).success.value
  val fatcaRegime            = fatcaMessageSpec.messageType.toString

  val formProvider = new ReportElectionsFormProvider()
  val crsForm      = formProvider(crsRegime)

  lazy val reportElectionsRoute          = controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode).url
  lazy val reportElectionsRouteCheckMode = controllers.elections.routes.ReportElectionsController.onPageLoad(CheckMode).url

  lazy val fatcaOnwardRoute                = controllers.elections.fatca.routes.TreasuryRegulationsController.onPageLoad(NormalMode).url
  lazy val crsOnwardRoute                  = controllers.elections.crs.routes.ElectCrsContractController.onPageLoad(NormalMode).url
  lazy val checkYourFileDetailsOnwardRoute = routes.CheckYourFileDetailsController.onPageLoad().url

  "ReportElections Controller" - {

    "must return OK and the correct view for a GET when ValidXMLPage data exists (CRS)" in {

      val application = applicationBuilder(userAnswers = Some(crsUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, reportElectionsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReportElectionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(reportingPeriod, crsRegime, expectedFiName, crsForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered (CRS)" in {

      val userAnswers = crsUserAnswers.set(ReportElectionsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, reportElectionsRoute)

        val view = application.injector.instanceOf[ReportElectionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(reportingPeriod, crsRegime, expectedFiName, crsForm.fill(true), NormalMode)(request,
                                                                                                                           messages(application)
        ).toString
      }
    }

    "must redirect to ElectCRSContractController on submission when regime is CRS" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(crsUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reportElectionsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual crsOnwardRoute
      }
    }

    "must redirect to TreasuryRegulationsController on submission when regime is FATCA" in {

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
          FakeRequest(POST, reportElectionsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual fatcaOnwardRoute
      }
    }

    "must redirect to TreasuryRegulationsController on submission when regime is FATCA on checkmode" in {

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
          FakeRequest(POST, reportElectionsRouteCheckMode)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual fatcaOnwardRoute
      }
    }

    "must redirect to Check your file detail on submission when user does not require elections" in {

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
          FakeRequest(POST, reportElectionsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual checkYourFileDetailsOnwardRoute
      }
    }

    "must redirect to Check your file detail on submission when user does not require elections on check mode for FATCA" in {

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
          FakeRequest(POST, reportElectionsRouteCheckMode)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual checkYourFileDetailsOnwardRoute
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(crsUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, reportElectionsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = crsForm.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ReportElectionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(reportingPeriod, crsRegime, expectedFiName, boundForm, NormalMode)(request, messages(application)).toString
      }
    }
  }
}
