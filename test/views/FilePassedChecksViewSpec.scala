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
import viewmodels.FileCheckViewModel
import views.html.FilePassedChecksView

class FilePassedChecksViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {
  val view: FilePassedChecksView                                        = app.injector.instanceOf[FilePassedChecksView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "FilePassedChecksView" - {

    "should render page components" in {
      val listOfKeys   = Seq("File ID (MessageRefId)", "Result of automatic checks")
      val listOfValues = Seq("MyFATCAReportMessageRefId1234567890", "Passed")
      val fileSummary  = FileCheckViewModel.createFileSummary("MyFATCAReportMessageRefId1234567890", "Accepted")

      val renderedHtml: HtmlFormat.Appendable = view(fileSummary, "#")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Your file has passed our checks")
      getPageHeading(doc) mustEqual "Your file has passed our checks"
      validateListValues(getAllElements(doc, ".file-check-status-key"), listOfKeys)
      validateListValues(getAllElements(doc, ".file-check-status-value"), listOfValues)
      elementText(doc, ".govuk-tag--green") mustEqual "Passed"
      elementText(doc, "#submit") mustEqual "Go to confirmation"
    }
  }

}
