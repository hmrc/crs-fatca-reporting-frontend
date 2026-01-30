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

import models.submission.ConversationId
import models.submission.fileDetails.RejectedSDES
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.http.Status.{NOT_FOUND, OK, REQUEST_TIMEOUT}
import utils.ISpecBase

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
        stubPostResponse(url, REQUEST_TIMEOUT)

        val result = connector.getStatus(conversationId)

        result.futureValue mustBe None

      }

    }
  }
}
