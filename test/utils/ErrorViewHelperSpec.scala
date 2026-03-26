import models.{GenericError, Message}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import utils.ErrorViewHelper

class ErrorViewHelperSpec extends AnyFreeSpec with Matchers {

  private val mockMessages: Messages = mock[Messages]
  when(mockMessages.apply(any[String], any[Any]())).thenAnswer(
    invocation => invocation.getArgument(0)
  )

  private val helper = new ErrorViewHelper()

  "ErrorViewHelper" - {

    "generateTable" - {

      "default case" - {

        "generates a plain text row for an unrecognised message key" in {
          val error  = GenericError(lineNumber = 5, message = Message("xml.elem.unknown"))
          val result = helper.generateTable(Seq(error))(mockMessages)

          result.size shouldBe 1
          result.head.size shouldBe 2
          result.head.head.content shouldBe Text("5")
          result.head.head.classes shouldBe "govuk-table__cell--numeric"
          result.head.head.attributes shouldBe Map("id" -> "lineNumber_5")
          result.head(1).attributes shouldBe Map("id" -> "errorMessage_5")
        }

        "passes message args through to messages" in {
          val error = GenericError(lineNumber = 7, message = Message("xml.elem.unknown", List("arg1", "arg2")))
          helper.generateTable(Seq(error))(mockMessages)

          verify(mockMessages).apply("xml.elem.unknown", "arg1", "arg2")
        }
      }

      "xml.elem.reportingPeriod.invalid" - {

        "generates an HtmlContent row" in {
          val error  = GenericError(lineNumber = 10, message = Message("xml.elem.reportingPeriod.invalid"))
          val result = helper.generateTable(Seq(error))(mockMessages)
          val row    = result.head

          row.head.content shouldBe Text("10")
          row.head.attributes shouldBe Map("id" -> "lineNumber_10")
          row(1).attributes shouldBe Map("id" -> "errorMessage_10")
          row(1).content shouldBe a[HtmlContent]
        }

        "includes all expected message keys in the html" in {
          val error = GenericError(lineNumber = 10, message = Message("xml.elem.reportingPeriod.invalid"))
          val html  = helper.generateTable(Seq(error))(mockMessages).head(1).content.asInstanceOf[HtmlContent].value.body

          html should include("xml.elem.reportingPeriod.invalid")
          html should include("xml.elem.reportingPeriod.invalid.li1")
          html should include("xml.elem.reportingPeriod.invalid.li2")
          html should include("xml.elem.reportingPeriod.invalid.li3")
          html should include("xml.elem.reportingPeriod.invalid.p2")
          html should include("govuk-list--bullet")
        }
      }

      "xml.elem.DocRefId.max" - {

        "generates an HtmlContent row" in {
          val error  = GenericError(lineNumber = 20, message = Message("xml.elem.DocRefId.max"))
          val result = helper.generateTable(Seq(error))(mockMessages)
          val row    = result.head

          row.head.attributes shouldBe Map("id" -> "lineNumber_20")
          row(1).attributes shouldBe Map("id" -> "errorMessage_20")
          row(1).content shouldBe a[HtmlContent]
        }

        "includes all expected message keys in the html" in {
          val error = GenericError(lineNumber = 20, message = Message("xml.elem.DocRefId.max"))
          val html  = helper.generateTable(Seq(error))(mockMessages).head(1).content.asInstanceOf[HtmlContent].value.body

          html should include("xml.elem.DocRefId.max")
          html should include("xml.elem.DocRefId.max.li1")
          html should include("xml.elem.DocRefId.max.li2")
          html should include("xml.elem.DocRefId.max.li3")
          html should include("xml.elem.DocRefId.max.p2")
        }
      }

      "xml.elem.messageRefId.max" - {

        "generates an HtmlContent row" in {
          val error  = GenericError(lineNumber = 30, message = Message("xml.elem.messageRefId.max"))
          val result = helper.generateTable(Seq(error))(mockMessages)
          val row    = result.head

          row.head.attributes shouldBe Map("id" -> "lineNumber_30")
          row(1).attributes shouldBe Map("id" -> "errorMessage_30")
          row(1).content shouldBe a[HtmlContent]
        }

        "includes all expected message keys including all 7 list items in the html" in {
          val error = GenericError(lineNumber = 30, message = Message("xml.elem.messageRefId.max"))
          val html  = helper.generateTable(Seq(error))(mockMessages).head(1).content.asInstanceOf[HtmlContent].value.body

          html should include("xml.elem.messageRefId.max")
          (1 to 7).foreach(
            i => html should include(s"xml.elem.messageRefId.max.li$i")
          )
          html should include("xml.elem.messageRefId.max.p2")
        }
      }

      "lineNumber in element ids" - {

        "is set correctly across all message key branches" in {
          val keys = Seq(
            "xml.elem.reportingPeriod.invalid",
            "xml.elem.DocRefId.max",
            "xml.elem.messageRefId.max",
            "xml.elem.unknown"
          )

          keys.zipWithIndex.foreach {
            case (key, i) =>
              val lineNumber = i + 100
              val result     = helper.generateTable(Seq(GenericError(lineNumber, Message(key))))(mockMessages)
              val row        = result.head

              row.head.attributes shouldBe Map("id" -> s"lineNumber_$lineNumber")
              row(1).attributes shouldBe Map("id" -> s"errorMessage_$lineNumber")
          }
        }
      }

      "multiple errors" - {

        "generates a row for each error" in {
          val errors = Seq(
            GenericError(1, Message("xml.elem.reportingPeriod.invalid")),
            GenericError(2, Message("xml.elem.DocRefId.max")),
            GenericError(3, Message("xml.elem.messageRefId.max")),
            GenericError(4, Message("xml.elem.unknown"))
          )

          helper.generateTable(errors)(mockMessages).size shouldBe 4
        }

        "generates a row for each error more than 100" in {
          val errors = Seq.tabulate(101) {
            i =>
              GenericError(i, Message("xml.elem.unknown"))
          }
          helper.generateTable(errors)(mockMessages).size shouldBe 100
        }
      }
    }
  }
}
