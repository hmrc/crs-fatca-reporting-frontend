/*
 * Copyright 2024 HM Revenue & Customs
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

import models.submission.fileDetails.FileStatus
import models.submission.{ConversationId, GiinAndElectionDBStatus}
import models.{CRS, CRSReportType, FATCA, FATCAReportType, MessageType, ReportType}
import play.api.libs.json.*

import java.time.{LocalDate, LocalDateTime}

case class FileDetails(
  _id: ConversationId,
  enrolmentId: String,
  messageRefId: String,
  reportingEntityName: Option[String],
  status: FileStatus,
  name: String,
  submitted: LocalDateTime,
  lastUpdated: LocalDateTime,
  reportingPeriod: LocalDate,
  messageType: MessageType,
  reportType: ReportType,
  fiNameFromFim: String,
  isFiUser: Boolean,
  fiPrimaryContactEmail: Option[String] = None,
  fiSecondaryContactEmail: Option[String] = None,
  subscriptionPrimaryContactEmail: String,
  subscriptionSecondaryContactEmail: Option[String] = None,
  errors: Option[FileValidationErrors] = None,
  giinAndElectionDBStatus: Option[GiinAndElectionDBStatus] = None
)

object FileDetails {

  given format: OFormat[FileDetails] = {
    given reads: Reads[FileDetails] = Reads {
      json =>
        for {
          id                                <- (json \ "_id").validate[ConversationId]
          enrolmentId                       <- (json \ "enrolmentId").validate[String]
          messageRefId                      <- (json \ "messageRefId").validate[String]
          reportingEntityName               <- (json \ "reportingEntityName").validateOpt[String]
          status                            <- (json \ "status").validate[FileStatus]
          name                              <- (json \ "name").validate[String]
          submitted                         <- (json \ "submitted").validate[LocalDateTime]
          lastUpdated                       <- (json \ "lastUpdated").validate[LocalDateTime]
          reportingPeriod                   <- (json \ "reportingPeriod").validate[LocalDate]
          messageType                       <- (json \ "messageType").validate[MessageType]
          reportType                        <- (json \ "reportType").validate[String]
          fiNameFromFim                     <- (json \ "fiNameFromFim").validate[String]
          isFiUser                          <- (json \ "isFiUser").validate[Boolean]
          fiPrimaryContactEmail             <- (json \ "fiPrimaryContactEmail").validateOpt[String]
          fiSecondaryContactEmail           <- (json \ "fiSecondaryContactEmail").validateOpt[String]
          subscriptionPrimaryContactEmail   <- (json \ "subscriptionPrimaryContactEmail").validate[String]
          subscriptionSecondaryContactEmail <- (json \ "subscriptionSecondaryContactEmail").validateOpt[String]
          errors                            <- (json \ "errors").validateOpt[FileValidationErrors]
          giinAndElectionDBStatus           <- (json \ "giinAndElectionDBStatus").validateOpt[GiinAndElectionDBStatus]
          reportTypeValue <- messageType match {
            case CRS   => summon[Reads[CRSReportType]].reads(JsString(reportType))
            case FATCA => summon[Reads[FATCAReportType]].reads(JsString(reportType))
          }
        } yield FileDetails(
          _id = id,
          enrolmentId = enrolmentId,
          messageRefId = messageRefId,
          reportingEntityName = reportingEntityName,
          status = status,
          name = name,
          submitted = submitted,
          lastUpdated = lastUpdated,
          reportingPeriod = reportingPeriod,
          messageType = messageType,
          reportType = reportTypeValue,
          isFiUser = isFiUser,
          fiNameFromFim = fiNameFromFim,
          fiPrimaryContactEmail = fiPrimaryContactEmail,
          fiSecondaryContactEmail = fiSecondaryContactEmail,
          subscriptionPrimaryContactEmail = subscriptionPrimaryContactEmail,
          subscriptionSecondaryContactEmail = subscriptionSecondaryContactEmail,
          errors = errors,
          giinAndElectionDBStatus = giinAndElectionDBStatus
        )
    }

    given writes: OWrites[FileDetails] = OWrites {
      fd =>
        Json.obj(
          "_id"                               -> fd._id,
          "enrolmentId"                       -> fd.enrolmentId,
          "messageRefId"                      -> fd.messageRefId,
          "reportingEntityName"               -> fd.reportingEntityName,
          "status"                            -> fd.status,
          "name"                              -> fd.name,
          "submitted"                         -> fd.submitted,
          "lastUpdated"                       -> fd.lastUpdated,
          "reportingPeriod"                   -> fd.reportingPeriod,
          "messageType"                       -> fd.messageType,
          "isFiUser"                          -> fd.isFiUser,
          "fiNameFromFim"                     -> fd.fiNameFromFim,
          "fiPrimaryContactEmail"             -> fd.fiPrimaryContactEmail,
          "fiSecondaryContactEmail"           -> fd.fiSecondaryContactEmail,
          "subscriptionPrimaryContactEmail"   -> fd.subscriptionPrimaryContactEmail,
          "subscriptionSecondaryContactEmail" -> fd.subscriptionSecondaryContactEmail,
          "errors"                            -> fd.errors,
          "giinAndElectionDBStatus"           -> fd.giinAndElectionDBStatus,
          "reportType" -> (fd.reportType match {
            case crsReportType: CRSReportType     => summon[Writes[CRSReportType]].writes(crsReportType)
            case fatcaReportType: FATCAReportType => summon[Writes[FATCAReportType]].writes(fatcaReportType)
          })
        )
    }
    OFormat(reads, writes)
  }
}
