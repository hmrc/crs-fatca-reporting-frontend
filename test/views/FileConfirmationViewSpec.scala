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
import models.fileDetails.FileDetails
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import viewmodels.FileConfirmationViewModel
import views.html.FileConfirmationView

import java.time.LocalDateTime

class FileConfirmationViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {
  val view: FileConfirmationView                                        = app.injector.instanceOf[FileConfirmationView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "FilePassedChecksView" - {

    "should render page components" in {
      val submittedTime = LocalDateTime.parse("2025-09-12T12:01:00")
      val date = "12 September 2025"
      val time = "12:01am"
      val fileDetails = FileDetails("name.xml", "c-8-new-f-va", "CRS", "EFG Bank plc", "New information", submittedTime, LocalDateTime.now())
      val fileSummary = FileConfirmationViewModel.getSummaryRows(fileDetails)
      val paraContent = FileConfirmationViewModel.getEmailParagraphForFI("user1@email.com",None)
      val listOfKeys = Seq("File ID (MessageRefId)", "Reporting regime (MessageType)",
        "Financial institution (ReportingFI Name)","File information")
      val listOfValues = Seq("c-8-new-f-va", "CRS", "EFG Bank plc", "New information")
      val listOfPara = Seq(s"We have sent a confirmation email to $paraContent.",
        "You can make any elections for EFG Bank plc in the service.",
        "We will contact you if we have any questions about your report.",
        "Your feedback helps us make our service better.",
        "Take a short survey to share your feedback on this service."
      )
      val listValues = Seq(
        "Print this page",
        "Upload another file for a financial institution",
        "Back to manage your CRS and FATCA reports"
      )

      val renderedHtml: HtmlFormat.Appendable = view(fileSummary,paraContent,date,time,true)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("File successfully sent")
      getPageHeading(doc) mustEqual "File successfully sent"
      getSubheadingText(doc,0) mustEqual "What happens next"
      getSubheadingText(doc,1) mustEqual "Before you go"
      validateListValues(getAllElements(doc, ".govuk-summary-list__key"), listOfKeys)
      validateListValues(getAllElements(doc, ".govuk-summary-list__value"), listOfValues)
      validateAllParaValues(getAllParagraph(doc).text(),listOfPara)
      validateListValues(getAllElements(doc, ".govuk-list"), listValues)
    }

    "should not render election para components" in {
      val submittedTime = LocalDateTime.parse("2025-09-12T12:01:00")
      val date = "12 September 2025"
      val time = "12:01am"
      val fileDetails = FileDetails("name.xml", "c-8-new-f-va", "CRS", "EFG Bank plc", "New information", submittedTime, LocalDateTime.now())
      val fileSummary = FileConfirmationViewModel.getSummaryRows(fileDetails)
      val paraContent = FileConfirmationViewModel.getEmailParagraphForFI("user1@email.com",None)


      val renderedHtml: HtmlFormat.Appendable = view(fileSummary,paraContent,date,time,false)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getAllParagraph(doc).text() mustNot include("You can make any elections for EFG Bank plc in the service.")
    }
  }

}
