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

package services

import cats.data.EitherT
import connectors.SubmissionConnector
import models.UserAnswers
import models.UserAnswers.extractMessageSpecData
import models.requests.DataRequest
import models.submission.*
import pages.elections.crs.{DormantAccountsPage, ElectCrsCarfGrossProceedsPage, ElectCrsContractPage, ThresholdsPage}
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}
import pages.{RequiredGiinPage, RequiresElectionsPage}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionService @Inject() (val connector: SubmissionConnector) extends Logging {

  def submitElectionsAndGiin(userAnswers: UserAnswers)(using
    request: DataRequest[_],
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[GiinAndElectionStatus] = {

    val giinUpdateRequest: Option[GiinUpdateRequest]                   = getGiinRequest(userAnswers)
    val electionsSubmissionRequest: Option[ElectionsSubmissionDetails] = getElectionsRequest(userAnswers)
    val giinFuture                                                     = giinUpdateRequest.fold(Future.successful(true))(connector.updateGiin)
    val electionsFuture                                                = electionsSubmissionRequest.fold(Future.successful(true))(connector.submitElections)

    (for {
      giinResult      <- EitherT.right(giinFuture)
      electionsResult <- EitherT.right(electionsFuture)
      _               <- checkElectionsGiinSubmissionIsSuccessful(giinResult, electionsResult)
    } yield GiinAndElectionSubmittedSuccessful).merge
  }

  private def checkElectionsGiinSubmissionIsSuccessful(giinStatus: Boolean, electionStatus: Boolean): EitherT[Future, GiinAndElectionStatus, Unit] =
    EitherT {
      (giinStatus, electionStatus) match {
        case (true, true)  => Future.successful(Right(()))
        case (false, _)    => Future.successful(Left(GiinUpdateFailed(giinStatus, electionStatus)))
        case (true, false) => Future.successful(Left(ElectionsSubmitFailed(giinStatus, electionStatus)))
      }
    }

  private def getGiinRequest(userAnswers: UserAnswers)(using request: DataRequest[_]): Option[GiinUpdateRequest] =
    userAnswers.get(RequiredGiinPage).fold(None: Option[GiinUpdateRequest]) {
      giin =>
        extractMessageSpecData(userAnswers) {
          messageSpecData =>
            val subId = request.fatcaId
            val fiId  = messageSpecData.sendingCompanyIN

            Some(GiinUpdateRequest(subId, fiId, giin))

        }

    }

  private def getElectionsRequest(userAnswers: UserAnswers)(using request: DataRequest[_]): Option[ElectionsSubmissionDetails] =
    userAnswers.get(RequiresElectionsPage).fold(None: Option[ElectionsSubmissionDetails]) {
      case false => None
      case true =>
        extractMessageSpecData(userAnswers) {
          messageSpecData =>
            val fiId            = messageSpecData.sendingCompanyIN
            val reportingPeriod = messageSpecData.reportingPeriod.getYear.toString

            val crsHasCARF            = userAnswers.get(ElectCrsCarfGrossProceedsPage).fold(None)(Some(_))
            val crsHasContracts       = userAnswers.get(ElectCrsContractPage).fold(None)(Some(_))
            val crsHasDormantAccounts = userAnswers.get(DormantAccountsPage).fold(None)(Some(_))
            val crsHasThresholds      = userAnswers.get(ThresholdsPage).fold(None)(Some(_))

            val fatHasThresholds          = userAnswers.get(ElectFatcaThresholdsPage).fold(None)(Some(_))
            val fatHasTreasuryRegulations = userAnswers.get(TreasuryRegulationsPage).fold(None)(Some(_))

            val crsDetails   = Some(CrsElectionsDetails(crsHasCARF, crsHasContracts, crsHasDormantAccounts, crsHasThresholds))
            val fatcaDetails = Some(FatcaElectionsDetails(hasThresholds = fatHasThresholds, hasTreasuryRegulations = fatHasTreasuryRegulations))

            Some(
              ElectionsSubmissionDetails(
                fiId = fiId,
                reportingPeriod = reportingPeriod,
                crsDetails = crsDetails,
                fatcaDetails = fatcaDetails
              )
            )
        }
    }
}
