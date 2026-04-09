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

import models.{GenericError, Message}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import utils.ReportingConstants.MAX_DATA_ERRORS

import javax.inject.Inject

class ErrorViewHelper @Inject() () {

  def generateTable(error: Seq[GenericError])(implicit messages: Messages): Seq[Seq[TableRow]] =
    error.take(MAX_DATA_ERRORS).map {
      er =>
        er.message.messageKey match {
          case "xml.elem.reportingPeriod.invalid" => invalidReportingPeriod(er.lineNumber)
          case "xml.elem.DocRefId.max"            => invalidDocRef(er.lineNumber)
          case "xml.elem.messageRefId.max"        => messageRefId(er.lineNumber)
          case "xml.elem.unknown"                 => unknownElem(er.lineNumber, er.message)
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

  private def invalidReportingPeriod(lineNumber: Int)(implicit messages: Messages) = errorRows(lineNumber, "xml.elem.reportingPeriod.invalid", 3)

  private def invalidDocRef(lineNumber: Int)(implicit messages: Messages) = errorRows(lineNumber, "xml.elem.DocRefId.max", 3)

  private def messageRefId(lineNumber: Int)(implicit messages: Messages) = errorRows(lineNumber, "xml.elem.messageRefId.max", 7)

  private def unknownElem(lineNumber: Int, m: Message)(implicit messages: Messages) = {
    val htmlContent =
      s"""
                          <p class="govuk-body govuk-!-margin-bottom-1">${messages(m.messageKey, m.args: _*)}</p>
                          <ul class="govuk-list govuk-list--bullet govuk-!-margin-bottom-0">
                            <li>${messages("xml.elem.unknown.li1")}</li>
                            <li>${messages("xml.elem.unknown.li2")}</li>
                          </ul>
                          """
    Seq(
      TableRow(content = Text(lineNumber.toString), classes = "govuk-table__cell--numeric", attributes = Map("id" -> s"lineNumber_$lineNumber")),
      TableRow(content = HtmlContent(htmlContent), attributes = Map("id" -> s"errorMessage_$lineNumber"))
    )
  }

  private def errorRows(lineNumber: Int, messageTag: String, listCount: Int)(implicit messages: Messages): Seq[TableRow] = {

    val listItemsHtml = (1 to listCount)
      .map {
        index => s"<li>${messages(s"$messageTag.li$index")}</li>"
      }
      .mkString("\n")

    val htmlContent =
      s"""
         |<p class="govuk-body govuk-!-margin-bottom-1">${messages(messageTag)}</p>
         |<ul class="govuk-list govuk-list--bullet">
         |  $listItemsHtml
         |</ul>
         |<p class="govuk-body govuk-!-margin-bottom-0">${messages(s"$messageTag.p2")}</p>
         |""".stripMargin

    Seq(
      TableRow(
        content = Text(lineNumber.toString),
        classes = "govuk-table__cell--numeric",
        attributes = Map("id" -> s"lineNumber_$lineNumber")
      ),
      TableRow(
        content = HtmlContent(htmlContent),
        attributes = Map("id" -> s"errorMessage_$lineNumber")
      )
    )
  }
}
