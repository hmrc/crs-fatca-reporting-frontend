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

package controllers.fatca

import models.{FATCA, FATCAReportType}
import pages.ValidXMLPage
import utils.ISpecBehaviours

class ElectFatcaThresholdsControllerISpec extends ISpecBehaviours {

  private val path            = "/report/elections/fatca/thresholds"
  private val messageSpecData = getMessageSpecData(FATCA, FATCAReportType.TestData)
  private val userAnswers     = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(messageSpecData))

  "GET ElectFatcaThresholdsController.onPageLoad" must {
    behave like pageLoads(path = path, pageTitle = "election.fatca.thresholds.title", userAnswers = userAnswers)
    behave like pageRedirectsWhenNotAuthorised(path)
  }

  "Post ElectFatcaThresholdsController.onSubmit" must {
    val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("true"))

    behave like standardOnSubmit(path, requestBody)
    behave like pageSubmits(path, "/report-for-crs-and-fatca/report/check-your-file-details", userAnswers, requestBody)
  }
}
