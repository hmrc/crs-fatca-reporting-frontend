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

package utils

import models.GenericError
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

import javax.inject.Inject

class ErrorViewHelper @Inject() () {

  def generateTable(error: Seq[GenericError])(implicit messages: Messages): Seq[Seq[TableRow]] =
    error.map {
      er =>
        er.message.messageKey match {
          case "xml.elem.reportingPeriod.invalid" => invalidReportingPeriod(er.lineNumber)
          case "xml.elem.DocRefId.max"            => invalidDocRef(er.lineNumber)
          case _ =>
            Seq(
              TableRow(content = Text(er.lineNumber.toString),
                       classes = "govuk-table__cell--numeric",
                       attributes = Map("id" -> s"lineNumber_${er.lineNumber}")
              ),
              TableRow(content = Text(messages(er.message.messageKey, er.message.args: _*)), attributes = Map("id" -> s"errorMessage_${er.lineNumber}"))
            )
        }

    }

  private def invalidReportingPeriod(lineNumber: Int)(implicit messages: Messages) = {
    val htmlContent =
      s"""
                      <p class="govuk-body govuk-!-margin-bottom-1">${messages("xml.elem.reportingPeriod.invalid")}</p>
                      <ul class="govuk-list govuk-list--bullet">
                        <li>${messages("xml.elem.reportingPeriod.invalid.li1")}</li>
                        <li>${messages("xml.elem.reportingPeriod.invalid.li2")}</li>
                        <li>${messages("xml.elem.reportingPeriod.invalid.li3")}</li>
                      </ul>
                      <p class="govuk-body govuk-!-margin-bottom-0">${messages("xml.elem.reportingPeriod.invalid.p2")}</p>
                      """
    Seq(
      TableRow(content = Text(lineNumber.toString), classes = "govuk-table__cell--numeric", attributes = Map("id" -> s"lineNumber_$lineNumber")),
      TableRow(content = HtmlContent(htmlContent), attributes = Map("id" -> s"errorMessage_$lineNumber"))
    )
  }

  private def invalidDocRef(lineNumber: Int)(implicit messages: Messages) = {
    val htmlContent =
      s"""
                      <p class="govuk-body govuk-!-margin-bottom-1">${messages("xml.elem.DocRefId.max")}</p>
                      <ul class="govuk-list govuk-list--bullet">
                        <li>${messages("xml.elem.DocRefId.max.li1")}</li>
                        <li>${messages("xml.elem.DocRefId.max.li2")}</li>
                        <li>${messages("xml.elem.DocRefId.max.li3")}</li>
                      </ul>
                      <p class="govuk-body govuk-!-margin-bottom-0">${messages("xml.elem.DocRefId.max.p2")}</p>
                      """
    Seq(
      TableRow(content = Text(lineNumber.toString), classes = "govuk-table__cell--numeric", attributes = Map("id" -> s"lineNumber_$lineNumber")),
      TableRow(content = HtmlContent(htmlContent), attributes = Map("id" -> s"errorMessage_$lineNumber"))
    )
  }
}
