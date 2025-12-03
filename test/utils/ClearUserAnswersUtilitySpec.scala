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

import models.{CRS, FATCA}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.elections.crs.{DormantAccountsPage, ElectCrsCarfGrossProceedsPage, ElectCrsContractPage, ElectCrsGrossProceedsPage, ThresholdsPage}
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}

class ClearUserAnswersUtilitySpec extends AnyFreeSpec with Matchers {
  "reportElectionPages" - {
    "returns all pages for fatca regime" in {
      reportElectionPages(FATCA) mustEqual Seq(
        TreasuryRegulationsPage,
        ElectFatcaThresholdsPage
      )
    }

    "returns all pages for crs regine" in {
      reportElectionPages(CRS) mustEqual Seq(
        ElectCrsContractPage,
        DormantAccountsPage,
        ThresholdsPage,
        ElectCrsCarfGrossProceedsPage,
        ElectCrsGrossProceedsPage
      )
    }
  }
}
