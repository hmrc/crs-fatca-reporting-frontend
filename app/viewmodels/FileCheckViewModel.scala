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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}

object FileCheckViewModel {

  def createFileSummary(fileMessageRef: String, fileStatus: String)(implicit messages: Messages): SummaryList = {

    val displayTags = HtmlContent(s"<strong class='govuk-tag govuk-tag--${Messages(s"cssColour.$fileStatus")}'>${Messages(s"status.$fileStatus")}</strong>")
    SummaryList(
      rows = Seq(
        SummaryListRow(
          key = Key(
            content = Text(messages("stillCheckingYourFile.fileId")),
            classes = "govuk-summary-checking-file__key"
          ),
          value = Value(content = Text(fileMessageRef))
        ),
        SummaryListRow(
          key = Key(content = Text(messages("stillCheckingYourFile.result"))),
          value = Value(content = displayTags)
        )
      )
    )
  }

}
