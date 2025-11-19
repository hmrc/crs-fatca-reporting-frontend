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

package controllers.elections.crs

import models.{CRS, MessageSpecData, ValidatedFileData}
import pages.ValidXMLPage
import utils.ISpecBehaviours
import java.time.LocalDate

class ThresholdsControllerISpec extends ISpecBehaviours {

  private val path = "/report/elections/crs/thresholds"
  val fiNameFM = "Test Financial Institution"
  private val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("true"))

  private def userAnswersWithReportingPeriod(year: Int) = {
    val messageSpec = MessageSpecData(
      messageType = CRS,
      sendingCompanyIN = "IN",
      messageRefId = "ref",
      reportingFIName = "FIName",
      reportingPeriod = LocalDate.of(year, 12, 31),
      giin = Some("giin"),
      fiNameFromFim = fiNameFM
    )
    emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(messageSpec))
  }

  val userAnswers2025 = userAnswersWithReportingPeriod(2025)
  val userAnswers2026 = userAnswersWithReportingPeriod(2026)

  private val routeFor2025OrEarlier = "/report/elections/check-your-file-details"
  private val routeFor2026OrLater = "/report/elections/crs/carf-gross-proceeds"

  "GET ThresholdsController.onPageLoad" must {
    behave like pageLoads(path = path, pageTitle = "elections.crs.thresholds.title", userAnswers = userAnswers2025)
    behave like pageRedirectsWhenNotAuthorised(path)
  }

  "POST ThresholdsController.onSubmit" must {

    "redirect to CheckYourFileDetailsController when reporting period is 2025 or earlier" in {
      behave like pageSubmits(
        path = path,
        redirectLocation = routeFor2025OrEarlier,
        ua = userAnswers2025,
        requestBody = requestBody
      )
    }

    "redirect to ElectCrsCarfGrossProceedsController when reporting period is 2026 or later" in {
      behave like pageSubmits(
        path = path,
        redirectLocation = routeFor2026OrLater,
        ua = userAnswers2026,
        requestBody = requestBody
      )
    }

    behave like standardOnSubmit(path, requestBody)
  }
}