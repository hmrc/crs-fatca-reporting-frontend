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

package utils

import base.SpecBase
import models.fileDetails.{ContactInfo, FileDetails}
import models.submission.ConversationId
import models.submission.fileDetails.Accepted
import models.{CRS, CRSReportType}

import java.time.{LocalDate, LocalDateTime}

class UrlHelpersSpec extends SpecBase {

  val submittedTime  = LocalDateTime.parse("2025-09-12T12:01:00")
  val reportingDate  = LocalDate.of(2024, 1, 1)
  val conversationId = ConversationId("conversation-123")

  "FileConfirmation Controller" - {
    val fileDetails = FileDetails(
      _id = conversationId,
      enrolmentId = "XACBC0000123456",
      messageRefId = "c-8-new-f-va",
      reportingEntityName = Some("Some-fi-name"),
      status = Accepted,
      name = "name.xml",
      submitted = submittedTime,
      lastUpdated = submittedTime,
      reportingPeriod = reportingDate,
      messageType = CRS,
      reportType = CRSReportType.NewInformation,
      isFiUser = true,
      fiNameFromFim = "Test Company & Co.",
      fiPrimaryContact = None,
      fiSecondaryContact = None,
      subscriptionPrimaryContact = ContactInfo("testUser", "test@email.com"),
      subscriptionSecondaryContact = None,
      electionSubmitted = Some(false),
      sendingCompanyIn = "123456789"
    )

    "createManageReportPath" - {
      "return the correct path with encoded parameters" in {
        val expectedPath = "elections/manage-elections-for-2024?fiId=123456789&fiName=Test+Company+%26+Co."
        createManageElectionsPath(fileDetails) mustEqual expectedPath
      }
    }
  }
}
