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
import models.CRSReportType.NewInformation
import models.{CRS, UserAnswers}
import pages.ValidXMLPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class FileConfirmationControllerSpec extends SpecBase {

  val messageSpecData = getMessageSpecData(CRS, fiNameFromFim = "Some-fi-name", reportType = NewInformation)
  val ua: UserAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(messageSpecData))

  "FileConfirmation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.FileConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        val resultHtml = contentAsString(result)
        resultHtml must include("c-8-new-f-va")
        resultHtml must include("CRS")
        resultHtml must include("EFG Bank plc")
        resultHtml must include("New information")
        resultHtml must include("12 September 2025")
        resultHtml must include("12:01pm")
      }
    }

    "must redirect to page unavailable when valid xml page is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.FileConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PageUnavailableController.onPageLoad().url
      }
    }
  }
}
