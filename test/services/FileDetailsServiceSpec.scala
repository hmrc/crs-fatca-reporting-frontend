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

package services

import base.SpecBase
import connectors.FileDetailsConnector
import models.fileDetails.{FileDetails, FileDetailsResult}
import models.{CRS, CRSReportType, IntenalIssueError, NoResultFound, UnexpectedJsResult}
import models.submission.ConversationId
import models.submission.fileDetails.Pending
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import org.mockito.Mockito
import org.mockito.Mockito.*

import java.time.{LocalDate, LocalDateTime}

class FileDetailsServiceSpec extends SpecBase {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val mockFileDetailsConnector   = mock[FileDetailsConnector]
  val conversationId             = ConversationId("test-conversation-id")
  val submittedTime              = LocalDateTime.of(2026, 1, 6, 12, 0, 0)
  val reportingDate              = LocalDate.of(2026, 1, 1)

  val fileDetails = FileDetails(
    _id = conversationId,
    enrolmentId = "XACBC0000123456",
    messageRefId = "GBXACBC12345678",
    reportingEntityName = Some("Test Entity"),
    status = Pending,
    name = "test-file.xml",
    submitted = submittedTime,
    lastUpdated = submittedTime,
    reportingPeriod = reportingDate,
    messageType = CRS,
    reportType = CRSReportType.TestData,
    isFiUser = true,
    fiNameFromFim = "Test FI Name",
    subscriptionPrimaryContactEmail = "some@email.com",
    sendCompanyIn = "some-company-in"
  )

  "getFileDetails" - {
    "return FileDetails when the connector call is successful" in {
      val service = new FileDetailsService(mockFileDetailsConnector)
      when(mockFileDetailsConnector.getFileDetails(any[ConversationId])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(fileDetails))

      val result = service.getFileDetails(conversationId).futureValue
      result mustBe Some(fileDetails)
    }

    "return None when the connector call fails" in {
      List(UnexpectedJsResult, UnexpectedJsResult, NoResultFound, IntenalIssueError).foreach {
        exception =>
          val service = new FileDetailsService(mockFileDetailsConnector)
          when(mockFileDetailsConnector.getFileDetails(any[ConversationId])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.failed(exception))

          val result = service.getFileDetails(conversationId).futureValue
          result mustBe None
      }

    }
  }

  "get all file details" - {
    val subscriptionId = "XACBC0000123456"
    "return a FileDetailsResult with a list of files and expected pages when the connector call is successful" in {
      val fileDetailsResult = FileDetailsResult(Seq(fileDetails), 1, 1)
      val service           = new FileDetailsService(mockFileDetailsConnector)
      when(mockFileDetailsConnector.getAllFileDetails(any[String], any[Int]())(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(fileDetailsResult))

      val result = service.getAllFileDetails(subscriptionId).futureValue
      result mustBe fileDetailsResult
    }

    "return an empty FileDetailsResult when the connector call fails" in {
      val service = new FileDetailsService(mockFileDetailsConnector)
      when(mockFileDetailsConnector.getAllFileDetails(any[String], any[Int]())(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(new Exception("Connector failure")))

      val result = service.getAllFileDetails(subscriptionId).futureValue
      result mustBe FileDetailsResult(Seq.empty, 0, 0)
    }
  }
}
