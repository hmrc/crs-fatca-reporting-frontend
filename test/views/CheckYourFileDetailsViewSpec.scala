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
import views.html.CheckYourFileDetailsView
import viewmodels.CheckYourFileDetailsViewModel

class CheckYourFileDetailsViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view: CheckYourFileDetailsView = app.injector.instanceOf[CheckYourFileDetailsView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "CheckYourFileDetailsView" - {
    "should render page components with a summary list" in {
      val financialInstitutionName = "Placeholder Name"

      val summaryList = CheckYourFileDetailsViewModel.getYourFileDetailsRows()

      val renderedHtml: HtmlFormat.Appendable = view(summaryList, financialInstitutionName)
      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) mustEqual s"Check your details are correct for the financial institution - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual s"Check your details are correct for $financialInstitutionName"

      doc.select(".govuk-summary-list").size() mustBe 1
      doc.select(".govuk-summary-list__row").size() mustBe 5

      doc.select(".govuk-summary-list__row:nth-child(1) .govuk-summary-list__key").text() mustBe "File ID (MessageRefId)"
      doc.select(".govuk-summary-list__row:nth-child(1) .govuk-summary-list__value").text() mustBe "MyFATCAReportMessageRefId234567890LONGONGLONGLONGLONG"

      doc.select(".govuk-summary-list__row:nth-child(5) .govuk-summary-list__key").text() mustBe "File information"
      doc.select(".govuk-summary-list__row:nth-child(5) .govuk-summary-list__value").text() mustBe "New information"
    }
  }
}