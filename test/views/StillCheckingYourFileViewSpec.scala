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
import utils.ViewHelper
import viewmodels.{CheckYourFileDetailsViewModel, FileCheckViewModel}
import views.html.{CheckYourFileDetailsView, StillCheckingYourFileView}

class StillCheckingYourFileViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view: StillCheckingYourFileView                                   = app.injector.instanceOf[StillCheckingYourFileView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "CheckYourFileDetailsView" - {
    "should render page components with a summary list when fi = user" in {
      val summaryList = FileCheckViewModel.createFileSummary("MyFATCAReportMessageRefId1234567890", "Pending")

      val renderedHtml: HtmlFormat.Appendable = view(summaryList, "", true, "EFG Bank plc")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) mustEqual s"We need more time to check your file - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual s"We need more time to check your file"
      getParagraphText(doc, 1) mustEqual s"You need to refresh the page for updates on the status of our automatic checks."
      getParagraphText(doc,
                       2
      ) mustEqual s"If you have been refreshing for more than 10 minutes, you can sign out. We will email you and your contacts for EFG Bank plc if your file has passed the checks or you can sign in again later to check the results."

      doc.select(".govuk-summary-list").size() mustBe 1
      doc.select(".govuk-summary-list__row").size() mustBe 2

      doc.select(".govuk-summary-list__row:nth-child(1) .govuk-summary-list__key").text() mustBe "File ID (MessageRefId)"
      doc.select(".govuk-summary-list__row:nth-child(1) .govuk-summary-list__value").text() mustBe "MyFATCAReportMessageRefId1234567890"

      doc.select(".govuk-summary-list__row:nth-child(2) .govuk-summary-list__key").text() mustBe "Result of automatic checks"
      doc.select(".govuk-summary-list__row:nth-child(2) .govuk-summary-list__value").text() mustBe "Pending"
    }

    "should render page components with a summary list when fi != user" in {
      val summaryList = FileCheckViewModel.createFileSummary("MyFATCAReportMessageRefId1234567890", "Pending")

      val renderedHtml: HtmlFormat.Appendable = view(summaryList, "", false, "")
      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) mustEqual s"We need more time to check your file - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual s"We need more time to check your file"
      getParagraphText(doc, 1) mustEqual s"You need to refresh the page for updates on the status of our automatic checks."
      getParagraphText(doc,
        2
      ) mustEqual s"If you have been refreshing for more than 10 minutes, you can sign out. We will email you if your file has passed the checks or you can sign in again later to check the results."

      doc.select(".govuk-summary-list").size() mustBe 1
      doc.select(".govuk-summary-list__row").size() mustBe 2

      doc.select(".govuk-summary-list__row:nth-child(1) .govuk-summary-list__key").text() mustBe "File ID (MessageRefId)"
      doc.select(".govuk-summary-list__row:nth-child(1) .govuk-summary-list__value").text() mustBe "MyFATCAReportMessageRefId1234567890"

      doc.select(".govuk-summary-list__row:nth-child(2) .govuk-summary-list__key").text() mustBe "Result of automatic checks"
      doc.select(".govuk-summary-list__row:nth-child(2) .govuk-summary-list__value").text() mustBe "Pending"
    }
  }
}
