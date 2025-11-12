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
import models.TimeZones.EUROPE_LONDON_TIME_ZONE
import models.UserAnswers.getMessageSpecData
import pages.*
import play.api.mvc.Call

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private val checkRouteMap: Page => UserAnswers => Call = _ => _ => routes.IndexController.onPageLoad()

  private val normalRoutes: Page => UserAnswers => Call = {
    case ValidXMLPage =>
      userAnswers => validFileUploadedNavigation(userAnswers)
    case RequiredGiinPage =>
      userAnswers => requiredGiinNavigation(userAnswers)
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private def validFileUploadedNavigation(userAnswers: UserAnswers): Call =
    getMessageSpecData(userAnswers) {
      messageSpecData =>
        if (messageSpecData.giin.isEmpty && messageSpecData.messageType == FATCA) {
          routes.RequiredGiinController.onPageLoad(NormalMode)
        } else {
          redirectToElectionPageOrCheckYourAnswers(messageSpecData)
        }
    }

  private def requiredGiinNavigation(userAnswers: UserAnswers): Call =
    getMessageSpecData(userAnswers) {
      messageSpecData =>
        redirectToElectionPageOrCheckYourAnswers(messageSpecData)
    }

  private def redirectToElectionPageOrCheckYourAnswers(messageSpecData: MessageSpecData): Call = {
    def requiresElection(reportingYear: Int): Boolean =
      isReportingYearValid(reportingYear) && messageSpecData.electionsRequired

    def isReportingYearValid(reportingYear: Int): Boolean = {
      val currentYear = LocalDate.now(EUROPE_LONDON_TIME_ZONE).getYear
      reportingYear >= (currentYear - 12) && reportingYear <= currentYear
    }

    if (requiresElection(messageSpecData.reportingPeriod.getYear)) {
      controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }
  }

}
