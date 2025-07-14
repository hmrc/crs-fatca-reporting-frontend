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
import views.html.InvalidMessageTypeErrorView

class InvalidMessageTypeErrorViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view: InvalidMessageTypeErrorView                                 = app.injector.instanceOf[InvalidMessageTypeErrorView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "InvalidMessageTypeErrorView" - {

    "should render page components" in {
      val renderedHtml: HtmlFormat.Appendable = view()
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      val paragraphValues = Seq(
        "You must update the MessageType value to either ‘CRS’ or ‘FATCA’ and then upload the updated file.",
        "Refer to the:"
      )

      getWindowTitle(doc) must include("There is a problem with the MessageType value in your file - Send a CRS or FATCA report - GOV.UK")
      getPageHeading(doc) mustEqual "There is a problem with the MessageType value in your file"
      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)

      val linkElements = doc.select(".govuk-link")
      linkElements.select(":contains(CRS technical guidance for XML files)").attr("href") mustEqual "#"
      linkElements.select(":contains(FATCA technical guidance for XML files)").attr("href") mustEqual "#"
    }
  }

  def validateAllParaValues(allParaValues: String, paraValues: Seq[String]): Unit =
    paraValues.foreach(
      p => allParaValues must include(p)
    )
}
