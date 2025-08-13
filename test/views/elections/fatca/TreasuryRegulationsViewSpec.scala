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

package views.elections.fatca

import base.SpecBase
import forms.elections.fatca.TreasuryRegulationsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.elections.fatca.TreasuryRegulationsView

class TreasuryRegulationsViewSpec extends SpecBase with ViewHelper {

  val view: TreasuryRegulationsView                                     = app.injector.instanceOf[TreasuryRegulationsView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                      = new TreasuryRegulationsFormProvider()
  val form                                                              = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "fiName")
  lazy val doc                            = Jsoup.parse(renderedHtml.body)

  "TreasuryRegulationsView" - {

    "should have a title" in {
      val title: String = "Is the financial institution using due diligence procedures from US Treasury Regulations for FATCA?"
      getWindowTitle(doc) must include(title)
    }
    "should have a heading" in {
      val heading: String = "Is fiName using due diligence procedures from US Treasury Regulations for FATCA?"
      getPageHeading(doc) mustEqual heading
    }

    "should have radio Button" in {
      val linkElements = getAllElements(doc, ".govuk-radios__label")
      linkElements.size() mustEqual 2
      linkElements.get(0).text() mustEqual "Yes"
      linkElements.get(1).text() mustEqual "No"
    }

    "should have continue Button" in {
      elementText(doc, ".govuk-button") mustEqual "Continue"
    }
  }

}
