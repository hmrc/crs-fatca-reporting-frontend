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

import models.fileDetails.FileDetailsModel
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}

object FileConfirmationViewModel {

  def getSummaryRows(receivedFileDetails: FileDetailsModel)(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(
        SummaryListRow(
          key = Key(
            content = Text(messages("fileConfirmation.messageRefId.key")),
            classes = "govuk-file-confirmation__key"
          ),
          value = Value(content = Text(receivedFileDetails.messageRefId))
        ),
        SummaryListRow(
          key = Key(
            content = Text(messages("fileConfirmation.messageType.key")),
            classes = "govuk-file-confirmation__key"
          ),
          value = Value(content = Text(receivedFileDetails.messageType))
        ),
        SummaryListRow(
          key = Key(
            content =
              Text(messages(if (receivedFileDetails.isCrsNilReport) "fileConfirmation.financialInstitute.key" else "fileConfirmation.reportingFIName.key")),
            classes = "govuk-file-confirmation__key"
          ),
          value = Value(content = Text(receivedFileDetails.reportingEntityName))
        ),
        SummaryListRow(
          key = Key(
            content = Text(messages("fileConfirmation.fileInformation.key")),
            classes = "govuk-file-confirmation__key"
          ),
          value = Value(content = Text(receivedFileDetails.fileInformation))
        )
      )
    )

  def getEmailParagraphForNonFI(regFirstEmail: String, regSecondEmail: Option[String], fiFirstEmail: Option[String], fiSecondEmail: Option[String]): String =
    (fiFirstEmail, regSecondEmail, fiSecondEmail) match {
      case (Some(fiFirst), Some(regSec), Some(fiSec)) => s"$regFirstEmail, $regSec, $fiFirst and $fiSec"
      case (Some(fiFirst), Some(regSec), None)        => s"$regFirstEmail, $regSec and $fiFirst"
      case (Some(fiFirst), None, Some(fiSec))         => s"$regFirstEmail, $fiFirst and $fiSec"
      case (Some(fiFirst), None, None)                => s"$regFirstEmail and $fiFirst"
      case (None, Some(regSec), Some(fiSec))          => s"$regFirstEmail, $regSec and $fiSec"
      case (None, Some(regSec), None)                 => s"$regFirstEmail and $regSec"
      case (None, None, Some(fiSec))                  => s"$regFirstEmail and $fiSec"
      case (None, None, None)                         => s"$regFirstEmail"
    }

  def getEmailParagraphForFI(regFirstEmail: String, regSecondEmail: Option[String]): String =
    regSecondEmail match {
      case Some(regSecEmail) => s"$regFirstEmail and $regSecEmail"
      case None              => s"$regFirstEmail"
    }

}
