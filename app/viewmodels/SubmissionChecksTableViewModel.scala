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

import models.fileDetails.BusinessRuleErrorCode.{FailedSchemaValidationCrs, FailedSchemaValidationFatca}
import models.fileDetails.FileDetailsResult
import models.submission.fileDetails.{Accepted, FileStatus, NotAccepted, Pending, Rejected, RejectedSDES, RejectedSDESVirus}
import viewmodels.NextStepLink.GotoConfirmation

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import java.util.Locale

enum NextStepLink:
  case GotoConfirmation, CheckErrors, ContactUs, UploadFileAgain, NoLink

case class SubmissionChecksTableViewModel(fileDetailsResults: FileDetailsResult)

object SubmissionChecksTableViewModel {

  def sent(ldt: LocalDateTime): String = {
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy h:mma", Locale.ENGLISH)
    ldt.format(formatter).replace("AM", "am").replace("PM", "pm")
  }

  def reportingPeriod(ld: LocalDate): Int = ld.getYear

  def status(fileStatus: FileStatus): String = fileStatus match {
    case Pending     => "Pending"
    case Accepted    => "Passed"
    case Rejected(_) => "Failed"
    case _           => "Problem"
  }

  def nextStepLink(fileStatus: FileStatus): NextStepLink = fileStatus match {
    case Accepted => GotoConfirmation
    case Rejected(validationError) =>
      val notAcceptedErrorCodes = Set(FailedSchemaValidationCrs, FailedSchemaValidationFatca)
      val isNotAccepted = validationError.fileError
        .getOrElse(Nil)
        .exists(
          e => notAcceptedErrorCodes(e.code)
        )

      if (isNotAccepted) NextStepLink.ContactUs else NextStepLink.CheckErrors

    case RejectedSDES | RejectedSDESVirus => NextStepLink.UploadFileAgain
    case NotAccepted                      => NextStepLink.ContactUs
    case Pending                          => NextStepLink.NoLink
  }
}
