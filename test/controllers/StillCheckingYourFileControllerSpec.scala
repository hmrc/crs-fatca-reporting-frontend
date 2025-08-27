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
import config.FrontendAppConfig
import org.scalactic.Prettifier.default
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.FileCheckViewModel
import views.html.StillCheckingYourFileView

class StillCheckingYourFileControllerSpec extends SpecBase {

  "StillCheckingYourFIle Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val appConfig = application.injector.instanceOf[FrontendAppConfig]

      running(application) {
        val request = FakeRequest(GET, routes.StillCheckingYourFileController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[StillCheckingYourFileView]

        val messagesApi         = messages(application)
        val expectedSummaryList = FileCheckViewModel.createFileSummary("MyFATCAReportMessageRefId1234567890", "Pending")(messagesApi)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedSummaryList, appConfig.signOutUrl, true, "EFG Bank plc")(request, messages(application)).toString
      }
    }
  }
}
