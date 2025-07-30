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
import views.html.ReportingPeriodErrorView

import java.time.{LocalDate, ZoneId}

class ReportingPeriodErrorViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view: ReportingPeriodErrorView                                    = app.injector.instanceOf[ReportingPeriodErrorView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))
  private val currentYear                               = LocalDate.now(ZoneId.of("Europe/London")).getYear

  "InvalidMessageTypeErrorView" - {

    "should render page components" in {
      val renderedHtml: HtmlFormat.Appendable = view(currentYear)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      val paragraphValues = Seq(
        s"The ReportingPeriod element in your file must contain a date between 31 December 2014 and 31 December $currentYear.",
        "Update the ReportingPeriod and upload the updated file."
      )

      getWindowTitle(doc) mustEqual "There is a problem with the reporting period in your file - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual "There is a problem with the reporting period in your file"
      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)
    }
  }

}
