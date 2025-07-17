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
import models.{GenericError, Message}
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.{ErrorViewHelper, ViewHelper}
import views.html.DataErrorsView

class DataErrorsViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view1: DataErrorsView                                             = app.injector.instanceOf[DataErrorsView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val errorViewHelper: ErrorViewHelper = app.injector.instanceOf[ErrorViewHelper]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "DataErrorsView" - {

    val errors = Seq(GenericError(12345, Message("error1")), GenericError(2, Message("error2")))

    "should render page components for CRS regimeType with under 100 errors" in {

      val paragraphValues = Seq(
        "We cannot accept the file PlaceHolder file name because it does not meet the CRS data requirements.",
      )

      val listValues = Seq(
        "Print this page",
        "View the errors on this page",
        "Refer to the CRS technical guidance for XML files and update your file",
        "Upload the file"
      )

      val renderedHtml: HtmlFormat.Appendable = view1(
        errorViewHelper.generateTable(errors),
        "PlaceHolder file name",
        "dataErrors.listCRSLink",
        "CRS",
        2
      )

      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("There is a problem with your file data")
      getPageHeading(doc) mustEqual "There is a problem with your file data"
      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)
      validateListValues(getAllElements(doc, ".govuk-list"), listValues)
      val linkElements = getAllElements(doc, ".govuk-link")
      val crsFILink    = linkElements.select(":contains(Back to manage your CRS and FATCA reports)").attr("href")
      linkElements.select(":contains(CRS technical guidance for XML files)").attr("href") mustEqual "#"
      linkElements.select(":contains(Upload the file)").attr("href") mustEqual "/report-for-crs-and-fatca/report/upload-file"
      crsFILink mustEqual "http://localhost:10033/manage-your-crs-and-fatca-financial-institutions"
    }

    def validateAllParaValues(allParaValues: String, paraValues: Seq[String]): Unit =
      paraValues.foreach(
        p => allParaValues must include(p)
      )

    def validateListValues(elements: Elements, listValues: Seq[String]): Unit = {
      val allListValues = elements.text()
      listValues.foreach(
        values => allListValues must include(values)
      )
    }

  }

}
