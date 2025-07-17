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
import org.jsoup.select.Elements
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.FileErrorView

class FileErrorViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view1: FileErrorView                                              = app.injector.instanceOf[FileErrorView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "FileErrorView" - {

    "should render page components" in {

      val paragraphValues = Seq(
        "We cannot accept the file filename.xml because there is a problem with its formatting.",
        "Check that all:",
        "The file must also have one of the following:",
        "To make sure your XML file is formatted correctly, refer to the:"
      )

      val listValues = Seq(
        "element names are correct",
        "elements have an opening and a closing tag",
        "tags are correctly nested",
        "a CRS_OECD element with an XML namespace (xmlns) of \"urn:oecd:ties:crs:v3\" for CRS",
        "a FATCA_OECD element with an XML namespace (xmlns) of \"urn:oecd:ties:fatca:v2\" for FATCA",
        "CRS technical guidance for XML files",
        "FATCA technical guidance for XML files",
        "Print this page",
        "Upload a different file",
        "Back to manage your CRS and FATCA reports"
      )

      val renderedHtml: HtmlFormat.Appendable = view1("filename.xml")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("There is a problem with the formatting of your file")
      getPageHeading(doc) mustEqual "There is a problem with the formatting of your file"
      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)
      validateListValues(getAllElements(doc, ".govuk-list"), listValues)
      val linkElements = getAllElements(doc, ".govuk-link")
      val crsFILink    = linkElements.select(":contains(Back to manage your CRS and FATCA reports)").attr("href")
      linkElements.select(":contains(CRS technical guidance for XML files)").attr("href") mustEqual "#"
      linkElements.select(":contains(FATCA technical guidance for XML files)").attr("href") mustEqual "#"
      linkElements.select(":contains(Upload a different file)").attr("href") mustEqual "/report-for-crs-and-fatca/report/upload-file"
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
