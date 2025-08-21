package views

import base.SpecBase
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.VirusFoundView

class VirusFoundViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {
  val view: VirusFoundView = app.injector.instanceOf[VirusFoundView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "VirusFoundView" - {

    "should render page components" in {
      val renderedHtml: HtmlFormat.Appendable = view()
      lazy val doc = Jsoup.parse(renderedHtml.body)

      val paragraphValues = Seq(
        "We cannot accept this file as it contains a virus.",
        "Upload a different file",
        "Back to manage your CRS and FATCA reports"
      )

      getWindowTitle(doc) mustEqual "There is a virus in your file - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual "There is a virus in your file"
      validateAllParaValues(getAllParagraph(doc).text(), paragraphValues)

      val linkElements = getAllElements(doc, ".govuk-link")
      linkElements.select(":contains(Upload a different file)").attr("href") mustEqual "/report-for-crs-and-fatca/report/upload-file"
      linkElements.select(":contains(Back to manage your CRS and FATCA reports)").attr("href") mustEqual "http://localhost:10033/manage-your-crs-and-fatca-financial-institutions"

    }
  }

}
