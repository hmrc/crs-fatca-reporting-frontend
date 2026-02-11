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
import models.{FATCA, FATCAReportType, UserAnswers}
import pages.ValidXMLPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.FileCheckViewModel
import views.html.FileFailedChecksView

class FileFailedChecksControllerSpec extends SpecBase {

  "FileFailedChecks Controller" - {
    val fiName       = "Test FI Name"
    val messageRefId = "some-ref-id"
    val ua: UserAnswers =
      emptyUserAnswers.withPage(ValidXMLPage,
                                getValidatedFileData(getMessageSpecData(FATCA, FATCAReportType.TestData, messageRefId = messageRefId, fiNameFromFim = fiName))
      )

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      val messagesApi = messages(application)

      val summary = FileCheckViewModel.createFileSummary(messageRefId, "Rejected")(messagesApi)

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileFailedChecksView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(summary)(request, messages(application)).toString
      }
    }

    "must redirect to page unavailable when valid xml page is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val messagesApi = messages(application)

      val summary = FileCheckViewModel.createFileSummary(messageRefId, "Rejected")(messagesApi)

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileFailedChecksView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }
  }
}
