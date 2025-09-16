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
import models.fileDetails.FileDetails
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}

import java.time.LocalDateTime

class FileConfirmationViewModelSpec extends SpecBase {
  "FileConfirmationViewModel" - {

    ".getSummaryList" - {
      "must return the getSummaryList" in {
        val fileDetails = FileDetails("name.xml", "c-8-new-f-va", "CRS", "EFG Bank plc", "New information", LocalDateTime.now(), LocalDateTime.now())
        val expectedSummary = SummaryList(
          List(
            SummaryListRow(Key(Text("File ID (MessageRefId)"), "govuk-file-confirmation__key"), Value(Text("c-8-new-f-va"), ""), "", None),
            SummaryListRow(Key(Text("Reporting regime (MessageType)"), "govuk-file-confirmation__key"), Value(Text("CRS"), ""), "", None),
            SummaryListRow(Key(Text("Financial institution (ReportingFI Name)"), "govuk-file-confirmation__key"), Value(Text("EFG Bank plc"), ""), "", None),
            SummaryListRow(Key(Text("File information"), "govuk-file-confirmation__key"), Value(Text("New information"), ""), "", None)
          ),
          None,
          "",
          Map()
        )

        FileConfirmationViewModel.getSummaryRows(fileDetails)(messages(app)) mustBe expectedSummary
      }
    }

    ".getEmailParagraphForNonFI" - {
      "must return paragraph with all emails" in {

        val formattedString =
          FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com", Some("user2@email.com"), "fiuser1@email.com", Some("fiuser2@email.com"))
        formattedString mustEqual "user1@email.com, user2@email.com, fiuser1@email.com and fiuser2@email.com"
      }

      "must return paragraph with 2 user emails & 1 fi user email" in {

        val formattedString = FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com", Some("user2@email.com"), "fiuser1@email.com", None)
        formattedString mustEqual "user1@email.com, user2@email.com and fiuser1@email.com"
      }

      "must return paragraph with 1 user emails & 2 fi user email" in {

        val formattedString = FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com", None, "fiuser1@email.com", Some("fiuser2@email.com"))
        formattedString mustEqual "user1@email.com, fiuser1@email.com and fiuser2@email.com"
      }

      "must return paragraph with 1 user emails & 1 fi user email" in {

        val formattedString = FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com", None, "fiuser1@email.com", None)
        formattedString mustEqual "user1@email.com and fiuser1@email.com"
      }
    }

    ".getEmailParagraphForFI" - {
      "must return paragraph with all emails" in {

        val formattedString = FileConfirmationViewModel.getEmailParagraphForFI("user1@email.com", Some("user2@email.com"))
        formattedString mustEqual "user1@email.com and user2@email.com"
      }

      "must return paragraph with 1 user emails" in {

        val formattedString = FileConfirmationViewModel.getEmailParagraphForFI("user1@email.com", None)
        formattedString mustEqual "user1@email.com"
      }
    }

  }
}
