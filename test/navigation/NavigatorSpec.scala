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

import base.SpecBase
import controllers.routes
import models.*
import pages.*
import pages.elections.crs.*
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from /file-validation" - {

        "to /required-giin" - {
          "when message type is FATCA and GIIN is not held" in {
            val msd         = getMessageSpecData(messageType = FATCA, FATCAReportType.TestData, giin = None)
            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe routes.RequiredGiinController.onPageLoad(NormalMode)
          }
        }
        "to /check-your-file-details" - {
          "when message type is FATCA and GIIN is held and does not require an election " in {
            val msd = getMessageSpecData(messageType = FATCA, FATCAReportType.TestData, giin = Some("giin"), electionsRequired = false)

            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe routes.CheckYourFileDetailsController.onPageLoad()
          }
          "when message type is CRS and does not require an election" in {
            val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, giin = Some("giin"), electionsRequired = false)
            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe routes.CheckYourFileDetailsController.onPageLoad()
          }
        }
        "to /report-elections" - {
          "when message type is FATCA and GIIN is held and requires an election " in {
            val msd         = getMessageSpecData(messageType = FATCA, FATCAReportType.TestData, giin = Some("giin"))
            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode)
          }
          "when message type is CRS and requires an election" in {
            val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData)
            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode)
          }
        }
        "to /file-contains-fatca-void" - {
          "when reportType is VoidReport" in {
            val msd         = getMessageSpecData(messageType = FATCA, FATCAReportType.VoidReport, giin = None)
            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe routes.FileContainsFatcaVoidController.onPageLoad()
          }
        }
      }

      "must go from /required-giin" - {
        "to /report-elections when requires an election" in {
          val msd = getMessageSpecData(messageType = CRS, CRSReportType.TestData)
          val userAnswers = emptyUserAnswers
            .withPage(ValidXMLPage, getValidatedFileData(msd))
            .withPage(RequiredGiinPage, "testGIIN")

          navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode)
        }
        "to /check-your-file-details when elections made already" in {
          val msd = getMessageSpecData(messageType = CRS, CRSReportType.TestData, electionsRequired = false)
          val userAnswers = emptyUserAnswers
            .withPage(ValidXMLPage, getValidatedFileData(msd))
            .withPage(RequiredGiinPage, "testGIIN")

          navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe routes.CheckYourFileDetailsController.onPageLoad()
        }
      }

      "must go from /elections/fatca/thresholds" - {
        "to /check-your-answers when elections made already" in {
          val msd = getMessageSpecData(messageType = FATCA, FATCAReportType.TestData, electionsRequired = false)
          val userAnswers = emptyUserAnswers
            .withPage(ValidXMLPage, getValidatedFileData(msd))
            .withPage(ElectFatcaThresholdsPage, true)

          navigator.nextPage(ElectFatcaThresholdsPage, NormalMode, userAnswers) mustBe routes.CheckYourFileDetailsController.onPageLoad()
        }
      }
      "must go from elections/fatca/us-treasury-regulations page" - {
        "to  /elections/fatca/thresholds" in {
          val msd = getMessageSpecData(FATCA, FATCAReportType.TestData)
          val userAnswers = emptyUserAnswers
            .withPage(ValidXMLPage, getValidatedFileData(msd))
            .withPage(TreasuryRegulationsPage, true)

          navigator.nextPage(TreasuryRegulationsPage, NormalMode, userAnswers) mustBe controllers.elections.fatca.routes.ElectFatcaThresholdsController
            .onPageLoad(NormalMode)
        }
      }

      "must go from elections/crs/dormant-accounts page" - {
        "to  /elections/crs/thresholds" in {
          val msd = getMessageSpecData(messageType = CRS, CRSReportType.TestData)
          val userAnswers = emptyUserAnswers
            .withPage(ValidXMLPage, getValidatedFileData(msd))
            .withPage(DormantAccountsPage, true)

          navigator.nextPage(DormantAccountsPage, NormalMode, userAnswers) mustBe controllers.elections.crs.routes.ThresholdsController.onPageLoad(NormalMode)
        }
      }

      "must go from elections/crs/contracts page" - {
        "to  /elections/crs/dormant-accounts" in {
          val msd = getMessageSpecData(messageType = CRS, CRSReportType.TestData)
          val userAnswers = emptyUserAnswers
            .withPage(ValidXMLPage, getValidatedFileData(msd))
            .withPage(ElectCrsContractPage, true)

          navigator.nextPage(ElectCrsContractPage, NormalMode, userAnswers) mustBe
            controllers.elections.crs.routes.DormantAccountsController.onPageLoad(NormalMode)
        }
      }

      "must go from  /elections/crs/gross-proceeds page" - {
        "to  /check-your-file-details" in {
          val msd = getMessageSpecData(messageType = CRS, CRSReportType.TestData)
          val userAnswers = emptyUserAnswers
            .withPage(ValidXMLPage, getValidatedFileData(msd))
            .withPage(ElectCrsGrossProceedsPage, true)

          navigator.nextPage(ElectCrsGrossProceedsPage, NormalMode, userAnswers) mustBe
            routes.CheckYourFileDetailsController.onPageLoad()
        }
      }

      "must go from elections/crs/thresholds page" - {
        val baseAnswers = emptyUserAnswers.withPage(ThresholdsPage, true)

        "to /elections/crs/gross-proceeds when the reporting period is 2026 or later" in {
          val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(2026, 1, 1))
          val userAnswers = baseAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

          navigator.nextPage(ThresholdsPage, NormalMode, userAnswers) mustBe
            controllers.elections.crs.routes.ElectCrsCarfGrossProceedsController.onPageLoad(NormalMode)
        }

        "to /check-your-file-details when the reporting period is 2025 or earlier" in {
          val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = baseAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

          navigator.nextPage(ThresholdsPage, NormalMode, userAnswers) mustBe
            controllers.routes.CheckYourFileDetailsController.onPageLoad()
        }
      }

      "must go from /report/elections/report-elections" - {
        "to /report/check-your-file-details  when elections are not required" in {
          Seq(CRS, FATCA).foreach {
            regime =>
              val reportType = regime match {
                case CRS   => CRSReportType.TestData
                case FATCA => FATCAReportType.TestData
              }
              val msd         = getMessageSpecData(messageType = regime, reportType, reportingPeriod = LocalDate.of(2025, 12, 31))
              val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd)).withPage(ReportElectionsPage, false)

              navigator.nextPage(ReportElectionsPage, NormalMode, userAnswers) mustBe controllers.routes.CheckYourFileDetailsController.onPageLoad()
          }
        }

        "to /report/elections/crs/contracts when regime is crs and elections are required" in {
          val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd)).withPage(ReportElectionsPage, true)

          navigator.nextPage(ReportElectionsPage, NormalMode, userAnswers) mustBe controllers.elections.crs.routes.ElectCrsContractController
            .onPageLoad(NormalMode)
        }

        "to /report/elections/fatca/us-treasury-regulations  when regime is fatca and elections are required" in {
          val msd         = getMessageSpecData(messageType = FATCA, FATCAReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd)).withPage(ReportElectionsPage, true)

          navigator.nextPage(ReportElectionsPage, NormalMode, userAnswers) mustBe controllers.elections.fatca.routes.TreasuryRegulationsController
            .onPageLoad(NormalMode)
        }
      }
    }

    "must go from elections/crs/carf-gross-proceeds page" - {
      "to /elections/crs/gross-proceeds when the answer is Yes" in {
        val userAnswers = emptyUserAnswers.withPage(ElectCrsCarfGrossProceedsPage, true)

        navigator.nextPage(ElectCrsCarfGrossProceedsPage, NormalMode, userAnswers) mustBe
          controllers.elections.crs.routes.ElectCrsGrossProceedsController.onPageLoad(NormalMode)
      }

      "to /check-your-file-details when the answer is No" in {
        val userAnswers = emptyUserAnswers.withPage(ElectCrsCarfGrossProceedsPage, false)

        navigator.nextPage(ElectCrsCarfGrossProceedsPage, NormalMode, userAnswers) mustBe
          routes.CheckYourFileDetailsController.onPageLoad()
      }

      "to Journey Recovery if the answer is missing" in {
        val userAnswers = emptyUserAnswers

        navigator.nextPage(ElectCrsCarfGrossProceedsPage, NormalMode, userAnswers) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check mode" - {
      "must go from /report/change-required-giin page" - {
        "to /check-your-file-details on submission" in {
          val userAnswers = emptyUserAnswers.withPage(RequiredGiinPage, "some-giin")

          navigator.nextPage(RequiredGiinPage, CheckMode, userAnswers) mustBe
            routes.CheckYourFileDetailsController.onPageLoad()
        }
      }

      "must go from /report/elections/report-elections" - {
        "to /report/check-your-file-details  when elections are not required" in {
          Seq(CRS, FATCA).foreach {
            regime =>
              val reportType = regime match {
                case CRS   => CRSReportType.TestData
                case FATCA => FATCAReportType.TestData
              }
              val msd         = getMessageSpecData(messageType = regime, reportType, reportingPeriod = LocalDate.of(2025, 12, 31))
              val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd)).withPage(ReportElectionsPage, false)

              navigator.nextPage(ReportElectionsPage, CheckMode, userAnswers) mustBe controllers.routes.CheckYourFileDetailsController.onPageLoad()
          }
        }

        "to /report/elections/crs/contracts when regime is crs and elections are required" in {
          val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd)).withPage(ReportElectionsPage, true)

          navigator.nextPage(ReportElectionsPage, CheckMode, userAnswers) mustBe controllers.elections.crs.routes.ElectCrsContractController
            .onPageLoad(NormalMode)
        }

        "to /report/elections/fatca/us-treasury-regulations  when regime is fatca and elections are required" in {
          val msd         = getMessageSpecData(messageType = FATCA, FATCAReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd)).withPage(ReportElectionsPage, true)

          navigator.nextPage(ReportElectionsPage, CheckMode, userAnswers) mustBe controllers.elections.fatca.routes.TreasuryRegulationsController
            .onPageLoad(NormalMode)
        }
      }

      "must go from /report/elections/fatca/us-treasury-regulations" - {
        "to /report/check-your-file-details" in {
          val baseAnswers = emptyUserAnswers.withPage(TreasuryRegulationsPage, true)

          val msd         = getMessageSpecData(messageType = FATCA, FATCAReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = baseAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

          navigator.nextPage(TreasuryRegulationsPage, CheckMode, userAnswers) mustBe controllers.routes.CheckYourFileDetailsController.onPageLoad()
        }
      }

      "must go from /report/elections/fatca/change-thresholds" - {
        "to /report/check-your-file-details" in {
          val baseAnswers = emptyUserAnswers.withPage(ElectFatcaThresholdsPage, true)

          val msd         = getMessageSpecData(messageType = FATCA, FATCAReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = baseAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

          navigator.nextPage(ElectFatcaThresholdsPage, CheckMode, userAnswers) mustBe controllers.routes.CheckYourFileDetailsController.onPageLoad()
        }
      }

      "must go from /report/elections/crs/change-contracts" - {
        "to /report/check-your-file-details" in {
          val baseAnswers = emptyUserAnswers.withPage(ElectCrsContractPage, true)

          val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = baseAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

          navigator.nextPage(ElectCrsContractPage, CheckMode, userAnswers) mustBe controllers.routes.CheckYourFileDetailsController.onPageLoad()
        }
      }

      "must go from /report/elections/crs/dormant-accounts" - {
        "to /report/check-your-file-details" in {
          val baseAnswers = emptyUserAnswers.withPage(DormantAccountsPage, true)

          val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = baseAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

          navigator.nextPage(DormantAccountsPage, CheckMode, userAnswers) mustBe controllers.routes.CheckYourFileDetailsController.onPageLoad()
        }
      }

      "must go from /report/elections/crs/thresholds" - {
        "to /report/check-your-file-details" in {
          val baseAnswers = emptyUserAnswers.withPage(ThresholdsPage, true)

          val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = baseAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

          navigator.nextPage(ThresholdsPage, CheckMode, userAnswers) mustBe controllers.routes.CheckYourFileDetailsController.onPageLoad()
        }
      }

      "must go from /report/elections/crs/carf-gross-proceeds" - {
        "to /report/check-your-file-details when ElectCrsCarfGrossProceedsPage is false" in {
          val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd)).withPage(ElectCrsCarfGrossProceedsPage, false)

          navigator.nextPage(ElectCrsCarfGrossProceedsPage, CheckMode, userAnswers) mustBe controllers.routes.CheckYourFileDetailsController.onPageLoad()
        }

        "to /report/elections/crs/gross-proceeds when ElectCrsCarfGrossProceedsPage is true" in {
          val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd)).withPage(ElectCrsCarfGrossProceedsPage, true)

          navigator.nextPage(ElectCrsCarfGrossProceedsPage, CheckMode, userAnswers) mustBe controllers.elections.crs.routes.ElectCrsGrossProceedsController
            .onPageLoad(NormalMode)
        }
      }

      "must go from /report/elections/crs/gross-proceeds" - {
        "to /report/check-your-file-details" in {
          val baseAnswers = emptyUserAnswers.withPage(ElectCrsGrossProceedsPage, true)

          val msd         = getMessageSpecData(messageType = CRS, CRSReportType.TestData, reportingPeriod = LocalDate.of(2025, 12, 31))
          val userAnswers = baseAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

          navigator.nextPage(ElectCrsGrossProceedsPage, CheckMode, userAnswers) mustBe controllers.routes.CheckYourFileDetailsController.onPageLoad()
        }
      }

      "must go from a page that doesn't exist in the edit route map to Index controller" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }
    }
  }
}
