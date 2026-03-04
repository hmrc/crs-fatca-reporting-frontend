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

package models.fileDetails

import base.SpecBase
import models.submission.ConversationId
import models.submission.fileDetails.Pending
import models.{CRS, CRSReportType}
import play.api.i18n.Messages.implicitMessagesProviderToMessages
import play.api.i18n.Messages

import java.time.{LocalDate, LocalDateTime}

class FileDetailsModelSpec extends SpecBase {

  "Create FileDetailsModel from FileDetails" in {
    implicit val msgs: Messages = messages(app)

    val conversationId = ConversationId("conversation-123")
    val submittedTime  = LocalDateTime.of(2026, 1, 6, 12, 0, 0)
    val reportingDate  = LocalDate.of(2026, 1, 1)
    val fileDetails = FileDetails(
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
      reportType = CRSReportType.TestData
    )

    val fileDetailsModel = FileDetailsModel(
      name = "test-file.xml",
      messageRefId = "GBXACBC12345678",
      messageType = "CRS",
      reportingEntityName = "Test Entity",
      fileInformation = "Test data",
      submitted = submittedTime,
      lastUpdated = submittedTime
    )

    FileDetailsModel.from(fileDetails) mustEqual fileDetailsModel
  }
}
