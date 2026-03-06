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

package services

import connectors.FileDetailsConnector
import models.fileDetails.FileDetails
import models.submission.ConversationId
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileDetailsService @Inject() (val connector: FileDetailsConnector) extends Logging {

  def getFileDetails(conversationId: ConversationId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[FileDetails]] =
    connector
      .getFileDetails(conversationId)
      .map(Some(_))
      .recover {
        case _ =>
          logger.warn(s"FileDetailsService: Failed to get FileDetails for conversationId: ${conversationId.value}")
          None
      }

}
