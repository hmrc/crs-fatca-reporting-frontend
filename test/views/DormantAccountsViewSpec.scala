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
import forms.RequiredGiinFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.DormantAccountsView

class DormantAccountsViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view: DormantAccountsView                                         = app.injector.instanceOf[DormantAccountsView]
  val formProvider                                                      = new RequiredGiinFormProvider()
  val form: Form[String]                                                = formProvider()
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "DormantAccountsView" - {
    val renderedHtml: HtmlFormat.Appendable =
      view(form, NormalMode, "fiName")
    lazy val doc = Jsoup.parse(renderedHtml.body)
    "should have a title" in {
      getWindowTitle(doc) must include("Is the financial institution treating dormant accounts as not being reportable accounts for CRS?")
    }
    "should have a heading" in {
      getPageHeading(doc) mustEqual "Is fiName treating dormant accounts as not being reportable accounts for CRS?"
    }
  }

}
