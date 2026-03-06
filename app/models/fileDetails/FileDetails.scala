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

import models.submission.ConversationId
import models.submission.fileDetails.FileStatus
import models.{CRS, CRSReportType, FATCA, FATCAReportType, MessageType, ReportType}
import play.api.libs.json.*
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

case class FileDetails(
  _id: ConversationId,
  enrolmentId: String,
  messageRefId: String,
  reportingEntityName: String,
  status: FileStatus,
  name: String,
  submitted: LocalDateTime,
  lastUpdated: LocalDateTime,
  reportingPeriod: LocalDate,
  messageType: MessageType,
  reportType: ReportType,
  fiNameFromFim: String,
  isFiUser: Boolean
)

object FileDetails {

  given format: OFormat[FileDetails] = {
    given reads: Reads[FileDetails] = Reads {
      json =>
        for {
          id                  <- (json \ "_id").validate[ConversationId]
          enrolmentId         <- (json \ "enrolmentId").validate[String]
          messageRefId        <- (json \ "messageRefId").validate[String]
          reportingEntityName <- (json \ "reportingEntityName").validate[String]
          status              <- (json \ "status").validate[FileStatus]
          name                <- (json \ "name").validate[String]
          submitted           <- (json \ "submitted").validate[LocalDateTime]
          lastUpdated         <- (json \ "lastUpdated").validate[LocalDateTime]
          reportingPeriod     <- (json \ "reportingPeriod").validate[LocalDate]
          messageType         <- (json \ "messageType").validate[MessageType]
          reportType          <- (json \ "reportType").validate[String]
          fiNameFromFim       <- (json \ "fiNameFromFim").validate[String]
          isFiUser            <- (json \ "isFiUser").validate[Boolean]
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
          fiNameFromFim = fiNameFromFim
        )
    }

    given writes: OWrites[FileDetails] = OWrites {
      fd =>
        Json.obj(
          "_id"                 -> fd._id,
          "enrolmentId"         -> fd.enrolmentId,
          "messageRefId"        -> fd.messageRefId,
          "reportingEntityName" -> fd.reportingEntityName,
          "status"              -> fd.status,
          "name"                -> fd.name,
          "submitted"           -> fd.submitted,
          "lastUpdated"         -> fd.lastUpdated,
          "reportingPeriod"     -> fd.reportingPeriod,
          "messageType"         -> fd.messageType,
          "messageType"         -> fd.messageType,
          "isFiUser"            -> fd.isFiUser,
          "fiNameFromFim"       -> fd.fiNameFromFim,
          "reportType" -> (fd.reportType match {
            case crsReportType: CRSReportType     => summon[Writes[CRSReportType]].writes(crsReportType)
            case fatcaReportType: FATCAReportType => summon[Writes[FATCAReportType]].writes(fatcaReportType)
          })
        )
    }
    OFormat(reads, writes)
  }

  val mongoFormat: OFormat[FileDetails] = {

    val localDateTimeReads: Reads[LocalDateTime] =
      Reads
        .at[String](__ \ "$date" \ "$numberLong")
        .map(
          date => Instant.ofEpochMilli(date.toLong).atZone(ZoneOffset.UTC).toLocalDateTime
        )

    val localDateTimeWrites: Writes[LocalDateTime] =
      Writes
        .at[String](__ \ "$date" \ "$numberLong")
        .contramap(_.toInstant(ZoneOffset.UTC).toEpochMilli.toString)

    given dateTimeFormat: Format[LocalDateTime] = Format(localDateTimeReads, localDateTimeWrites)

    given dateFormat: Format[LocalDate] = MongoJavatimeFormats.localDateFormat

    given mongoReads: Reads[FileDetails] = Reads {
      json =>
        for {
          id                  <- (json \ "_id").validate[ConversationId]
          enrolmentId         <- (json \ "enrolmentId").validate[String]
          messageRefId        <- (json \ "messageRefId").validate[String]
          reportingEntityName <- (json \ "reportingEntityName").validate[String]
          status              <- (json \ "status").validate[FileStatus]
          name                <- (json \ "name").validate[String]
          submitted           <- (json \ "submitted").validate[LocalDateTime]
          lastUpdated         <- (json \ "lastUpdated").validate[LocalDateTime]
          reportingPeriod     <- (json \ "reportingPeriod").validate[LocalDate]
          messageType         <- (json \ "messageType").validate[MessageType]
          reportType          <- (json \ "reportType").validate[String]
          isFiUser            <- (json \ "isFiUser").validate[Boolean]
          fiNameFromFim       <- (json \ "fiNameFromFim").validate[String]
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
          fiNameFromFim = fiNameFromFim
        )
    }

    given mongoWrites: OWrites[FileDetails] = OWrites {
      fd =>
        Json.obj(
          "_id"                 -> fd._id,
          "enrolmentId"         -> fd.enrolmentId,
          "messageRefId"        -> fd.messageRefId,
          "reportingEntityName" -> fd.reportingEntityName,
          "status"              -> fd.status,
          "name"                -> fd.name,
          "submitted"           -> fd.submitted,
          "lastUpdated"         -> fd.lastUpdated,
          "reportingPeriod"     -> fd.reportingPeriod,
          "messageType"         -> fd.messageType,
          "reportType" -> (fd.reportType match {
            case crsReportType: CRSReportType     => summon[Writes[CRSReportType]].writes(crsReportType)
            case fatcaReportType: FATCAReportType => summon[Writes[FATCAReportType]].writes(fatcaReportType)
          })
        )
    }

    OFormat(mongoReads, mongoWrites)
  }
}
