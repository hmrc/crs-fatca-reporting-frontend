/*
 * Copyright 2026 HM Revenue & Customs
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

package models.submission

import models.{CRS, FATCA, MessageSpecData, UserAnswers}
import pages.RequiredGiinPage
import pages.elections.crs.{DormantAccountsPage, ElectCrsCarfGrossProceedsPage, ElectCrsContractPage, ThresholdsPage}
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}

case class GiinAndElectionSubmissionRequest(giinUpdateRequest: Option[GiinUpdateRequest], electionsSubmissionRequest: Option[ElectionsSubmissionDetails])

object GiinAndElectionSubmissionRequest {

  def apply(messageSpecData: MessageSpecData, userAnswers: UserAnswers, fatcaId: String): GiinAndElectionSubmissionRequest = {

    val reportingPeriod = messageSpecData.reportingPeriod.toString
    (messageSpecData.messageType, messageSpecData.electionsRequired) match {
      case (CRS, false) =>
        GiinAndElectionSubmissionRequest(None, None)
      case (CRS, true) =>
        val crsElectionsDetails: CrsElectionsDetails = CrsElectionsDetails(
          hasCARF = userAnswers.get(ElectCrsCarfGrossProceedsPage),
          hasContracts = userAnswers.get(ElectCrsContractPage),
          hasDormantAccounts = userAnswers.get(DormantAccountsPage),
          hasThresholds = userAnswers.get(ThresholdsPage)
        )
        GiinAndElectionSubmissionRequest(
          None,
          Some(
            ElectionsSubmissionDetails(
              fiId = messageSpecData.sendingCompanyIN,
              reportingPeriod = reportingPeriod,
              crsDetails = Some(crsElectionsDetails),
              fatcaDetails = None
            )
          )
        )
      case (FATCA, true) =>
        val fatcaElectionRequest =
          FatcaElectionsDetails(hasThresholds = userAnswers.get(ElectFatcaThresholdsPage), hasTreasuryRegulations = userAnswers.get(TreasuryRegulationsPage))

        val giinUpdateRequest: Option[GiinUpdateRequest] =
          if (messageSpecData.giin.isDefined) None
          else {
            userAnswers
              .get(RequiredGiinPage)
              .map {
                giin =>
                  GiinUpdateRequest(
                    subscriptionId = fatcaId,
                    fiid = messageSpecData.sendingCompanyIN,
                    giin = giin
                  )
              }
          }

        GiinAndElectionSubmissionRequest(
          giinUpdateRequest,
          Some(
            ElectionsSubmissionDetails(
              fiId = messageSpecData.sendingCompanyIN,
              reportingPeriod = reportingPeriod,
              crsDetails = None,
              fatcaDetails = Some(fatcaElectionRequest)
            )
          )
        )

      case (FATCA, false) =>
        val giinUpdateRequest: Option[GiinUpdateRequest] =
          if (messageSpecData.giin.isDefined) None
          else {
            userAnswers
              .get(RequiredGiinPage)
              .map {
                giin =>
                  GiinUpdateRequest(
                    subscriptionId = fatcaId,
                    fiid = messageSpecData.sendingCompanyIN,
                    giin = giin
                  )
              }
          }
        GiinAndElectionSubmissionRequest(giinUpdateRequest, None)

    }
  }
}
