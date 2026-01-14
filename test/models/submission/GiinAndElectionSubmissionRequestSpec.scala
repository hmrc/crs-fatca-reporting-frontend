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

import base.SpecBase
import models.{CRS, FATCA}
import org.scalatest
import org.scalatest.matchers.should.Matchers.shouldBe
import pages.RequiredGiinPage
import pages.elections.crs.{DormantAccountsPage, ElectCrsCarfGrossProceedsPage, ElectCrsContractPage, ThresholdsPage}
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}
import submissions.{createCrsElectionsDetails, createFatcaElectionsDetails}

import java.time.LocalDate

class GiinAndElectionSubmissionRequestSpec extends SpecBase {
  val reportingPeriod = LocalDate.of(2000, 12, 1)

  "CRS GiinAndElectionSubmissionRequest" - {
    "should return none for giinUpdateRequest and electionsSubmissionRequest when electionsRequired is false" in {
      val messageSpecData = getMessageSpecData(messageType = CRS, electionsRequired = false)
      val userAnswers     = emptyUserAnswers

      val result = GiinAndElectionSubmissionRequest(messageSpecData, userAnswers, fatcaId = "12345")

      result.giinUpdateRequest shouldBe None
      result.electionsSubmissionRequest shouldBe None
    }

    "should return none for giinUpdateRequest and define an electionsSubmissionRequest for CRS when electionsRequired is true" in {
      val messageSpecData = getMessageSpecData(reportingPeriod = reportingPeriod, messageType = CRS, electionsRequired = true)

      val userAnswers = emptyUserAnswers
        .withPage(ElectCrsCarfGrossProceedsPage, true)
        .withPage(ElectCrsContractPage, false)
        .withPage(DormantAccountsPage, true)
        .withPage(ThresholdsPage, false)

      val result = GiinAndElectionSubmissionRequest(messageSpecData, userAnswers, fatcaId = "12345")

      result.giinUpdateRequest shouldBe None
      result.electionsSubmissionRequest.isDefined shouldBe true
      result shouldBe GiinAndElectionSubmissionRequest(
        None,
        Some(
          ElectionsSubmissionDetails(fiId = "testFI",
                                     reportingPeriod = reportingPeriod.toString,
                                     crsDetails = Some(createCrsElectionsDetails()),
                                     fatcaDetails = None
          )
        )
      )

    }
  }

  "FATCA GiinAndElectionSubmissionRequest" - {
    "should return none for giinUpdateRequest and define an electionsSubmissionRequest for Fatca when electionsRequired is true and giin is present in messageSpecData" in {
      val messageSpecData = getMessageSpecData(giin = Some("12345"), reportingPeriod = reportingPeriod, messageType = FATCA, electionsRequired = true)

      val userAnswers = emptyUserAnswers
        .withPage(ElectFatcaThresholdsPage, true)
        .withPage(TreasuryRegulationsPage, false)

      val result = GiinAndElectionSubmissionRequest(messageSpecData, userAnswers, fatcaId = "12345")

      result.giinUpdateRequest shouldBe None
      result.electionsSubmissionRequest.isDefined shouldBe true

      result shouldBe GiinAndElectionSubmissionRequest(
        None,
        Some(
          ElectionsSubmissionDetails(fiId = "testFI",
                                     reportingPeriod = reportingPeriod.toString,
                                     crsDetails = None,
                                     fatcaDetails = Some(createFatcaElectionsDetails())
          )
        )
      )

    }

    "should return a giinUpdateRequest and define an electionsSubmissionRequest for Fatca when electionsRequired is true and giin is not present in messageSpecData" in {
      val testGin         = "testGIN"
      val messageSpecData = getMessageSpecData(reportingPeriod = reportingPeriod, messageType = FATCA, electionsRequired = true)

      val userAnswers = emptyUserAnswers
        .withPage(ElectFatcaThresholdsPage, true)
        .withPage(TreasuryRegulationsPage, false)
        .withPage(RequiredGiinPage, testGin)

      val result = GiinAndElectionSubmissionRequest(messageSpecData, userAnswers, fatcaId = "12345")

      result.giinUpdateRequest shouldBe Some(GiinUpdateRequest("12345", "testFI", "testGIN"))
      result.electionsSubmissionRequest.isDefined shouldBe true

      result shouldBe GiinAndElectionSubmissionRequest(
        Some(
          GiinUpdateRequest(
            subscriptionId = "12345",
            fiid = "testFI",
            giin = testGin
          )
        ),
        Some(
          ElectionsSubmissionDetails(fiId = "testFI",
                                     reportingPeriod = reportingPeriod.toString,
                                     crsDetails = None,
                                     fatcaDetails = Some(createFatcaElectionsDetails())
          )
        )
      )
    }

    "should define a giinUpdateRequest and no electionsSubmissionRequest for Fatca when electionsRequired is not required and giin is not present in messageSpecData" in {
      val testGin         = "testGIN"
      val messageSpecData = getMessageSpecData(reportingPeriod = reportingPeriod, messageType = FATCA, electionsRequired = false)

      val userAnswers = emptyUserAnswers
        .withPage(RequiredGiinPage, testGin)

      val result = GiinAndElectionSubmissionRequest(messageSpecData, userAnswers, fatcaId = "12345")

      result.giinUpdateRequest shouldBe Some(GiinUpdateRequest("12345", "testFI", "testGIN"))
      result.electionsSubmissionRequest.isEmpty shouldBe true

      result shouldBe GiinAndElectionSubmissionRequest(
        Some(
          GiinUpdateRequest(
            subscriptionId = "12345",
            fiid = "testFI",
            giin = testGin
          )
        ),
        None
      )
    }

  }
}
