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

package navigation

import controllers.routes
import models.*
import pages.*
import play.api.mvc.Call

import java.time.{LocalDate, ZoneId}
import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case ValidXMLPage =>
      userAnswers =>
        userAnswers.get(ValidXMLPage) match {
          case Some(validatedFileData) =>
            val messageSpecData = validatedFileData.messageSpecData
            if (messageSpecData.giin.isEmpty && messageSpecData.messageType == FATCA) {
              routes.RequiredGiinController.onPageLoad(NormalMode)
            } else {
              if (requiresElection(messageSpecData.reportingPeriod.getYear))
                controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode)
              else
                routes.CheckYourAnswersController.onPageLoad()
            }
          case None => routes.IndexController.onPageLoad()
        }
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.IndexController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private def requiresElection(reportingYear: Int): Boolean =
    isReportingYearValid(reportingYear) && !hasElectionsHappened()

  private def isReportingYearValid(reportingYear: Int): Boolean = {
    val currentYear = LocalDate.now(ZoneId.of("Europe/London")).getYear
    reportingYear >= (currentYear - 12) && reportingYear <= currentYear
  }

  /* Will be implemented later in  DAC6-3959 & DAC6-3964
  Placeholder implementation; replace with actual logic to determine if elections have happened */
  private def hasElectionsHappened(): Boolean = false
}
