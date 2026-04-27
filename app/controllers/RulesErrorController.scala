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

package controllers

import controllers.actions.*
import models.fileDetails.FileValidationErrors
import models.submission.ConversationId
import models.submission.fileDetails.Rejected
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FileDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.FileRejectedViewModel
import views.html.RulesErrorView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RulesErrorController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  fileDetailsService: FileDetailsService,
  val controllerComponents: MessagesControllerComponents,
  view: RulesErrorView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(conversationId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      fileDetailsService.getFileDetails(ConversationId(conversationId)).map {
        case Some(fileDetails) =>
          (fileDetails.status, fileDetails.errors) match {
            case (Rejected, Some(fileValidationErrors)) =>
              val errorLength = fileValidationErrors.fileError.fold(0)(_.length) + fileValidationErrors.recordError.fold(0)(_.length)
              Ok(view(fileDetails.name, fileDetails.messageType.toString, errorLength, FileRejectedViewModel(fileValidationErrors)))

            case _ =>
              logger.error(s"Unexpected state for conversationId: $conversationId - status: ${fileDetails.status}, errors: ${fileDetails.errors}")
              Redirect(controllers.routes.PageUnavailableController.onPageLoad())
          }

        case None =>
          logger.error(s"No file details found for conversationId: $conversationId")
          Redirect(controllers.routes.PageUnavailableController.onPageLoad())
      }
  }
}
