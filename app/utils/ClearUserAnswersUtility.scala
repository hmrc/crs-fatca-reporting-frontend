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

package utils

import pages.{InvalidXMLPage, ReportElectionsPage, RequiredGiinPage, ValidXMLPage}
import pages.elections.crs.{DormantAccountsPage, ElectCrsCarfGrossProceedsPage, ElectCrsContractPage, ElectCrsGrossProceedsPage, ThresholdsPage}
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}
import queries.Settable

def reportElectionPages: Seq[Settable[_]] =
  Seq(
    TreasuryRegulationsPage,
    ElectFatcaThresholdsPage,
    ElectCrsContractPage,
    DormantAccountsPage,
    ThresholdsPage,
    ElectCrsGrossProceedsPage,
    ElectCrsCarfGrossProceedsPage
  )

def uploadFilePagesForValidXml(): Seq[Settable[_]] =
  reportElectionPages ++ Seq(ReportElectionsPage, RequiredGiinPage, InvalidXMLPage)

def uploadFilePagesForInvalidXml(): Seq[Settable[_]] =
  reportElectionPages ++ Seq(ReportElectionsPage, RequiredGiinPage, ValidXMLPage)
