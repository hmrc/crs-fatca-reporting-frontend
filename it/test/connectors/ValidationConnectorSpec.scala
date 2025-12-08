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

package connectors

import models.upscan.FileValidateRequest
import models.{CRS, Errors, FIIDNotMatchingError, GenericError, IncorrectMessageTypeError, InvalidXmlFileError, Message, MessageSpecData, NonFatalErrors, ReportingPeriodError, SchemaValidationErrors, ValidationErrors}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import utils.ISpecBase

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class ValidationConnectorSpec extends AnyFreeSpec with ISpecBase {

  lazy val connector: ValidationConnector = app.injector.instanceOf[ValidationConnector]

  private val fileValidateRequest =
    FileValidateRequest(url = "/some-url", conversationId = "conversation-id", subscriptionId = "subscription-id", fileReferenceId = "file-reference-id")

  "ValidationConnector" - {
    "must return messageSpec when status is okay  response is for a successful upload" in new TestContext {
      stubPostResponse(
        s"/crs-fatca-reporting/validate-submission",
        OK,
        body = messageSpecResponse
      )
      val result: Either[Errors, MessageSpecData] = Await.result(connector.sendForValidation(fileValidateRequest), 2.seconds)
      result.isRight mustBe true
      result.getOrElse(fail("Expected Right but got Left")) mustBe MessageSpecData(
        messageType = CRS,
        sendingCompanyIN = "COMP123",
        messageRefId = "MSGREF001",
        reportingFIName = "Test FI",
        reportingPeriod = LocalDate.parse("2024-01-01"),
        giin = Some("GIIN123"),
        fiNameFromFim = "fi-name",
        electionsRequired = true
      )
    }

    "must return schemavalidationErrors when status is okay response is for schemavalidation errors" in new TestContext {

      stubPostResponse(
        s"/crs-fatca-reporting/validate-submission",
        OK,
        body = schemeValidationError
      )
      private val result = Await.result(connector.sendForValidation(fileValidateRequest), 2.seconds)

      result.isLeft mustBe true
      result.left.getOrElse(fail("Expecting value in left")) mustBe SchemaValidationErrors(
        ValidationErrors(
          List(
            GenericError(176, Message("xml.empty.field", List("Entity"))),
            GenericError(258, Message("xml.add.a.element", List("Summary")))
          ),
          None
        ),
        "CRS"
      )
    }

    "must return FIIDNotMatchingError when status is okay and response is for fiid not matching errors" in new TestContext {

      stubPostResponse(
        s"/crs-fatca-reporting/validate-submission",
        OK,
        body = fiidErrorResponse
      )
      val result: Either[Errors, MessageSpecData] = Await.result(connector.sendForValidation(fileValidateRequest), 2.seconds)

      result.isLeft mustBe true
      result.left.getOrElse(fail("Expecting value in left")) mustBe FIIDNotMatchingError
    }

    "must return ReportingPeriodError when status is okay and response is for invalid reporting date" in new TestContext {

      stubPostResponse(
        s"/crs-fatca-reporting/validate-submission",
        OK,
        body = reportingPeriodResponse
      )
      val result: Either[Errors, MessageSpecData] = Await.result(connector.sendForValidation(fileValidateRequest), 2.seconds)

      result.isLeft mustBe true
      result.left.getOrElse(fail("Expecting value in left")) mustBe ReportingPeriodError
    }

    "must return InvalidMessageTypeError when status is ok request and response is for invalid message type" in new TestContext {

      stubPostResponse(
        s"/crs-fatca-reporting/validate-submission",
        OK,
        body = invalidMessageTypeResponse
      )
      val result: Either[Errors, MessageSpecData] = Await.result(connector.sendForValidation(fileValidateRequest), 2.seconds)

      result.isLeft mustBe true
      result.left.getOrElse(fail("Expecting value in left")) mustBe IncorrectMessageTypeError
    }

    "must return invalid xml when status is bad request request and response is for invalid xml" in new TestContext {

      stubPostResponse(
        s"/crs-fatca-reporting/validate-submission",
        BAD_REQUEST,
        body = invalidXmlResponse
      )
      val result: Either[Errors, MessageSpecData] = Await.result(connector.sendForValidation(fileValidateRequest), 2.seconds)

      result.isLeft mustBe true
      result.left.getOrElse(fail("Expecting value in left")) mustBe InvalidXmlFileError
    }

    "must return non fatal error for a internal server response" in new TestContext {

      stubPostResponse(
        s"/crs-fatca-reporting/validate-submission",
        INTERNAL_SERVER_ERROR,
        body = "invalidXmlResponse"
      )
      val result: Either[Errors, MessageSpecData] = Await.result(connector.sendForValidation(fileValidateRequest), 2.seconds)

      result.isLeft mustBe true
      result.left.getOrElse(fail("Expecting value in left")).isInstanceOf[NonFatalErrors] mustBe true
    }

  }

  trait TestContext {

    val messageSpecResponse: String =
      """
        |{
        |  "type": "Success",
        |  "messageSpecData": {
        |    "messageType": "CRS",
        |    "sendingCompanyIN": "COMP123",
        |    "messageRefId": "MSGREF001",
        |    "reportingFIName": "Test FI",
        |    "reportingPeriod": "2024-01-01",
        |    "giin": "GIIN123",
        |    "fiNameFromFim": "fi-name",
        |    "electionsRequired": true
        |  }
        |}
            """.stripMargin

    val schemeValidationError: String =
      """
        |{
        |  "validationErrors": {
        |    "errors": [
        |      {
        |        "lineNumber": 176,
        |        "message": {
        |          "messageKey": "xml.empty.field",
        |          "args": [
        |            "Entity"
        |          ]
        |        }
        |      },
        |      {
        |        "lineNumber": 258,
        |        "message": {
        |          "messageKey": "xml.add.a.element",
        |          "args": [
        |            "Summary"
        |          ]
        |        }
        |      }
        |    ]
        |  },
        |  "messageType": "CRS",
        |  "type": "ValidationFailure"
        |}
        """.stripMargin

    val fiidErrorResponse: String =
      """
        |{
        |  "error": "The FI ID in your file does not match any financial institutions in the service",
        |  "type": "InvalidFIID"
        |}
        |""".stripMargin

    val reportingPeriodResponse: String =
      """
        |{
        |  "error": "Invalid reporting period",
        |  "type": "InvalidReportingPeriod"
        |}
        |""".stripMargin

    val invalidMessageTypeResponse: String =
      """
        |{
        |  "error": "Invalid message type",
        |  "type": "InvalidMessageType"
        |}
        |""".stripMargin

    val invalidXmlResponse: String =
      """
        |{
        |  "error": "Invalid xml",
        |  "type": "InvalidXml"
        |}
        |""".stripMargin

  }

}
