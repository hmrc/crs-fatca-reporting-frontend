package models

import base.SpecBase

class MessageKeyForReportTypeSpec extends SpecBase {

  "ReportTypeMessageKey" - {
    "CRSReportType" - {
      "must return the correct message key for each CRSReportType value" in {
        messageKeyForReportType(CRSReportType.TestData) mustBe "reportType.testData"
        messageKeyForReportType(CRSReportType.NilReport) mustBe "reportType.nilReport"
        messageKeyForReportType(CRSReportType.NewInformation) mustBe "reportType.newInformation"
        messageKeyForReportType(CRSReportType.CorrectedAndDeletedInformationForExistingReport) mustBe "reportType.correctedAndDeletedInformationForExistingReport"
        messageKeyForReportType(CRSReportType.DeletedInformationForExistingReport) mustBe "reportType.deletedInformationForExistingReport"
        messageKeyForReportType(CRSReportType.DeletionOfExistingReport) mustBe "reportType.deletionOfExistingReport"
        messageKeyForReportType(CRSReportType.AdditionalInformationForExistingReport) mustBe "reportType.additionalInformationForExistingReport"
      }
    }

  }
}
