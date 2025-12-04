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
import pages.elections.crs.{DormantAccountsPage, ElectCrsCarfGrossProceedsPage, ElectCrsContractPage, ElectCrsGrossProceedsPage, ThresholdsPage}
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}
import play.api.mvc.Call
import utils.ReportingConstants.*

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

  private val checkRouteMap: Page => UserAnswers => Call = {
    case ElectCrsCarfGrossProceedsPage =>
      userAnswers =>
        userAnswers.get(ElectCrsCarfGrossProceedsPage) match {
          case Some(true)  => controllers.elections.crs.routes.ElectCrsGrossProceedsController.onPageLoad(NormalMode)
          case Some(false) => routes.CheckYourFileDetailsController.onPageLoad()
          case None        => routes.JourneyRecoveryController.onPageLoad()
        }
    case ReportElectionsPage =>
      userAnswers =>
        userAnswers.get(ReportElectionsPage) match {
          case Some(true) =>
            getMessageSpecData(userAnswers) {
              messageSpecData =>
                messageSpecData.messageType match {
                  case CRS =>
                    controllers.elections.crs.routes.ElectCrsContractController.onPageLoad(NormalMode)
                  case FATCA =>
                    controllers.elections.fatca.routes.TreasuryRegulationsController.onPageLoad(NormalMode)
                }
            }
          case Some(false) =>
            routes.CheckYourFileDetailsController.onPageLoad()
        }
    case ElectCrsGrossProceedsPage | ThresholdsPage | DormantAccountsPage | ElectCrsContractPage | ElectFatcaThresholdsPage | TreasuryRegulationsPage |
        RequiredGiinPage =>
      _ => routes.CheckYourFileDetailsController.onPageLoad()
    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case ValidXMLPage =>
      userAnswers => validFileUploadedNavigation(userAnswers)
    case RequiredGiinPage =>
      userAnswers => requiredGiinNavigation(userAnswers)
    case ElectFatcaThresholdsPage =>
      _ => routes.CheckYourFileDetailsController.onPageLoad()
    case TreasuryRegulationsPage =>
      _ => controllers.elections.fatca.routes.ElectFatcaThresholdsController.onPageLoad(NormalMode)
    case DormantAccountsPage =>
      _ => controllers.elections.crs.routes.ThresholdsController.onPageLoad(NormalMode)
    case ElectCrsContractPage =>
      userAnswers => controllers.elections.crs.routes.DormantAccountsController.onPageLoad(NormalMode)
    case ElectCrsCarfGrossProceedsPage =>
      userAnswers =>
        userAnswers.get(ElectCrsCarfGrossProceedsPage) match {
          case Some(true)  => controllers.elections.crs.routes.ElectCrsGrossProceedsController.onPageLoad(NormalMode)
          case Some(false) => routes.CheckYourFileDetailsController.onPageLoad()
          case None        => routes.JourneyRecoveryController.onPageLoad()
        }
    case ThresholdsPage =>
      userAnswers => thresholdsNavigation(userAnswers)
    case ElectCrsGrossProceedsPage =>
      _ => routes.CheckYourFileDetailsController.onPageLoad()
    case ReportElectionsPage =>
      userAnswers =>
        userAnswers.get(ReportElectionsPage) match {
          case Some(true) =>
            getMessageSpecData(userAnswers) {
              messageSpecData =>
                messageSpecData.messageType match {
                  case CRS =>
                    controllers.elections.crs.routes.ElectCrsContractController.onPageLoad(NormalMode)
                  case FATCA =>
                    controllers.elections.fatca.routes.TreasuryRegulationsController.onPageLoad(NormalMode)
                }
            }
          case Some(false) =>
            routes.CheckYourFileDetailsController.onPageLoad()
        }

    case _ => _ => routes.IndexController.onPageLoad()
  }

  private def thresholdsNavigation(userAnswers: UserAnswers): Call =
    getMessageSpecData(userAnswers) {
      messageSpecData =>
        if (messageSpecData.reportingPeriod.getYear >= ThresholdDate.getYear) {
          controllers.elections.crs.routes.ElectCrsCarfGrossProceedsController.onPageLoad(NormalMode)
        } else {
          controllers.routes.CheckYourFileDetailsController.onPageLoad()
        }
    }

  private def validFileUploadedNavigation(userAnswers: UserAnswers): Call =
    getMessageSpecData(userAnswers) {
      messageSpecData =>
        if (messageSpecData.giin.isEmpty && messageSpecData.messageType == FATCA) {
          routes.RequiredGiinController.onPageLoad(NormalMode)
        } else {
          redirectToElectionPageOrCheckFileDetails(messageSpecData.reportingPeriod.getYear)
        }
    }

  private def requiredGiinNavigation(userAnswers: UserAnswers): Call =
    getMessageSpecData(userAnswers) {
      messageSpecData =>
        redirectToElectionPageOrCheckFileDetails(messageSpecData.reportingPeriod.getYear)
    }

  private def redirectToElectionPageOrCheckFileDetails(reportingPeriodYear: Int): Call = {
    def requiresElection(reportingYear: Int): Boolean =
      isReportingYearValid(reportingYear) && !hasElectionsHappened

    def isReportingYearValid(reportingYear: Int): Boolean = {
      val currentYear = LocalDate.now(EUROPE_LONDON_TIME_ZONE).getYear
      reportingYear >= (currentYear - 12) && reportingYear <= currentYear
    }

    /* Will be implemented later in  DAC6-3959 & DAC6-3964
    Placeholder implementation; replace with actual logic to determine if elections have happened */
    def hasElectionsHappened: Boolean = false

    if (requiresElection(reportingPeriodYear)) {
      controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode)
    } else {
      routes.CheckYourFileDetailsController.onPageLoad()
    }
  }

}
