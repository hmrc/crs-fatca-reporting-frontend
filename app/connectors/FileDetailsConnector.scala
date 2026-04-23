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

import config.FrontendAppConfig
import models.fileDetails.FileDetails
import models.submission.fileDetails.FileStatus
import models.submission.{ConversationId, GiinAndElectionDBStatus}
import models.{IntenalIssueError, NoResultFound, UnExpectedResponse, UnexpectedJsResult}
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.*
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class FileDetailsConnector @Inject() (httpClient: HttpClientV2, config: FrontendAppConfig) extends Logging {

  def getStatus(conversationId: ConversationId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[FileStatus]] = {
    val url = url"${config.crsFatcaBackendUrl}/crs-fatca-reporting/files/${conversationId.value}/status"

    httpClient
      .get(url)
      .execute[HttpResponse]
      .map {
        case responseMessage if is2xx(responseMessage.status) =>
          responseMessage.json.asOpt[FileStatus]
        case _ =>
          logger.warn("FileDetailsConnector: Failed to getStatus")
          None
      }
      .recover {
        case NonFatal(e) =>
          logger.error(s"FileDetailsConnector: Exception occurred while getting status for conversationId: ${conversationId.value}", e)
          None
      }

  }

  def updateGiinAndElectionStatus(conversationId: ConversationId, status: GiinAndElectionDBStatus)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Unit] = {
    val url = url"${config.crsFatcaBackendUrl}/crs-fatca-reporting/files/${conversationId.value}/giin-election-status"

    httpClient
      .put(url)
      .withBody(Json.toJson(status))
      .execute[HttpResponse]
      .map {
        case response if is2xx(response.status) => ()
        case response =>
          logger.error(
            s"FileDetailsConnector: Failed to update GiinAndElectionStatus for conversationId: ${conversationId.value} with status ${response.status}"
          )
      }
      .recover {
        case NonFatal(e) =>
          logger.error(s"FileDetailsConnector: Exception occurred while updating GiinAndElectionStatus for conversationId: ${conversationId.value}", e)
      }
  }

  def getFileDetails(conversationId: ConversationId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[FileDetails] = {
    val url = url"${config.crsFatcaBackendUrl}/crs-fatca-reporting/files/${conversationId.value}/details"
    httpClient.get(url).execute[HttpResponse].flatMap(handleResponse(conversationId, _))
  }

  private def handleResponse(conversationId: ConversationId, response: HttpResponse): Future[FileDetails] =
    response.status match {
      case OK => parseFileDetails(conversationId, response)
      case NOT_FOUND =>
        logger.warn(s"FileDetailsConnector: File details does not exist for conversationId=${conversationId.value}")
        Future.failed(NoResultFound)
      case status if unExpected2xxStatus(status) =>
        logger.error(s"FileDetailsConnector: Failed to get fileDetails for conversationId: ${conversationId.value} of unexpected status $status")
        Future.failed(UnExpectedResponse)
      case status =>
        logger.error(s"FileDetailsConnector: Failed to get fileDetails for conversationId: ${conversationId.value} with status $status")
        Future.failed(IntenalIssueError)
    }

  private def parseFileDetails(conversationId: ConversationId, response: HttpResponse): Future[FileDetails] =
    response.json.validate[FileDetails] match {
      case JsSuccess(fileDetails, _) => Future.successful(fileDetails)
      case JsError(errors) =>
        val errorMsg = errors
          .map {
            case (path, errs) => s"$path: ${errs.map(_.message).mkString(",")}"
          }
          .mkString("; ")
        logger.error(s"FileDetailsConnector: Failed to parse FileDetails JSON for conversationId=${conversationId.value}. Errors: $errorMsg")
        Future.failed(UnexpectedJsResult)
    }

  private def unExpected2xxStatus(status: Int) = status >= 201 && status < 300

}
