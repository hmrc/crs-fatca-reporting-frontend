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
import models.submission.{ConversationId, ElectionsSubmissionDetails, GiinUpdateRequest, SubmissionDetails}
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class SubmissionConnector @Inject() (http: HttpClientV2, config: FrontendAppConfig) extends Logging {

  def submitDocument(submissionDetails: SubmissionDetails)(using
                                                           hc: HeaderCarrier,
                                                           ec: ExecutionContext
  ): Future[Option[ConversationId]] = {
    val url = url"${config.crsFatcaBackendUrl}/crs-fatca-reporting/submit"

    http
      .post(url)
      .withBody(Json.toJson(submissionDetails))
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case ACCEPTED =>
              val conversationId = (Json.parse(response.body) \ "uploadId").as[String]
              Some(ConversationId(conversationId))
            case INTERNAL_SERVER_ERROR =>
              logger.error(s"Submission failed due to internal server error: ${response.body}")
              None
            case _ =>
              logger.error(s"Submission received unexpected status ${response.status}: ${response.body}")
              None
          }
      }
  }


  def updateGiin(request: GiinUpdateRequest)(using hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val url = url"${config.crsFatcaBackendUrl}/crs-fatca-reporting/update/giin"

    http
      .post(url)
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case NO_CONTENT =>
              logger.info("Update GIIN successful.")
              true
            case BAD_REQUEST =>
              logger.warn(s"Update GIIN failed due to bad request: ${response.body}")
              false
            case INTERNAL_SERVER_ERROR =>
              logger.error(s"Update GIIN failed due to internal server error: ${response.body}")
              false
            case _ =>
              logger.error(s"Update GIIN received unexpected status ${response.status}: ${response.body}")
              false
          }
      }
      .recover {
        case NonFatal(e) =>
          logger.error(s"Non fatal exception while updating GIIN: ${e.getMessage}", e)
          false
      }
  }

  def submitElections(request: ElectionsSubmissionDetails)(using hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    val url = url"${config.crsFatcaBackendUrl}/crs-fatca-reporting/elections/submit"

    http
      .post(url)
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case NO_CONTENT =>
              logger.info("Elections submission successful.")
              true
            case _ =>
              false
          }
      }
      .recover {
        case upstreamErrorRes: UpstreamErrorResponse =>
          false

        case NonFatal(e) =>
          logger.error(s"Non fatal exception while submitting elections: ${e.getMessage}", e)
          false
      }

}
