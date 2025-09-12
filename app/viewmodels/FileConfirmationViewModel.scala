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

import models.fileDetails.FileDetails
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}

object FileConfirmationViewModel {

  def getSummaryRows(receivedFileDetails: FileDetails)(implicit messages: Messages): SummaryList =
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
        ),SummaryListRow(
          key = Key(
            content = Text(messages("fileConfirmation.reportingFIName.key")),
            classes = "govuk-file-confirmation__key"
          ),
          value = Value(content = Text(receivedFileDetails.reportingEntityName))
        ),SummaryListRow(
          key = Key(
            content = Text(messages("fileConfirmation.fileInformation.key")),
            classes = "govuk-file-confirmation__key"
          ),
          value = Value(content = Text(receivedFileDetails.fileInformation))
        )
      )
    )

  def getEmailParagraphForNonFI(regFirstEmail: String, regSecondEmail: Option[String],
                                fiFirstEmail: String, fiSecondEmail: Option[String])
                               (implicit messages: Messages): String = {
    (regSecondEmail, fiSecondEmail) match {
      case (Some(regSecEmail), Some(fiSecEmail)) => s"$regFirstEmail, $regSecEmail, $fiFirstEmail and $fiSecEmail"
      case (Some(regSecEmail), None) => s"$regFirstEmail, $regSecEmail and $fiFirstEmail"
      case (None, Some(fiSecEmail)) => s"$regFirstEmail, $fiFirstEmail and $fiSecEmail"
      case (None, None) => s"$regFirstEmail and $fiFirstEmail"
    }
  }

  def getEmailParagraphForFI(regFirstEmail: String, regSecondEmail: Option[String])
                               (implicit messages: Messages): String = {
    regSecondEmail match {
      case Some(regSecEmail) => s"$regFirstEmail and $regSecEmail"
      case None => s"$regFirstEmail"
    }
  }

}
