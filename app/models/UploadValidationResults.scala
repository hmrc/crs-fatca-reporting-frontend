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

import play.api.libs.json.{__, Format, JsError, JsObject, JsResult, JsString, JsSuccess, JsValue, Json, OFormat, OWrites, Reads, Writes}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed trait SubmissionValidationResult

sealed trait MessageType

case object CRS extends MessageType

case object FATCA extends MessageType

object MessageType {

  def fromString(s: String): MessageType = s.toUpperCase match {
    case "CRS"   => CRS
    case "FATCA" => FATCA
    case _       => throw new NoSuchElementException
  }

  implicit val write: Writes[MessageType] = Writes[MessageType] {
    case CRS   => JsString("CRS")
    case FATCA => JsString("FATCA")
  }

  implicit val reads: Reads[MessageType] = Reads[MessageType] {
    case JsString("CRS")   => JsSuccess(CRS)
    case JsString("FATCA") => JsSuccess(FATCA)
    case value             => JsError(s"Unexpected value of _type: $value")
  }
}

case class MessageSpecData(messageType: MessageType,
                           sendingCompanyIN: String,
                           messageRefId: String,
                           reportingFIName: String,
                           reportingPeriod: LocalDate,
                           giin: Option[String] = None
)

object MessageSpecData {

  implicit val localDateFormat: Format[LocalDate] = Format(
    Reads.localDateReads("yyyy-MM-dd"),
    Writes.temporalWrites[LocalDate, DateTimeFormatter](DateTimeFormatter.ISO_LOCAL_DATE)
  )
  implicit val format: OFormat[MessageSpecData] = Json.format[MessageSpecData]
}

case class SubmissionValidationSuccess(messageSpecData: MessageSpecData) extends SubmissionValidationResult

object SubmissionValidationSuccess {
  implicit val format: OFormat[SubmissionValidationSuccess] = Json.format[SubmissionValidationSuccess]
}

case class SubmissionValidationFailure(validationErrors: ValidationErrors, messageType: String) extends SubmissionValidationResult

object SubmissionValidationFailure {
  implicit val format: OFormat[SubmissionValidationFailure] = Json.format[SubmissionValidationFailure]
}

case class FIIDDoesNotMatchSendCompanyInError(error: String) extends SubmissionValidationResult

object FIIDDoesNotMatchSendCompanyInError {
  implicit val format: OFormat[FIIDDoesNotMatchSendCompanyInError] = Json.format[FIIDDoesNotMatchSendCompanyInError]
}

case class InvalidReportingPeriodError(error: String) extends SubmissionValidationResult

object InvalidReportingPeriodError {
  implicit val format: OFormat[InvalidReportingPeriodError] = Json.format[InvalidReportingPeriodError]
}

case class InvalidMessageTypeError(error: String = "Invalid message type") extends SubmissionValidationResult

object InvalidMessageTypeError {

  implicit val format: OFormat[InvalidMessageTypeError] = new OFormat[InvalidMessageTypeError] {
    def reads(json: JsValue): JsResult[InvalidMessageTypeError] =
      JsSuccess(InvalidMessageTypeError())
    def writes(o: InvalidMessageTypeError): JsObject =
      Json.obj()
  }
}

case class ValidationErrors(errors: Seq[GenericError], boolean: Option[Boolean])

object ValidationErrors {
  implicit val format: OFormat[ValidationErrors] = Json.format[ValidationErrors]
}

object SubmissionValidationResult {
  private val successFmt                         = SubmissionValidationSuccess.format
  private val failureFmt                         = SubmissionValidationFailure.format
  private val invalidReportingPeriod             = InvalidReportingPeriodError.format
  private val fIIDDoesNotMatchSendCompanyInError = FIIDDoesNotMatchSendCompanyInError.format

  implicit val format: OFormat[SubmissionValidationResult] = {
    val reads: Reads[SubmissionValidationResult] =
      (__ \ "type").read[String].flatMap {
        case "Success" =>
          Reads(
            js => successFmt.reads(js)
          )
        case "ValidationFailure" =>
          Reads(
            js => failureFmt.reads(js)
          )
        case "InvalidMessageType" =>
          Reads(
            _ => JsSuccess(InvalidMessageTypeError())
          )
        case "InvalidReportingPeriod" =>
          Reads(
            js => invalidReportingPeriod.reads(js)
          )
        case "InvalidFIID" =>
          Reads(
            js => fIIDDoesNotMatchSendCompanyInError.reads(js)
          )
        case other =>
          Reads(
            _ => JsError(s"Unknown SubmissionValidationResult type '$other'")
          )
      }

    val writes: OWrites[SubmissionValidationResult] = OWrites {
      case s: SubmissionValidationSuccess        => successFmt.writes(s) + ("type"                         -> JsString("Success"))
      case f: SubmissionValidationFailure        => failureFmt.writes(f) + ("type"                         -> JsString("ValidationFailure"))
      case e: InvalidReportingPeriodError        => invalidReportingPeriod.writes(e) + ("type"             -> JsString("InvalidReportingPeriod"))
      case e: FIIDDoesNotMatchSendCompanyInError => fIIDDoesNotMatchSendCompanyInError.writes(e) + ("type" -> JsString("InvalidFIID"))
      case _: InvalidMessageTypeError            => Json.obj("type" -> JsString("InvalidMessageType"))
    }

    OFormat(reads, writes)
  }
}

case class ValidatedFileData(fileName: String, messageSpecData: MessageSpecData, fileSize: Long, checksum: String)

object ValidatedFileData {
  implicit val format: OFormat[ValidatedFileData] = Json.format[ValidatedFileData]
}

sealed trait Errors
case class NonFatalErrors(e: String) extends Errors
case object InvalidXmlFileError extends Errors
case object ReportingPeriodError extends Errors
case object FIIDNotMatchingError extends Errors
case object IncorrectMessageTypeError extends Errors
case class SchemaValidationErrors(validationErrors: ValidationErrors, messageType: String) extends Errors
