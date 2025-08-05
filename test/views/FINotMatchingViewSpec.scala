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
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.FINotMatchingView

class FINotMatchingViewSpec extends SpecBase with ViewHelper {

  val view: FINotMatchingView                                           = app.injector.instanceOf[FINotMatchingView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  val renderedHtml: HtmlFormat.Appendable = view()
  lazy val doc: Document = Jsoup.parse(renderedHtml.body)

  "FINotMatchingView" - {

    "should have title and heading" in {
      val heading: String = "The FI ID in your file does not match any financial institutions in the service"
      getWindowTitle(doc) must include(heading)
      getPageHeading(doc) mustEqual heading
    }

    "should have paragraphs" in {
      val paragraphValues = Seq(
        "You must update the SendingCompanyIN value in your file to match the FI ID of a financial institution in the service, then upload the updated file.",
        "If you have not added the financial institution yet, then you must add a new financial institution first."
      )
      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)
    }

    "should have links" in {
      val linkElements = getAllElements(doc, ".govuk-link")

      val FIManagementLink = linkElements
        .select(":contains(add a new financial institution first)")
        .attr("href") mustEqual "http://localhost:10033/manage-your-crs-and-fatca-financial-institutions/?goToYourFIs=true"

      linkElements.select(":contains(upload the updated file)").attr("href") mustEqual controllers.routes.IndexController.onPageLoad().url
    }
  }

}
