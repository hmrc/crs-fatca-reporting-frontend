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
import utils.ViewHelper
import views.html.FileContainsFatcaVoidView

class FileContainsFatcaVoidViewSpec extends SpecBase with ViewHelper {

  private val fileContainsFatcaVoidView: FileContainsFatcaVoidView              = app.injector.instanceOf[FileContainsFatcaVoidView]
  private val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  private def getHtml =
    Jsoup.parse(fileContainsFatcaVoidView().body)

  "FileContainsFatcaVoidView" - {
    "should have title and heading" in {
      val heading: String = "There is a problem with one or more DocTypeIndic values in your file"
      getWindowTitle(getHtml) must include(heading)
      getPageHeading(getHtml) mustEqual heading
    }
    "should have subheading" in {
      val heading: String = "How to void a previous report"
      getSubheadingText(getHtml, 0) mustEqual heading
    }
    "should have a list with correct values" in {
      val listValues = Seq(
        "Go to your reports.",
        "Find the report you want to void.",
        " Send a void request to delete all information in that report."
      )
      validateListValues(getAllElements(getHtml, ".govuk-list"), listValues)
    }
  }

}
