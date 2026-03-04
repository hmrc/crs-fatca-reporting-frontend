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

import models.CRSReportType.NilReport
import models.messageKeyForReportType
import play.api.i18n.Messages

import java.time.LocalDateTime

case class  FileDetailsModel(name: String,
                            messageRefId: String,
                            messageType: String,
                            reportingEntityName: String,
                            fileInformation: String,
                            submitted: LocalDateTime,
                            lastUpdated: LocalDateTime,
                            isCrsNilReport: Boolean
)

object FileDetailsModel {

  def from(fileDetails: FileDetails)(using messages: Messages) =
    FileDetailsModel(
      name = fileDetails.name,
      messageRefId = fileDetails.messageRefId,
      messageType = fileDetails.messageType.toString,
      reportingEntityName = if (fileDetails.reportType == NilReport) fileDetails.fiNameFromFim else fileDetails.reportingEntityName,
      fileInformation = messages(messageKeyForReportType(fileDetails.reportType)),
      submitted = fileDetails.submitted,
      lastUpdated = fileDetails.lastUpdated,
      isCrsNilReport = fileDetails.reportType == NilReport
    )
}
