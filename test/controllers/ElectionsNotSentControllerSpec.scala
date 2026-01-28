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
import pages.{GiinAndElectionStatusPage, RequiredGiinPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ElectionsNotSentView

class ElectionsNotSentControllerSpec extends SpecBase {

  "ElectionsNotSent Controller" - {
    val answers = emptyUserAnswers.withPage(GiinAndElectionStatusPage, GiinAndElectionDBStatus(true, true))

    "must return OK and the correct view for a GET when No GIIN sent" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ElectionsNotSentController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ElectionsNotSentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(false)(request, messages(application)).toString
      }
    }
    "must return OK and the correct view for a GET when GIIN sent" in {

      val application = applicationBuilder(userAnswers = Some(answers.withPage(RequiredGiinPage, "98096B.00000.LE.350"))).build()

      running(application) {
        val request = FakeRequest(GET, routes.ElectionsNotSentController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ElectionsNotSentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(true)(request, messages(application)).toString
      }
    }

    "must return REDIRECT and the correct view for a GET When no required data" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.withPage(RequiredGiinPage, "98096B.00000.LE.350"))).build()

      running(application) {
        val request = FakeRequest(GET, routes.ElectionsNotSentController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ElectionsNotSentView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
