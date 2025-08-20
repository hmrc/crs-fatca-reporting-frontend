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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.ElectionsNotSentView

class ElectionsNotSentViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view: ElectionsNotSentView                                        = app.injector.instanceOf[ElectionsNotSentView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "ElectionsNotSentView" - {

    "should render page components" in {
      val giinSentAndSaved                    = true
      val renderedHtml: HtmlFormat.Appendable = view(giinSentAndSaved)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      val paragraphValues = Seq(
        "We have received the GIIN for this financial institution, but have not been able to receive the elections.",
        "You can still send your file without the elections, then add them after in the service."
      )

      getWindowTitle(doc) mustEqual "There is a problem with sending the elections - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual "There is a problem with sending the elections"
      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)
      getWarningText(doc) must include("You must finish sending your file to complete the reporting process.")
    }

    "should not display 'We have received the GIIN or this financial institution' message when giinSentAndSaved value is false" in {
      val giinSentAndSaved                    = false
      val renderedHtml: HtmlFormat.Appendable = view(giinSentAndSaved)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      val paragraphValues = Seq(
        "You can still send your file without the elections, then add them after in the service."
      )

      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)
      getAllParagraph(doc).text() mustNot include("We have received the GIIN for this financial institution, but have not been able to receive the elections.")
    }

    def getWarningText(page: Document): String = page.select(".govuk-warning-text__text").text()

  }
}
