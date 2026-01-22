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
import models.{CRS, CRSReportType, ValidatedFileData}
import org.scalatest.matchers.must.Matchers
import pages.ValidXMLPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.CheckYourFileDetailsViewModel
import views.html.CheckYourFileDetailsView

import java.time.LocalDate

class CheckYourFileDetailsControllerSpec extends SpecBase with Matchers {

  "CheckYourFileDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val reportingPeriodYear = 2025
      val answers =
        emptyUserAnswers.withPage(
          ValidXMLPage,
          getValidatedFileData(getMessageSpecData(CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(reportingPeriodYear, 1, 1)))
        )

      val application         = applicationBuilder(userAnswers = Some(answers)).build()
      val messagesApi         = messages(application)
      val helperModel         = CheckYourFileDetailsViewModel(answers)(using messagesApi)
      val expectedFileDetails = helperModel.fileDetailsSummary
      val expectedFIDetails   = helperModel.financialInstitutionDetailsSummary

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourFileDetailsController.onPageLoad().url)
        val view    = application.injector.instanceOf[CheckYourFileDetailsView]
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(expectedFileDetails, expectedFIDetails, getMessageSpecData(CRS, CRSReportType.TestData).fiNameFromFim)(request, messagesApi).toString
      }
    }
  }
}
