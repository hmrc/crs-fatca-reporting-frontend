package viewmodels

import base.SpecBase
import models.fileDetails.{FileErrors, FileValidationErrors}
import models.submission.fileDetails.{Accepted, NotAccepted, Pending, Rejected, RejectedSDES, RejectedSDESVirus}

import java.time.LocalDateTime

class SubmissionChecksTableViewModelSpec extends SpecBase {
  "SubmissionChecksTableViewModel" - {

    "return correct status text" - {
      SubmissionChecksTableViewModel.status(Pending) mustBe "Pending"
      SubmissionChecksTableViewModel.status(Accepted) mustBe "Passed"
      SubmissionChecksTableViewModel.status(NotAccepted) mustBe "Problem"
      SubmissionChecksTableViewModel.status(RejectedSDES) mustBe "Problem"
      SubmissionChecksTableViewModel.status(RejectedSDESVirus) mustBe "Problem"
      SubmissionChecksTableViewModel.status(Rejected(FileValidationErrors(None, None))) mustBe "Failed"
    }

    "return correct next step link" - {
      SubmissionChecksTableViewModel.nextStepLink(Pending) mustBe NextStepLink.NoLink
      SubmissionChecksTableViewModel.nextStepLink(Accepted) mustBe NextStepLink.GotoConfirmation
      SubmissionChecksTableViewModel.nextStepLink(NotAccepted) mustBe NextStepLink.ContactUs
      SubmissionChecksTableViewModel.nextStepLink(RejectedSDES) mustBe NextStepLink.UploadFileAgain
      SubmissionChecksTableViewModel.nextStepLink(RejectedSDESVirus) mustBe NextStepLink.UploadFileAgain

      val validationErrorWithNotAcceptedCodeCRS =
        FileValidationErrors(Some(List(FileErrors(models.fileDetails.BusinessRuleErrorCode.FailedSchemaValidationCrs, None))), None)
      SubmissionChecksTableViewModel.nextStepLink(Rejected(validationErrorWithNotAcceptedCodeCRS)) mustBe NextStepLink.ContactUs

      val validationErrorWithNotAcceptedCodeFatca =
        FileValidationErrors(Some(List(FileErrors(models.fileDetails.BusinessRuleErrorCode.FailedSchemaValidationFatca, None))), None)
      SubmissionChecksTableViewModel.nextStepLink(Rejected(validationErrorWithNotAcceptedCodeFatca)) mustBe NextStepLink.ContactUs

      val validationErrorWithoutNotAcceptedCode =
        FileValidationErrors(Some(List(FileErrors(models.fileDetails.BusinessRuleErrorCode.CRSFailedThreatScan, None))), None)
      SubmissionChecksTableViewModel.nextStepLink(Rejected(validationErrorWithoutNotAcceptedCode)) mustBe NextStepLink.CheckErrors
    }

    "return correctly formatted sent date" in {
      val dateTime = LocalDateTime.of(2026, 1, 6, 12, 0, 0)
      SubmissionChecksTableViewModel.sent(dateTime) mustBe "6 Jan 2026 12:00pm"
    }

    "return correct reporting period year" in {
      val date = LocalDateTime.of(2026, 1, 6, 12, 0, 0).toLocalDate
      SubmissionChecksTableViewModel.reportingPeriod(date) mustBe 2026
    }
  }
}
