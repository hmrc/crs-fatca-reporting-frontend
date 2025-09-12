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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.{RulesErrorHelper, ViewHelper}
import views.html.{DataErrorsView, RulesErrorView}

class RulesErrorViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper with RulesErrorHelper {
  val view: RulesErrorView                                              = app.injector.instanceOf[RulesErrorView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  val fileName = "filename.xml"

  "RulesErrorsView" - {

    "should render page components for CRS regimeType with under 100 errors" in {
      val regimeType = "CRS"
      val paragraphValues = Seq(
        "We cannot accept the file filename.xml because it does not meet the CRS business rules."
      )

      val listValues = Seq(
        "Print this page",
        "View the errors on this page",
        "Refer to the CRS technical guidance for XML files and update your file",
        "Upload the file"
      )
      val tableHeaderValues = Seq(
        "Code",
        "DocRefId",
        "Error"
      )

      val tableCellValueFirstRow = Seq(
        "CRS 80002",
        "N/A",
        "The CorrDocRefId provided does not match any DocRefId in our records."
      )

      val tableCellValueSecondRow = Seq(
        "CRS 50008",
        "GB2026GB-FIID123456789-CRSReport2026001-ReportingFI-001",
        "MessageRefId must be 100 characters or less and follow this structure in the order referenced: the same value as the year in the MessageSpec ReportingPeriod in the format ‘YYYY’ For example, ‘GB2022GBXACBC0000999999CBC40120230523T140000123456789’."
      )

      val renderedHtml: HtmlFormat.Appendable = view(fileName, regimeType, 2, createFileRejectedViewModel())

      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) mustEqual "There is a problem with your file data - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual "There is a problem with your file data"
      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)
      validateListValues(getAllElements(doc, ".govuk-list"), listValues)
      val linkElements = getAllElements(doc, ".govuk-link")
      val crsFILink    = linkElements.select(":contains(Back to manage your CRS and FATCA reports)").attr("href")
      linkElements.select(":contains(CRS technical guidance for XML files)").attr("href") mustEqual "#"
      linkElements.select(":contains(Upload the file)").attr("href") mustEqual "/report-for-crs-and-fatca/report/upload-file"
      crsFILink mustEqual "http://localhost:10033/manage-your-crs-and-fatca-financial-institutions"

      validateListValues(getAllElements(doc, ".govuk-table__header"), tableHeaderValues)
      doc.select(".govuk-table__body .govuk-table__row").size() mustEqual 2
      validateListValues(getAllElements(doc, ".govuk-table__body .govuk-table__row:nth-child(1)"), tableCellValueFirstRow)
      validateListValues(getAllElements(doc, ".govuk-table__body .govuk-table__row:nth-child(2)"), tableCellValueSecondRow)

    }

    "should render page components for FATCA regimeType with over 100 errors" in {
      val regimeType = "FATCA"
      val paragraphValues = Seq(
        "We cannot accept the file filename.xml because it does not meet the FATCA business rules.",
        "Your file has over 100 errors. We only show the first 100 errors on this page."
      )

      val listValues = Seq(
        "Print this page",
        "View the errors on this page",
        "Refer to the FATCA technical guidance for XML files and update your file",
        "Upload the file"
      )
      val tableHeaderValues = Seq(
        "Code",
        "DocRefId",
        "Error"
      )

      val tableCellValueFirstRow = Seq(
        "FATCA 80002",
        "N/A",
        "The CorrDocRefId provided does not match any DocRefId in our records."
      )

      val tableCellValueSecondRow = Seq(
        "FATCA 50008",
        "GB2026GB-FIID123456789-CRSReport2026001-ReportingFI-001",
        "MessageRefId must be 100 characters or less and follow this structure in the order referenced: the same value as the year in the MessageSpec ReportingPeriod in the format ‘YYYY’ For example, ‘GB2022GBXACBC0000999999CBC40120230523T140000123456789’."
      )

      val renderedHtml: HtmlFormat.Appendable = view(fileName, regimeType, 101, createFileRejectedViewModel())

      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) mustEqual "There is a problem with your file data - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual "There is a problem with your file data"
      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)
      validateListValues(getAllElements(doc, ".govuk-list"), listValues)
      val linkElements = getAllElements(doc, ".govuk-link")
      val crsFILink    = linkElements.select(":contains(Back to manage your CRS and FATCA reports)").attr("href")
      linkElements.select(":contains(Refer to the FATCA technical guidance for XML files)").attr("href") mustEqual "#"
      linkElements.select(":contains(Upload the file)").attr("href") mustEqual "/report-for-crs-and-fatca/report/upload-file"
      crsFILink mustEqual "http://localhost:10033/manage-your-crs-and-fatca-financial-institutions"

      validateListValues(getAllElements(doc, ".govuk-table__header"), tableHeaderValues)
      doc.select(".govuk-table__body .govuk-table__row").size() mustEqual 2
      validateListValues(getAllElements(doc, ".govuk-table__body .govuk-table__row:nth-child(1)"), tableCellValueFirstRow)
      validateListValues(getAllElements(doc, ".govuk-table__body .govuk-table__row:nth-child(2)"), tableCellValueSecondRow)
    }

  }

}
