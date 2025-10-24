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

package connectors

import config.FrontendAppConfig
import models.upscan.FileValidateRequest
import models.{
  Errors,
  FIIDDoesNotMatchSendCompanyInError,
  FIIDNotMatchingError,
  IncorrectMessageTypeError,
  InvalidMessageTypeError,
  InvalidReportingPeriodError,
  InvalidXmlFileError,
  MessageSpecData,
  NonFatalErrors,
  ReportingPeriodError,
  SchemaValidationErrors,
  SubmissionValidationFailure,
  SubmissionValidationResult,
  SubmissionValidationSuccess
}
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class ValidationConnector @Inject() (http: HttpClientV2, config: FrontendAppConfig) extends Logging {

  val url = url"${config.crsUrl}/crs-fatca-reporting/validate-submission"

  def sendForValidation(fileValidateRequest: FileValidateRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Errors, MessageSpecData]] =
    http
      .post(url)
      .withBody(Json.toJson(fileValidateRequest))
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case OK =>
              response.json.as[SubmissionValidationResult] match {
                case x: SubmissionValidationSuccess =>
                  Right(x.messageSpecData)
                case x: SubmissionValidationFailure =>
                  Left(SchemaValidationErrors(x.validationErrors, x.messageType))
                case _: FIIDDoesNotMatchSendCompanyInError =>
                  Left(FIIDNotMatchingError)
                case _: InvalidReportingPeriodError =>
                  Left(ReportingPeriodError)
                case _: InvalidMessageTypeError =>
                  Left(IncorrectMessageTypeError)
              }
            case status =>
              logger.warn(s"Unexpected response status $status: ${response.body}")
              Left(NonFatalErrors(s"Unexpected response status $status: ${response.body}"))
          }
      }
      .recover {
        case NonFatal(e) =>
          if (e.getMessage contains "InvalidXml") {
            logger.error(s"XML parsing failed. The XML parser in country-by-country-reporting backend has thrown the exception: $e")
            Left(InvalidXmlFileError)
          } else {
            logger.error(s"Remote service timed out. The XML parser in crs-fatca-reporting backend has thrown the exception", e)
            Left(NonFatalErrors(e.getMessage))
          }
      }
}
