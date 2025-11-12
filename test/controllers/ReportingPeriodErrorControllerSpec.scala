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
import models.TimeZones.EUROPE_LONDON_TIME_ZONE
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import java.time.LocalDate

class ReportingPeriodErrorControllerSpec extends SpecBase {

  "ReportingPeriodError Controller" - {

    "must return OK and the correct view for a GET" in {

      val application     = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val currentYear     = LocalDate.now(EUROPE_LONDON_TIME_ZONE).getYear
      val expectedMessage = s"The ReportingPeriod element in your file must contain a date between 31 December 2014 and 31 December $currentYear."

      running(application) {
        val request = FakeRequest(GET, routes.ReportingPeriodErrorController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(expectedMessage)
      }
    }
  }
}
