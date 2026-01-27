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
import models.submission.GiinAndElectionDBStatus
import pages.{GiinAndElectionStatusPage, ReportElectionsPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.GiinNotSentView

class GiinNotSentControllerSpec extends SpecBase {

  "GiinNotSent Controller" - {

    val answers = emptyUserAnswers.withPage(GiinAndElectionStatusPage, GiinAndElectionDBStatus(true, true))

    "must return OK and the correct view for a GET When there is no report election" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.GiinNotSentController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[GiinNotSentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(None)(request, messages(application)).toString
      }
    }
    "must return OK and the correct view for a GET When report election is false" in {

      val application = applicationBuilder(userAnswers = Some(answers.withPage(ReportElectionsPage, false))).build()

      running(application) {
        val request = FakeRequest(GET, routes.GiinNotSentController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[GiinNotSentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(None)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET When report election is true and election sent" in {

      val application = applicationBuilder(userAnswers = Some(answers.withPage(ReportElectionsPage, true))).build()

      running(application) {
        val request = FakeRequest(GET, routes.GiinNotSentController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[GiinNotSentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Some(true))(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET When report election is true and election sending failed" in {

      val answers = emptyUserAnswers.withPage(GiinAndElectionStatusPage, GiinAndElectionDBStatus(true, false))

      val application = applicationBuilder(userAnswers = Some(answers.withPage(ReportElectionsPage, true))).build()

      running(application) {
        val request = FakeRequest(GET, routes.GiinNotSentController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[GiinNotSentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Some(false))(request, messages(application)).toString
      }
    }

    "must return REDIRECT and the correct view for a GET When no required data" in {

      val answers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(answers.withPage(ReportElectionsPage, true))).build()

      running(application) {
        val request = FakeRequest(GET, routes.GiinNotSentController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[GiinNotSentView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
