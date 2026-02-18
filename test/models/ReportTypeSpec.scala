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

package models

import base.SpecBase

class ReportTypeSpec extends SpecBase {

  "messageKeyForReportType" - {
    "CRSReportType" - {
      "must return the correct message key for each CRSReportType value" in {
        messageKeyForReportType(CRSReportType.TestData) mustBe "reportType.crs.testData"
        messageKeyForReportType(CRSReportType.CorrectedInformationForExistingReport) mustBe "reportType.crs.correctedInformationForExistingReport"
        messageKeyForReportType(CRSReportType.NilReport) mustBe "reportType.crs.nilReport"
        messageKeyForReportType(CRSReportType.NewInformation) mustBe "reportType.crs.newInformation"
        messageKeyForReportType(
          CRSReportType.CorrectedAndDeletedInformationForExistingReport
        ) mustBe "reportType.crs.correctedAndDeletedInformationForExistingReport"
        messageKeyForReportType(CRSReportType.DeletedInformationForExistingReport) mustBe "reportType.crs.deletedInformationForExistingReport"
        messageKeyForReportType(CRSReportType.DeletionOfExistingReport) mustBe "reportType.crs.deletionOfExistingReport"
        messageKeyForReportType(CRSReportType.AdditionalInformationForExistingReport) mustBe "reportType.crs.additionalInformationForExistingReport"
      }
    }
    "FATCAReportType" - {
      "must return the correct message key for each FATCAReportType value" in {
        messageKeyForReportType(FATCAReportType.TestData) mustBe "reportType.fatca.testData"
        messageKeyForReportType(FATCAReportType.NilReport) mustBe "reportType.fatca.nilReport"
        messageKeyForReportType(FATCAReportType.NewInformation) mustBe "reportType.fatca.newInformation"
        messageKeyForReportType(FATCAReportType.CorrectedInformationForExistingReport) mustBe "reportType.fatca.correctedInformationForExistingReport"
        messageKeyForReportType(FATCAReportType.AmendedInformationForExistingReport) mustBe "reportType.fatca.amendedInformationForExistingReport"
      }
    }

  }

  "requiresWarningMessage" - {
    "CRSReportType" - {
      "must return true for CRSReportType values that require a warning message" in {
        requiresWarningMessage(CRSReportType.TestData) mustBe true
        requiresWarningMessage(CRSReportType.DeletedInformationForExistingReport) mustBe true
        requiresWarningMessage(CRSReportType.DeletionOfExistingReport) mustBe true
        requiresWarningMessage(CRSReportType.CorrectedInformationForExistingReport) mustBe true
        requiresWarningMessage(CRSReportType.CorrectedAndDeletedInformationForExistingReport) mustBe true
      }

      "must return false for CRSReportType values that do not require a warning message" in {
        requiresWarningMessage(CRSReportType.NilReport) mustBe false
        requiresWarningMessage(CRSReportType.NewInformation) mustBe false
        requiresWarningMessage(CRSReportType.AdditionalInformationForExistingReport) mustBe false
      }
    }
    "FATCAReportType" - {
      "must return true for FATCAReportType values that require a warning message" in {
        requiresWarningMessage(FATCAReportType.TestData) mustBe true
        requiresWarningMessage(FATCAReportType.CorrectedInformationForExistingReport) mustBe true
        requiresWarningMessage(FATCAReportType.AmendedInformationForExistingReport) mustBe true
      }

      "must return false for FATCAReportType values that do not require a warning message" in {
        requiresWarningMessage(FATCAReportType.NilReport) mustBe false
        requiresWarningMessage(FATCAReportType.NewInformation) mustBe false
        requiresWarningMessage(FATCAReportType.VoidReport) mustBe false
      }
    }
  }

  "messageKeyForReportTypeWithWarning" - {
    "CRSReportType" - {
      "must return the correct message key for each CRSReportType value that requires a warning message" in {
        messageKeyForReportTypeWithWarning(CRSReportType.TestData) mustBe "reportType.crs.testData.warning"
        messageKeyForReportTypeWithWarning(
          CRSReportType.DeletedInformationForExistingReport
        ) mustBe "reportType.crs.deletedInformationForExistingReport.warning"
        messageKeyForReportTypeWithWarning(CRSReportType.DeletionOfExistingReport) mustBe "reportType.crs.deletionOfExistingReport.warning"
        messageKeyForReportTypeWithWarning(
          CRSReportType.CorrectedInformationForExistingReport
        ) mustBe "reportType.crs.correctedInformationForExistingReport.warning"
        messageKeyForReportTypeWithWarning(
          CRSReportType.CorrectedAndDeletedInformationForExistingReport
        ) mustBe "reportType.crs.correctedAndDeletedInformationForExistingReport.warning"
      }
    }

    "FATCAReportType" - {
      "must return the correct message key for each FATCAReportType value that requires a warning message" in {
        messageKeyForReportTypeWithWarning(FATCAReportType.TestData) mustBe "reportType.fatca.testData.warning"
        messageKeyForReportTypeWithWarning(
          FATCAReportType.CorrectedInformationForExistingReport
        ) mustBe "reportType.fatca.correctedInformationForExistingReport.warning"
        messageKeyForReportTypeWithWarning(
          FATCAReportType.AmendedInformationForExistingReport
        ) mustBe "reportType.fatca.amendedInformationForExistingReport.warning"
      }
    }
  }
}
