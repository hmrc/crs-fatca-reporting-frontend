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
import models.{CRS, FATCA, MessageSpecData, ValidatedFileData}
import org.jsoup.Jsoup
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
    "should render page components with a summary list without election" in {
      val expectedFiName      = "fi-name"
      val fileName            = "test-file.xml"
      val FileSize            = 100L
      val FileChecksum        = "checksum"
      val reportingPeriodYear = 2025
      val messageSpecData = MessageSpecData(
        messageType = CRS,
        sendingCompanyIN = "sendingCompanyIN",
        messageRefId = "messageRefId",
        reportingFIName = "reportingFIName",
        reportingPeriod = LocalDate.of(reportingPeriodYear, 1, 1),
        giin = None,
        fiNameFromFim = expectedFiName
      )

      val crsValidatedFileData = ValidatedFileData(fileName, messageSpecData, FileSize, FileChecksum)
      val userAnswers          = emptyUserAnswers.withPage(ValidXMLPage, crsValidatedFileData)

      val viewModelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
      val fileDetails     = viewModelHelper.getYourFileDetailsRows
      val fiDetails       = viewModelHelper.getFIDetailsRows

      val renderedHtml: HtmlFormat.Appendable = view(fileDetails, fiDetails, expectedFiName)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) mustEqual s"Check your file details are correct for the financial institution - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual s"Check your file details are correct for $expectedFiName"

      doc.select(".govuk-summary-list").size() mustBe 1
      doc.select(".govuk-summary-list__row").size() mustBe 5

      doc.select(".govuk-summary-list__row:nth-child(1) .govuk-summary-list__key").text() mustBe "File ID (MessageRefId)"
      doc.select(".govuk-summary-list__row:nth-child(1) .govuk-summary-list__value").text() mustBe "messageRefId"

      doc.select(".govuk-summary-list__row:nth-child(2) .govuk-summary-list__key").text() mustBe "Reporting regime (MessageType)"
      doc.select(".govuk-summary-list__row:nth-child(2) .govuk-summary-list__value").text() mustBe "CRS"

      doc.select(".govuk-summary-list__row:nth-child(3) .govuk-summary-list__key").text() mustBe "FI ID (SendingCompanyIN)"
      doc.select(".govuk-summary-list__row:nth-child(3) .govuk-summary-list__value").text() mustBe "sendingCompanyIN"

      doc.select(".govuk-summary-list__row:nth-child(4) .govuk-summary-list__key").text() mustBe "Financial institution (ReportingFI Name)"
      doc.select(".govuk-summary-list__row:nth-child(4) .govuk-summary-list__value").text() mustBe "reportingFIName"

      doc.select(".govuk-summary-list__row:nth-child(5) .govuk-summary-list__key").text() mustBe "File information"
      doc.select(".govuk-summary-list__row:nth-child(5) .govuk-summary-list__value").text() mustBe "New information"

      doc.select("#submit").text() mustBe "Continue"

      doc.text() must not include "File details"
      doc.text() must not include "Financial institution details"
    }
    "should render page components with a summary list without election & required GIIN for FATCA" in {
      val expectedFiName      = "fi-name"
      val fileName            = "test-file.xml"
      val FileSize            = 100L
      val FileChecksum        = "checksum"
      val reportingPeriodYear = 2025
      val messageSpecData = MessageSpecData(
        messageType = FATCA,
        sendingCompanyIN = "sendingCompanyIN",
        messageRefId = "messageRefId",
        reportingFIName = "reportingFIName",
        reportingPeriod = LocalDate.of(reportingPeriodYear, 1, 1),
        giin = None,
        fiNameFromFim = expectedFiName
      )

      val crsValidatedFileData = ValidatedFileData(fileName, messageSpecData, FileSize, FileChecksum)
      val userAnswers = emptyUserAnswers
        .withPage(ValidXMLPage, crsValidatedFileData)
        .withPage(RequiredGiinPage, "testGIINValue")

      val viewModelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
      val fileDetails     = viewModelHelper.getYourFileDetailsRows
      val fiDetails       = viewModelHelper.getFIDetailsRows

      val renderedHtml: HtmlFormat.Appendable = view(fileDetails, fiDetails, expectedFiName)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) mustEqual s"Check your file details are correct for the financial institution - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual s"Check your file details are correct for $expectedFiName"

      doc.select(".govuk-summary-list").size() mustBe 2
      val elements = doc.select(".govuk-summary-list__row")
      elements.size() mustBe 6

      elements.get(0).select(".govuk-summary-list__key").text() mustBe "File ID (MessageRefId)"
      elements.get(0).select(".govuk-summary-list__value").text() mustBe "messageRefId"

      elements.get(1).select(".govuk-summary-list__key").text() mustBe "Reporting regime (MessageType)"
      elements.get(1).select(".govuk-summary-list__value").text() mustBe "FATCA"

      elements.get(2).select(".govuk-summary-list__key").text() mustBe "FI ID (SendingCompanyIN)"
      elements.get(2).select(".govuk-summary-list__value").text() mustBe "sendingCompanyIN"

      elements.get(3).select(".govuk-summary-list__key").text() mustBe "Financial institution (ReportingFI Name)"
      elements.get(3).select(".govuk-summary-list__value").text() mustBe "reportingFIName"

      elements.get(4).select(".govuk-summary-list__key").text() mustBe "File information"
      elements.get(4).select(".govuk-summary-list__value").text() mustBe "New information"

      elements.get(5).select(".govuk-summary-list__key").text() mustBe "Global Intermediary Identification Number"
      elements.get(5).select(".govuk-summary-list__value").text() mustBe "testGIINValue"

      val headings = doc.getElementsByTag("h2")
      headings.get(0).text() mustBe "File details"
      headings.get(1).text() mustBe "Financial institution details"
    }
    "should render page components with a summary list with report election as false" in {
      val expectedFiName      = "fi-name"
      val fileName            = "test-file.xml"
      val FileSize            = 100L
      val FileChecksum        = "checksum"
      val reportingPeriodYear = 2025
      val messageSpecData = MessageSpecData(
        messageType = CRS,
        sendingCompanyIN = "sendingCompanyIN",
        messageRefId = "messageRefId",
        reportingFIName = "reportingFIName",
        reportingPeriod = LocalDate.of(reportingPeriodYear, 1, 1),
        giin = None,
        fiNameFromFim = expectedFiName
      )

      val crsValidatedFileData = ValidatedFileData(fileName, messageSpecData, FileSize, FileChecksum)
      val userAnswers          = emptyUserAnswers.withPage(ValidXMLPage, crsValidatedFileData).withPage(ReportElectionsPage, false)

      val viewModelHelper = CheckYourFileDetailsViewModel(userAnswers)(using messages(app))
      val fileDetails     = viewModelHelper.getYourFileDetailsRows
      val fiDetails       = viewModelHelper.getFIDetailsRows

      val renderedHtml: HtmlFormat.Appendable = view(fileDetails, fiDetails, expectedFiName)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) mustEqual s"Check your file details are correct for the financial institution - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual s"Check your file details are correct for $expectedFiName"

      doc.select(".govuk-summary-list").size() mustBe 2
      val elements = doc.select(".govuk-summary-list__row")
      elements.size() mustBe 6

      elements.get(0).select(".govuk-summary-list__key").text() mustBe "File ID (MessageRefId)"
      elements.get(0).select(".govuk-summary-list__value").text() mustBe "messageRefId"

      elements.get(1).select(".govuk-summary-list__key").text() mustBe "Reporting regime (MessageType)"
      elements.get(1).select(".govuk-summary-list__value").text() mustBe "CRS"

      elements.get(2).select(".govuk-summary-list__key").text() mustBe "FI ID (SendingCompanyIN)"
      elements.get(2).select(".govuk-summary-list__value").text() mustBe "sendingCompanyIN"

      elements.get(3).select(".govuk-summary-list__key").text() mustBe "Financial institution (ReportingFI Name)"
      elements.get(3).select(".govuk-summary-list__value").text() mustBe "reportingFIName"

      elements.get(4).select(".govuk-summary-list__key").text() mustBe "File information"
      elements.get(4).select(".govuk-summary-list__value").text() mustBe "New information"

      elements.get(5).select(".govuk-summary-list__key").text() mustBe "Do you want to make any elections for the CRS reporting period 2025?"
      elements.get(5).select(".govuk-summary-list__value").text() mustBe "No"

      val headings = doc.getElementsByTag("h2")
      headings.get(0).text() mustBe "File details"
      headings.get(1).text() mustBe "Financial institution details"
    }
  }
}
