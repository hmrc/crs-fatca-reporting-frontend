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

package views.elections

import base.SpecBase
import forms.elections.ReportElectionsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.elections.ReportElectionsView

class ReportElectionsViewSpec extends SpecBase with ViewHelper {

  val view: ReportElectionsView                                         = app.injector.instanceOf[ReportElectionsView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                      = new ReportElectionsFormProvider()
  val form                                                              = formProvider("crs")

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  val renderedHtml: HtmlFormat.Appendable = view("2026", "crs", "Placeholder name", form, NormalMode)
  lazy val doc                            = Jsoup.parse(renderedHtml.body)

  "ReportElectionsView" - {

    "should have title and heading" in {
      val title: String   = "Do you want to make any elections for the crs reporting period 2026?"
      val heading: String = "Do you want to make any elections for Placeholder name for the crs reporting period 2026?"
      getWindowTitle(doc) must include(title)
      getPageHeading(doc) mustEqual heading
    }

    "should have radio Button" in {
      val linkElements = getAllElements(doc, ".govuk-radios__label")
      linkElements.size() mustEqual 2
      linkElements.get(0).text() mustEqual "Yes"
      linkElements.get(1).text() mustEqual "No"
    }

    "should have continue Button" in {
      elementText(doc, "#submit") mustEqual "Continue"
    }
  }

}
