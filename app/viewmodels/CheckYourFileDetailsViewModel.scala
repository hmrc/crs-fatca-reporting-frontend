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

package viewmodels

import controllers.routes
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

object CheckYourFileDetailsViewModel {

  def getYourFileDetailsRows()(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(
        SummaryListRow(
          key = Key(content = Text(messages("checkYourFileDetails.fileId.key"))),
          value = Value(content = Text("MyFATCAReportMessageRefId234567890LONGONGLONGLONGLONG")),
          classes = "no-border-bottom"
        ),
        SummaryListRow(
          key = Key(content = Text(messages("checkYourFileDetails.reportingRegime.key"))),
          value = Value(content = Text("CRS")),
          classes = "no-border-bottom"
        ),
        SummaryListRow(
          key = Key(content = Text(messages("checkYourFileDetails.fiId.key"))),
          value = Value(content = Text("ABC00000124")),
          classes = "no-border-bottom"
        ),
        SummaryListRow(
          key = Key(content = Text(messages("checkYourFileDetails.financialInstitution.key"))),
          value = Value(content = Text("EFG Bank plc")),
          classes = "no-border-bottom"
        ),
        SummaryListRow(
          key = Key(content = Text(messages("File information"))),
          value = Value(content = Text("New information")),
          actions = Some(
            Actions(
              items = Seq(
                ActionItem(
                  href = routes.IndexController.onPageLoad().url,
                  content = Text(messages("checkYourFileDetails.fileInformation.change")),
                  visuallyHiddenText = Some(messages("checkYourFileDetails.fileInformation.change"))
                )
              )
            )
          )
        )
      )
    )
}
