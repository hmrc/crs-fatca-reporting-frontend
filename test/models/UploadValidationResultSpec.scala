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

package models

import base.SpecBase
import play.api.libs.json.Json
import org.scalatest.matchers.should.Matchers.shouldBe

import java.time.LocalDate

class UploadValidationResultSpec extends SpecBase {

  "deserialize UploadValidationResult" - {
    "deserialize SubmissionValidationSuccess correctly" in {
      val json = Json.parse("""
          |{
          |  "type": "Success",
          |  "messageSpecData": {
          |    "messageType": "CRS",
          |    "sendingCompanyIN": "COMP123",
          |    "messageRefId": "MSGREF001",
          |    "reportingFIName": "Test FI",
          |    "reportingPeriod": "2024-01-01",
          |    "giin": "GIIN123",
          |    "fiName": "fi-name"
          |  }
          |}
            """.stripMargin)

      val result = json.validate[SubmissionValidationResult]
      result.isSuccess shouldBe true
      result.get mustEqual SubmissionValidationSuccess(
        MessageSpecData(
          messageType = CRS,
          sendingCompanyIN = "COMP123",
          messageRefId = "MSGREF001",
          reportingFIName = "Test FI",
          reportingPeriod = LocalDate.parse("2024-01-01"),
          giin = Some("GIIN123"),
          fiName = "fi-name"
        )
      )
    }

    "deserialize SubmissionValidationFailure with ValidationErrors correctly" in {
      val json = Json.parse("""
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
        """.stripMargin)

      val result = json.validate[SubmissionValidationResult]
      result.isSuccess shouldBe true
      result.get mustEqual SubmissionValidationFailure(
        validationErrors = ValidationErrors(
          errors = Seq(
            GenericError(176, Message("xml.empty.field", List("Entity"))),
            GenericError(258, Message("xml.add.a.element", List("Summary")))
          ),
          boolean = None
        ),
        messageType = "CRS"
      )
    }

    "deserialize FIIDDoesNotMatchSendCompanyInError correctly" in {
      val json = Json.parse("""
          |{
          |  "error": "The FI ID in your file does not match any financial institutions in the service",
          |  "type": "InvalidFIID"
          |}
          |""".stripMargin)

      val result = json.validate[SubmissionValidationResult]
      result.isSuccess shouldBe true
      result.get mustEqual FIIDDoesNotMatchSendCompanyInError("The FI ID in your file does not match any financial institutions in the service")

    }

    "deserialize InvalidReportingPeriodError correctly" in {
      val json = Json.parse("""
          |{
          |  "error": "Invalid reporting period",
          |  "type": "InvalidReportingPeriod"
          |}
          |""".stripMargin)

      val result = json.validate[SubmissionValidationResult]
      result.isSuccess shouldBe true
      result.get mustEqual InvalidReportingPeriodError("Invalid reporting period")
    }

    "deserialize InvalidMessageTypeError correctly" in {
      val json = Json.parse("""
          |{
          |  "error": "Invalid message type",
          |  "type": "InvalidMessageType"
          |}
          |""".stripMargin)

      val result = json.validate[SubmissionValidationResult]
      result.isSuccess shouldBe true
      result.get mustEqual InvalidMessageTypeError("Invalid message type")
    }
  }
}
