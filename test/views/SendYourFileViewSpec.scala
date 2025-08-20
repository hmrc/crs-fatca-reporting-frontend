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
import models.SendYourFileAdditionalText.{BOTH, ELECTIONS, GIIN, NONE}
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.SendYourFileView

class SendYourFileViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view1: SendYourFileView                                           = app.injector.instanceOf[SendYourFileView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "SendYourFileView" - {
    val paragraphValues = Seq(
      "We will now automatically check your file for business rule errors and send you to another page once completed.",
      "By sending this file, you are confirming that the information is correct and complete to the best of your knowledge."
    )
    "should render page components with none" in {
      val renderedHtml: HtmlFormat.Appendable = view1(NONE)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Send your file")
      getPageHeading(doc) mustEqual "Send your file"
      val allParagraphValues = getAllParagraph(doc).text()
      validateAllParaValues(allParagraphValues,paragraphValues)
      allParagraphValues mustNot include("also send your GIIN or let you know if there are any issues with sending it.")
      allParagraphValues mustNot include("also send your elections or let you know if there are any issues with sending them.")
      allParagraphValues mustNot include("also send your GIIN and elections, or let you know if there are any issues with sending them.")
      elementText(doc, "#submit") mustEqual "Confirm and send"
    }

    "should render page components with both" in {
      val renderedHtml: HtmlFormat.Appendable = view1(BOTH)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Send your file")
      getPageHeading(doc) mustEqual "Send your file"
      val allParagraphValues = getAllParagraph(doc).text()
      validateAllParaValues(allParagraphValues,paragraphValues)
      allParagraphValues mustNot include("also send your GIIN or let you know if there are any issues with sending it.")
      allParagraphValues mustNot include("also send your elections or let you know if there are any issues with sending them.")
      allParagraphValues must include("also send your GIIN and elections, or let you know if there are any issues with sending them.")
      elementText(doc, "#submit") mustEqual "Confirm and send"
    }

    "should render page components with only giin" in {
      val renderedHtml: HtmlFormat.Appendable = view1(GIIN)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Send your file")
      getPageHeading(doc) mustEqual "Send your file"
      val allParagraphValues = getAllParagraph(doc).text()
      validateAllParaValues(allParagraphValues,paragraphValues)
      allParagraphValues must include("also send your GIIN or let you know if there are any issues with sending it.")
      allParagraphValues mustNot include("also send your elections or let you know if there are any issues with sending them.")
      allParagraphValues mustNot include("also send your GIIN and elections, or let you know if there are any issues with sending them.")
      elementText(doc, "#submit") mustEqual "Confirm and send"
    }

    "should render page components with only elections" in {
      val renderedHtml: HtmlFormat.Appendable = view1(ELECTIONS)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Send your file")
      getPageHeading(doc) mustEqual "Send your file"
      val allParagraphValues = getAllParagraph(doc).text()
      validateAllParaValues(allParagraphValues,paragraphValues)
      allParagraphValues mustNot include("also send your GIIN or let you know if there are any issues with sending it.")
      allParagraphValues must include("also send your elections or let you know if there are any issues with sending them.")
      allParagraphValues mustNot include("also send your GIIN and elections, or let you know if there are any issues with sending them.")
      elementText(doc, "#submit") mustEqual "Confirm and send"
    }
  }
}
