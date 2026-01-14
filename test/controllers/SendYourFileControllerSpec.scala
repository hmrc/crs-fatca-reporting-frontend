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
import models.{FATCA, SendYourFileAdditionalText, UserAnswers}
import pages.ValidXMLPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.SendYourFileView

class SendYourFileControllerSpec extends SpecBase {

  lazy val pageUnavailableUrl: String = controllers.routes.PageUnavailableController.onPageLoad().url
  lazy val sendYourFileUrl: String    = routes.SendYourFileController.onPageLoad().url
  val hardcodedFiName                 = "testFiName"
  val exampleGiin                     = "8Q298C.00000.LE.340"
  val ua: UserAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(getMessageSpecData(FATCA, fiNameFromFim = hardcodedFiName)))

  "SendYourFile Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SendYourFileView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(SendYourFileAdditionalText.NONE)(request, messages(application)).toString
      }
    }

    "must redirect to page unavailable when xml valid page is not present in user answers for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, sendYourFileUrl)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual pageUnavailableUrl
      }
    }
  }
}
