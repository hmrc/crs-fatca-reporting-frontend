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
import models.submission.ConversationId
import models.submission.fileDetails.FileStatus
import play.api.Logging
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
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

}
