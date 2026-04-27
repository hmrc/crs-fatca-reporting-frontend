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

package viewmodels

import base.SpecBase
import models.fileDetails.{FileErrors, FileValidationErrors}
import models.submission.fileDetails.{Accepted, NotAccepted, Pending, Rejected, RejectedSDES, RejectedSDESVirus}

import java.time.LocalDateTime

class SubmissionChecksTableViewModelSpec extends SpecBase {
  "SubmissionChecksTableViewModel" - {

    "return correct status text" in {
      SubmissionChecksTableViewModel.status(Pending) mustBe "Pending"
      SubmissionChecksTableViewModel.status(Accepted) mustBe "Passed"
      SubmissionChecksTableViewModel.status(NotAccepted) mustBe "Problem"
      SubmissionChecksTableViewModel.status(RejectedSDES) mustBe "Problem"
      SubmissionChecksTableViewModel.status(RejectedSDESVirus) mustBe "Failed"
      val validationErrorWithNotAcceptedCodeFatca =
        FileValidationErrors(Some(List(FileErrors(models.fileDetails.BusinessRuleErrorCode.FailedSchemaValidationFatca, None))), None)
      SubmissionChecksTableViewModel.status(Rejected(validationErrorWithNotAcceptedCodeFatca)) mustBe "Problem"
      SubmissionChecksTableViewModel.status(Rejected(FileValidationErrors(None, None))) mustBe "Failed"
    }

    "return correct next step link" in {
      SubmissionChecksTableViewModel.nextStepLink(Pending) mustBe NextStepLink.NoLink
      SubmissionChecksTableViewModel.nextStepLink(Accepted) mustBe NextStepLink.GotoConfirmation
      SubmissionChecksTableViewModel.nextStepLink(NotAccepted) mustBe NextStepLink.ContactUs
      SubmissionChecksTableViewModel.nextStepLink(RejectedSDES) mustBe NextStepLink.UploadFileAgain
      SubmissionChecksTableViewModel.nextStepLink(RejectedSDESVirus) mustBe NextStepLink.VirusFound

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
