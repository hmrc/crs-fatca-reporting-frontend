/*
 * Copyright 2026 HM Revenue & Customs
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

import models.{CRS, CRSReportType}
import models.{IntenalIssueError, NoResultFound, UnExpectedResponse, UnexpectedJsResult}
import models.fileDetails.FileDetails
import models.submission.ConversationId
import models.submission.fileDetails.{Pending, RejectedSDES}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.{an, mustBe}
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, REQUEST_TIMEOUT}
import utils.ISpecBase

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class FileDetailsConnectorSpec extends AnyFreeSpec with ISpecBase {
  lazy val connector: FileDetailsConnector = app.injector.instanceOf[FileDetailsConnector]

  "FileDetailsConnector" - {
    "getStatus" - {
      "should return FileStatus when backend returns OK with valid body" in {
        val conversationId = ConversationId("test-conversation-id")
        val url            = s"/crs-fatca-reporting/files/$conversationId/status"

        val responseBody =
          """
                |{
                |  "RejectedSDES": "{}}"
                |}
                |""".stripMargin

        stubGetResponse(url, OK, responseBody)

        val result = Await.result(connector.getStatus(conversationId), 2.seconds)

        result.get mustBe RejectedSDES
      }

      "should return None when backend returns NOT_FOUND" in {
        val conversationId = ConversationId("test-conversation-id")
        val url            = s"/crs-fatca-reporting/files/$conversationId/status"

        stubGetResponse(url, NOT_FOUND, "")

        val result = Await.result(connector.getStatus(conversationId), 2.seconds)

        assert(result.isEmpty)
      }

      "must return None when getStatus fails with Request Timeout" in {
        val conversationId = ConversationId("test-conversation-id")
        val url            = s"/crs-fatca-reporting/files/$conversationId/status"
        stubGetResponse(url, REQUEST_TIMEOUT)

        val result = connector.getStatus(conversationId)

        result.futureValue mustBe None

      }

    }
    
    "get file details" - {
      "get file details for a valid conversation id " in {
        val conversationId = ConversationId("conversation-123")
        val url = s"/crs-fatca-reporting/files/$conversationId/details"

        stubGetResponse(url, OK, getFileDetailsStubResponse)

        val result = connector.getFileDetails(conversationId)

        val submittedTime = LocalDateTime.of(2026, 1, 6, 12, 0, 0)
        val reportingDate = LocalDate.of(2026, 1, 1)


        result.futureValue mustBe FileDetails(
          _id = conversationId,
          enrolmentId = "XACBC0000123456",
          messageRefId = "GBXACBC12345678",
          reportingEntityName = "Test Entity",
          status = Pending,
          name = "test-file.xml",
          submitted = submittedTime,
          lastUpdated = submittedTime,
          reportingPeriod = reportingDate,
          messageType = CRS,
          reportType = CRSReportType.TestData,
          isFiUser = true,
          fiNameFromFim = "Test FI Name",
        )

      }

      "return a UnexpectedJsResult exception when 200 response returns an invalid body" in {
        val conversationId = ConversationId("conversation-123")
        val url = s"/crs-fatca-reporting/files/$conversationId/details"

        stubGetResponse(url, OK, """{"invalid": "response"}""")

        val result = connector.getFileDetails(conversationId)

        result.failed.futureValue mustBe an[UnexpectedJsResult.type]
      }

      "return a UnExpectedResponse when an expected success status code is returned" in {
        val conversationId = ConversationId("conversation-123")
        val url = s"/crs-fatca-reporting/files/$conversationId/details"

        stubGetResponse(url, CREATED, getFileDetailsStubResponse)

        val result = connector.getFileDetails(conversationId)

        result.failed.futureValue mustBe an[UnExpectedResponse.type]
      }

      "return a NoResultFound  when 404 response is returned" in {
        val conversationId = ConversationId("conversation-123")
        val url = s"/crs-fatca-reporting/files/$conversationId/details"

        stubGetResponse(url, NOT_FOUND, "")

        val result = connector.getFileDetails(conversationId)

        result.failed.futureValue mustBe an[NoResultFound.type]
      }

      "return a IntenalIssueError when 500 response is returned" in {
        val conversationId = ConversationId("conversation-123")
        val url = s"/crs-fatca-reporting/files/$conversationId/details"

        stubGetResponse(url, INTERNAL_SERVER_ERROR, "")

        val result = connector.getFileDetails(conversationId)

        result.failed.futureValue mustBe an[IntenalIssueError.type]
      }
    }
  }

  private def getFileDetailsStubResponse: String =
    """
      |{
      |  "_id": "conversation-123",
      |  "enrolmentId": "XACBC0000123456",
      |  "messageRefId": "GBXACBC12345678",
      |  "reportingEntityName": "Test Entity",
      |  "status": {"Pending":{}},
      |  "name": "test-file.xml",
      |  "submitted": "2026-01-06T12:00:00",
      |  "lastUpdated": "2026-01-06T12:00:00",
      |  "reportingPeriod": "2026-01-01",
      |  "messageType": "CRS",
      |  "reportType": "TEST_DATA",
      |  "fiNameFromFim": "Test FI Name",
      |  "isFiUser": true,
      |  "fileType":"NormalFile"
      |}
      |""".stripMargin
}
