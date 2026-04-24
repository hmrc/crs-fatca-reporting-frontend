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
import models.fileDetails.{FileDetails, FileDetailsResult, FileValidationErrors}
import models.submission.ConversationId
import models.submission.fileDetails.{NotAccepted, Pending, Rejected, RejectedSDES, RejectedSDESVirus}
import models.{CRS, CRSReportType}
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import viewmodels.SubmissionChecksTableViewModel
import views.html.{FileErrorView, SubmissionsChecksView}

import java.time.{LocalDate, LocalDateTime}

class SubmissionsChecksViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view1: SubmissionsChecksView                                      = app.injector.instanceOf[SubmissionsChecksView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "SubmissionsChecksView" - {

    "should render page components" in {

      val paragraphValues = Seq(
        "We keep this information for 28 days from when you sent the report.",
        "Back to manage your CRS and FATCA reports"
      )

      val tableHeaderValues = Seq(
        "Financial institution name and FI ID",
        "Reporting period",
        "Regime",
        "MessageRefId",
        "Sent",
        "Result",
        "Next step"
      )

      val tableCellValueFirstRow = Seq(
        "Test FI Name some-company-in",
        "2027",
        "CRS",
        "GBXACBC12345678",
        "6 Jan 2026 12:13pm",
        "Passed",
        "Go to confirmation"
      )

      val renderedHtml: HtmlFormat.Appendable = view1(SubmissionChecksTableViewModel(viewModel))
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Results of submission checks")
      getPageHeading(doc) mustEqual "Results of submission checks"
      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)
      validateListValues(getAllElements(doc, ".govuk-table__header"), tableHeaderValues)
      validateListValues(getAllElements(doc, ".govuk-table__body .govuk-table__row:nth-child(1)"), tableCellValueFirstRow)

      val linkElements = getAllElements(doc, ".govuk-link")
      val crsFILink    = linkElements.select(":contains(Back to manage your CRS and FATCA reports)").attr("href")
      crsFILink mustEqual "http://localhost:10033/manage-your-crs-and-fatca-financial-institutions"

    }

    def viewModel = {
      val submittedTime  = LocalDateTime.of(2026, 1, 6, 12, 13, 54)
      val reportingDate  = LocalDate.of(2027, 1, 1)
      val conversationId = ConversationId("conversation-123")

      FileDetailsResult(
        Seq(
          FileDetails(
            _id = conversationId,
            enrolmentId = "XACBC0000123456",
            messageRefId = "GBXACBC12345678",
            reportingEntityName = Some("Test Entity"),
            status = models.submission.fileDetails.Accepted,
            name = "test-file.xml",
            submitted = submittedTime,
            lastUpdated = submittedTime,
            reportingPeriod = reportingDate,
            messageType = CRS,
            reportType = CRSReportType.TestData,
            isFiUser = true,
            fiNameFromFim = "Test FI Name",
            fiPrimaryContactEmail = Some("fiPrimary@email.com"),
            fiSecondaryContactEmail = Some("fiSecondary@email.com"),
            subscriptionPrimaryContactEmail = "test@email.com",
            subscriptionSecondaryContactEmail = Some("secondarySub@email.com"),
            sendingCompanyIn = "some-company-in"
          )
        ),
        pages = 1
      )
    }

  }
}
