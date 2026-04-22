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

package controllers

import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FileDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.SubmissionChecksTableViewModel
import views.html.SubmissionsChecksView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubmissionsChecksController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  fileDetailsService: FileDetailsService,
  val controllerComponents: MessagesControllerComponents,
  view: SubmissionsChecksView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(page: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      fileDetailsService.getAllFileDetails(request.fatcaId, page) map {
        fileDetailsResult =>
          fileDetailsResult.fileDetailsList match {
            case Nil =>
              Redirect(controllers.routes.PageUnavailableController.onPageLoad().url)
            case _ =>
              Ok(view(SubmissionChecksTableViewModel(fileDetailsResult)))
          }
      }
  }
}
