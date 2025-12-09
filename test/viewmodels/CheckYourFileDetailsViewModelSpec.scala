/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels

import base.SpecBase
import controllers.routes
import models.{CRS, CheckMode, FATCA, MessageSpecData, ValidatedFileData}
import pages.elections.crs.*
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}
import pages.{ReportElectionsPage, RequiredGiinPage, ValidXMLPage}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}

import java.time.LocalDate

class CheckYourFileDetailsViewModelSpec extends SpecBase {

  def crsValidatedFileData(year: Int) = getValidatedFileData(getMessageSpecData(CRS, reportingPeriod = LocalDate.of(year, 1, 1)))

  def fatcaValidatedFileData(year: Int) =
    getValidatedFileData(getMessageSpecData(FATCA, reportingPeriod = LocalDate.of(year, 1, 1)))

  "CheckYourFileDetailsViewModel" - {

    ".fileDetailsSummary" - {
      "must return the getSummaryList for File Details when No Election Required for CRS" in {
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(Key(Text("File ID (MessageRefId)")), Value(Text("testRefId")), "no-border-bottom", None),
            SummaryListRow(Key(Text("Reporting regime (MessageType)")), Value(Text("CRS")), "no-border-bottom", None),
            SummaryListRow(Key(Text("FI ID (SendingCompanyIN)")), Value(Text("testFI")), "no-border-bottom", None),
            SummaryListRow(Key(Text("Financial institution (ReportingFI Name)")), Value(Text("testReportingName")), "no-border-bottom", None),
            SummaryListRow(
              Key(Text("File information")),
              Value(Text("New information")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = routes.IndexController.onPageLoad().url,
                      content = Text("Change file"),
                      visuallyHiddenText = Some("Change file")
                    )
                  )
                )
              )
            )
          ),
          None,
          "",
          Map()
        )
        val reportingPeriodYear = 2025
        val userAnswers         = emptyUserAnswers.withPage(ValidXMLPage, crsValidatedFileData(reportingPeriodYear))
        val modelHelper         = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
        modelHelper.fileDetailsSummary mustBe expectedSummary
      }
      "must return the getSummaryList for File Details when No Election Required for FATCA" in {
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(Key(Text("File ID (MessageRefId)")), Value(Text("testRefId")), "no-border-bottom", None),
            SummaryListRow(Key(Text("Reporting regime (MessageType)")), Value(Text("FATCA")), "no-border-bottom", None),
            SummaryListRow(Key(Text("FI ID (SendingCompanyIN)")), Value(Text("testFI")), "no-border-bottom", None),
            SummaryListRow(Key(Text("Financial institution (ReportingFI Name)")), Value(Text("testReportingName")), "no-border-bottom", None),
            SummaryListRow(
              Key(Text("File information")),
              Value(Text("New information")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = routes.IndexController.onPageLoad().url,
                      content = Text("Change file"),
                      visuallyHiddenText = Some("Change file")
                    )
                  )
                )
              )
            )
          ),
          None,
          "",
          Map()
        )

        val reportingPeriodYear = 2025
        val userAnswers         = emptyUserAnswers.withPage(ValidXMLPage, fatcaValidatedFileData(reportingPeriodYear))
        val modelHelper         = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
        modelHelper.fileDetailsSummary mustBe expectedSummary
      }
    }

    ".financialInstitutionDetailsSummary" - {
      "must return the getSummaryList for FIDetails when report Election is false for CRS" in {
        val reportingPeriodYear = 2025
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(
              Key(Text(s"Do you want to make any elections for the CRS reporting period $reportingPeriodYear?")),
              Value(Text("No")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.routes.ReportElectionsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if you want to make any elections for CRS")
                    )
                  )
                )
              )
            )
          ),
          None,
          "",
          Map()
        )
        val userAnswers = emptyUserAnswers
          .withPage(ValidXMLPage, crsValidatedFileData(reportingPeriodYear))
          .withPage(ReportElectionsPage, false)
        val modelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
        modelHelper.financialInstitutionDetailsSummary mustBe expectedSummary
      }
      "must return the getSummaryList for FIDetails when report Election is true and reporting period lesser than 2026 for CRS" in {
        val reportingPeriodYear = 2025
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(
              Key(Text(s"Do you want to make any elections for the CRS reporting period $reportingPeriodYear?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.routes.ReportElectionsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if you want to make any elections for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they excluding cash value insurance contracts or annuity contracts from their reporting for CRS?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.ElectCrsContractController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are excluding cash value insurance contracts or annuity contracts from their reporting for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they treating dormant accounts as not being reportable accounts for CRS?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.DormantAccountsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are treating dormant accounts as not being reportable accounts for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they applying thresholds to any of their accounts in their due diligence process for CRS?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.ThresholdsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are applying thresholds to any of their accounts in their due diligence process for CRS")
                    )
                  )
                )
              )
            )
          ),
          None,
          "",
          Map()
        )

        val userAnswers = emptyUserAnswers
          .withPage(ValidXMLPage, crsValidatedFileData(reportingPeriodYear))
          .withPage(ReportElectionsPage, true)
          .withPage(ElectCrsContractPage, true)
          .withPage(DormantAccountsPage, true)
          .withPage(ThresholdsPage, true)
        val modelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
        modelHelper.financialInstitutionDetailsSummary mustBe expectedSummary
      }
      "must return the getSummaryList for FIDetails when report Election is true and reporting period is 2026 And Carf Gross Proceed is false for CRS" in {
        val reportingPeriodYear = 2026
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(
              Key(Text(s"Do you want to make any elections for the CRS reporting period $reportingPeriodYear?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.routes.ReportElectionsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if you want to make any elections for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they excluding cash value insurance contracts or annuity contracts from their reporting for CRS?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.ElectCrsContractController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are excluding cash value insurance contracts or annuity contracts from their reporting for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they treating dormant accounts as not being reportable accounts for CRS?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.DormantAccountsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are treating dormant accounts as not being reportable accounts for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they applying thresholds to any of their accounts in their due diligence process for CRS?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.ThresholdsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are applying thresholds to any of their accounts in their due diligence process for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they reporting gross proceeds under the Cryptoasset Reporting Framework (CARF) for 2026?")),
              Value(Text("No")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.ElectCrsCarfGrossProceedsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are reporting gross proceeds under the Cryptoasset Reporting Framework (CARF) for 2026")
                    )
                  )
                )
              )
            )
          ),
          None,
          "",
          Map()
        )
        val userAnswers = emptyUserAnswers
          .withPage(ValidXMLPage, crsValidatedFileData(reportingPeriodYear))
          .withPage(ReportElectionsPage, true)
          .withPage(ElectCrsContractPage, true)
          .withPage(DormantAccountsPage, true)
          .withPage(ThresholdsPage, true)
          .withPage(ElectCrsCarfGrossProceedsPage, false)
        val modelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
        modelHelper.financialInstitutionDetailsSummary mustBe expectedSummary
      }
      "must return the getSummaryList for FIDetails when report Election is true and reporting period is 2026 And Carf Gross Proceed is true for CRS" in {
        val reportingPeriodYear = 2026
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(
              Key(Text(s"Do you want to make any elections for the CRS reporting period $reportingPeriodYear?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.routes.ReportElectionsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if you want to make any elections for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they excluding cash value insurance contracts or annuity contracts from their reporting for CRS?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.ElectCrsContractController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are excluding cash value insurance contracts or annuity contracts from their reporting for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they treating dormant accounts as not being reportable accounts for CRS?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.DormantAccountsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are treating dormant accounts as not being reportable accounts for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they applying thresholds to any of their accounts in their due diligence process for CRS?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.ThresholdsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are applying thresholds to any of their accounts in their due diligence process for CRS")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they reporting gross proceeds under the Cryptoasset Reporting Framework (CARF) for 2026?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.ElectCrsCarfGrossProceedsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are reporting gross proceeds under the Cryptoasset Reporting Framework (CARF) for 2026")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they also reporting these gross proceeds for the CARF under CRS?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.crs.routes.ElectCrsGrossProceedsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText =
                        Some("Change if they are also reporting these gross proceeds for the Cryptoasset Reporting Framework (CARF) under CRS")
                    )
                  )
                )
              )
            )
          ),
          None,
          "",
          Map()
        )
        val userAnswers = emptyUserAnswers
          .withPage(ValidXMLPage, crsValidatedFileData(reportingPeriodYear))
          .withPage(ReportElectionsPage, true)
          .withPage(ElectCrsContractPage, true)
          .withPage(DormantAccountsPage, true)
          .withPage(ThresholdsPage, true)
          .withPage(ElectCrsCarfGrossProceedsPage, true)
          .withPage(ElectCrsGrossProceedsPage, true)
        val modelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
        modelHelper.financialInstitutionDetailsSummary mustBe expectedSummary
      }
      "must return the getSummaryList for FIDetails when report Election is false for FATCA" in {
        val reportingPeriodYear = 2025
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(
              Key(Text(s"Do you want to make any elections for the FATCA reporting period $reportingPeriodYear?")),
              Value(Text("No")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.routes.ReportElectionsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if you want to make any elections for FATCA")
                    )
                  )
                )
              )
            )
          ),
          None,
          "",
          Map()
        )
        val userAnswers = emptyUserAnswers
          .withPage(ValidXMLPage, fatcaValidatedFileData(reportingPeriodYear))
          .withPage(ReportElectionsPage, false)
        val modelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
        modelHelper.financialInstitutionDetailsSummary mustBe expectedSummary
      }
      "must return the getSummaryList for FIDetails when report Election is true for FATCA and No GIIN required" in {
        val reportingPeriodYear = 2025
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(
              Key(Text(s"Do you want to make any elections for the FATCA reporting period $reportingPeriodYear?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.routes.ReportElectionsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if you want to make any elections for FATCA")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they using due diligence procedures from US Treasury Regulations for FATCA?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.fatca.routes.TreasuryRegulationsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are using due diligence procedures from US Treasury Regulations for FATCA")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they applying thresholds to any of their accounts in their due diligence process for FATCA?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.fatca.routes.ElectFatcaThresholdsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are applying thresholds to any of their accounts in their due diligence process for FATCA")
                    )
                  )
                )
              )
            )
          ),
          None,
          "",
          Map()
        )
        val userAnswers = emptyUserAnswers
          .withPage(ValidXMLPage, fatcaValidatedFileData(reportingPeriodYear))
          .withPage(ReportElectionsPage, true)
          .withPage(TreasuryRegulationsPage, true)
          .withPage(ElectFatcaThresholdsPage, true)
        val modelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
        modelHelper.financialInstitutionDetailsSummary mustBe expectedSummary
      }
      "must return the getSummaryList for FIDetails when report Election is true for FATCA and GIIN required" in {
        val reportingPeriodYear = 2025
        val testGIINValue       = "testGIINValue"
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(
              Key(Text("Global Intermediary Identification Number")),
              Value(Text(testGIINValue)),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = routes.RequiredGiinController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change the Global Intermediary Identification Number")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text(s"Do you want to make any elections for the FATCA reporting period $reportingPeriodYear?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.routes.ReportElectionsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if you want to make any elections for FATCA")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they using due diligence procedures from US Treasury Regulations for FATCA?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.fatca.routes.TreasuryRegulationsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are using due diligence procedures from US Treasury Regulations for FATCA")
                    )
                  )
                )
              )
            ),
            SummaryListRow(
              Key(Text("Are they applying thresholds to any of their accounts in their due diligence process for FATCA?")),
              Value(Text("Yes")),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = controllers.elections.fatca.routes.ElectFatcaThresholdsController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change if they are applying thresholds to any of their accounts in their due diligence process for FATCA")
                    )
                  )
                )
              )
            )
          ),
          None,
          "",
          Map()
        )
        val userAnswers = emptyUserAnswers
          .withPage(ValidXMLPage, fatcaValidatedFileData(reportingPeriodYear))
          .withPage(RequiredGiinPage, testGIINValue)
          .withPage(ReportElectionsPage, true)
          .withPage(TreasuryRegulationsPage, true)
          .withPage(ElectFatcaThresholdsPage, true)
        val modelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
        modelHelper.financialInstitutionDetailsSummary mustBe expectedSummary
      }
      "must return the getSummaryList for FIDetails when GIIN required and no report election" in {
        val reportingPeriodYear = 2025
        val testGIINValue       = "testGIINValue"
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(
              Key(Text("Global Intermediary Identification Number")),
              Value(Text(testGIINValue)),
              "",
              Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = routes.RequiredGiinController.onPageLoad(CheckMode).url,
                      content = Text("Change"),
                      visuallyHiddenText = Some("Change the Global Intermediary Identification Number")
                    )
                  )
                )
              )
            )
          ),
          None,
          "",
          Map()
        )
        val userAnswers = emptyUserAnswers
          .withPage(ValidXMLPage, fatcaValidatedFileData(reportingPeriodYear))
          .withPage(RequiredGiinPage, testGIINValue)
        val modelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
        modelHelper.financialInstitutionDetailsSummary mustBe expectedSummary
      }
    }

  }
}
