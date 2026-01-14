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
import models.{CRS, FATCA, ValidatedFileData}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.{ReportElectionsPage, RequiredGiinPage, ValidXMLPage}
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import viewmodels.CheckYourFileDetailsViewModel
import views.html.CheckYourFileDetailsView

import java.time.LocalDate

class CheckYourFileDetailsViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view: CheckYourFileDetailsView                                    = app.injector.instanceOf[CheckYourFileDetailsView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "CheckYourFileDetailsView" - {
    val summaryKeyLocator   = ".govuk-summary-list__key"
    val summaryValueLocator = ".govuk-summary-list__value"

    "should render page components with a summary list without election" in {
      val expectedFiName  = "fi-name"
      val userAnswers     = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(getMessageSpecData(CRS, fiNameFromFim = expectedFiName)))
      val viewModelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
      val fileDetails     = viewModelHelper.fileDetailsSummary
      val fiDetails       = viewModelHelper.financialInstitutionDetailsSummary

      val renderedHtml: HtmlFormat.Appendable = view(fileDetails, fiDetails, expectedFiName)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      verifyPageHeading(doc, expectedFiName)
      doc.select(".govuk-summary-list").size() mustBe 1
      val elements = doc.select(".govuk-summary-list__row")
      elements.size() mustBe 5
      verifyFileDetails(elements, "CRS")
      doc.select("#submit").text() mustBe "Continue"

      doc.text() must not include "File details"
      doc.text() must not include "Financial institution details"
    }
    "should render page components with a summary list without election & required GIIN for FATCA" in {
      val expectedFiName = "fi-name"
      val userAnswers = emptyUserAnswers
        .withPage(ValidXMLPage, getValidatedFileData(getMessageSpecData(FATCA, fiNameFromFim = expectedFiName)))
        .withPage(RequiredGiinPage, "testGIINValue")
      val viewModelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
      val fileDetails     = viewModelHelper.fileDetailsSummary
      val fiDetails       = viewModelHelper.financialInstitutionDetailsSummary

      val renderedHtml: HtmlFormat.Appendable = view(fileDetails, fiDetails, expectedFiName)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      verifyPageHeading(doc, expectedFiName)
      doc.select(".govuk-summary-list").size() mustBe 2
      val headings = doc.getElementsByTag("h2")
      headings.get(0).text() mustBe "File details"
      headings.get(1).text() mustBe "Financial institution details"
      val elements = doc.select(".govuk-summary-list__row")
      elements.size() mustBe 6
      verifyFileDetails(elements, "FATCA")
      assertRowValue(elements, 5, summaryKeyLocator, "Global Intermediary Identification Number")
      assertRowValue(elements, 5, summaryValueLocator, "testGIINValue")
    }
    "should render page components with a summary list with report election as false" in {
      val expectedFiName = "fi-name"
      val userAnswers = emptyUserAnswers
        .withPage(ValidXMLPage, getValidatedFileData(getMessageSpecData(CRS, fiNameFromFim = expectedFiName, reportingPeriod = LocalDate.of(2025, 1, 1))))
        .withPage(ReportElectionsPage, false)
      val viewModelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
      val fileDetails     = viewModelHelper.fileDetailsSummary
      val fiDetails       = viewModelHelper.financialInstitutionDetailsSummary

      val renderedHtml: HtmlFormat.Appendable = view(fileDetails, fiDetails, expectedFiName)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      verifyPageHeading(doc, expectedFiName)
      val headings = doc.getElementsByTag("h2")
      headings.get(0).text() mustBe "File details"
      headings.get(1).text() mustBe "Financial institution details"

      doc.select(".govuk-summary-list").size() mustBe 2
      val elements = doc.select(".govuk-summary-list__row")
      elements.size() mustBe 6

      verifyFileDetails(elements, "CRS")
      assertRowValue(elements, 5, summaryKeyLocator, "Do you want to make any elections for the CRS reporting period 2025?")
      assertRowValue(elements, 5, summaryValueLocator, "No")
    }

    def verifyPageHeading(doc: Document, expectedFiName: String) =
      getWindowTitle(doc) mustEqual s"Check your file details are correct for the financial institution - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual s"Check your file details are correct for $expectedFiName"

    def verifyFileDetails(elements: Elements, messageType: String) =
      assertRowValue(elements, 0, summaryKeyLocator, "File ID (MessageRefId)")
      assertRowValue(elements, 0, summaryValueLocator, "testRefId")
      assertRowValue(elements, 1, summaryKeyLocator, "Reporting regime (MessageType)")
      assertRowValue(elements, 1, summaryValueLocator, messageType)
      assertRowValue(elements, 2, summaryKeyLocator, "FI ID (SendingCompanyIN)")
      assertRowValue(elements, 2, summaryValueLocator, "testFI")
      assertRowValue(elements, 3, summaryKeyLocator, "Financial institution (ReportingFI Name)")
      assertRowValue(elements, 3, summaryValueLocator, "testReportingName")
      assertRowValue(elements, 4, summaryKeyLocator, "File information")
      assertRowValue(elements, 4, summaryValueLocator, "New information")

    def assertRowValue(elements: Elements, index: Int, key: String, value: String) = elements.get(index).select(key).text() mustBe value
  }
}
