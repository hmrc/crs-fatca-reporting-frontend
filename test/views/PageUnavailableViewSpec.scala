package views

import base.SpecBase
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.PageUnavailableView

class PageUnavailableViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view: PageUnavailableView                                         = app.injector.instanceOf[PageUnavailableView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "PageUnavailableView" - {

    "should render page components" in {
      val renderedHtml: HtmlFormat.Appendable = view()
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) mustEqual "This page is no longer available - Send a CRS or FATCA report - GOV.UK"
      getPageHeading(doc) mustEqual "This page is no longer available"

      val linkElements = getAllElements(doc, ".govuk-link")
      val crsFILink    = linkElements.select(":contains(Back to manage your CRS and FATCA reports)").attr("href")
      crsFILink mustEqual "http://localhost:10033/manage-your-crs-and-fatca-financial-institutions"

    }
  }
}
