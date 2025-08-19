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
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.GiinNotSentView

class GiinNotSentViewSpec extends SpecBase with ViewHelper {

  val view: GiinNotSentView                                             = app.injector.instanceOf[GiinNotSentView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  def getHtml(bool: Option[Boolean] = None) = {
    val renderedHtml: HtmlFormat.Appendable = view(bool)
    Jsoup.parse(renderedHtml.body)
  }
  "GiinNotSentView" - {
    "should have title and heading" in {
      val heading: String = "We’re unable to receive your file"
      getWindowTitle(getHtml()) must include(heading)
      getPageHeading(getHtml()) mustEqual heading
    }
    "should have dynamic paragraph " - {
      "when elections not sent" in validateAllParaValues(
        getAllParagraph(getHtml(None)).text(),
        Seq("We have not been able to receive the GIIN for this financial institution. We need the GIIN to receive your file.")
      )
      "when elections sent successfully" in validateAllParaValues(
        getAllParagraph(getHtml(Some(true))).text(),
        Seq("We have received the elections for this financial institution, but have not been able to receive the GIIN. We need the GIIN to receive your file.")
      )
      "when elections failed to send" in validateAllParaValues(
        getAllParagraph(getHtml(Some(false))).text(),
        Seq("We have not been able to receive the GIIN or elections for this financial institution. We need the GIIN to receive your file.")
      )

    }
    "should have none dynamic paragraph" in {
      validateAllParaValues(getAllParagraph(getHtml()).text(), Seq("Try sending your file again later."))
    }

    "should have warning" in {
      val warningText = "You must send your file later to complete the reporting process."
      getAllElements(getHtml(), ".govuk-warning-text__text").text() must include(warningText)
    }
  }

}
