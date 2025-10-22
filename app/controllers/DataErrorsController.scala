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
import models.{GenericError, Message}
import pages.{GenericErrorPage, InvalidXMLPage}
import play.api.i18n.Lang.logger

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ErrorViewHelper
import views.html.{DataErrorsView, ThereIsAProblemView}

class DataErrorsController @Inject() (
  override val messagesApi: MessagesApi,
  errorViewHelper: ErrorViewHelper,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: DataErrorsView,
  errorView: ThereIsAProblemView
) extends FrontendBaseController
    with I18nSupport {

      def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
        implicit request =>
          (request.userAnswers.get(GenericErrorPage), request.userAnswers.get(InvalidXMLPage)) match {
            case (Some(errors), Some(fileName)) =>
              val xmlErrors = for {
                error <- errors.sorted
              } yield error

              val errorLength: Int = xmlErrors.length
              val fileName: String = "PlaceHolder file name"
              val regimeType: String = "CRS"
              val regimeTypeMessage: String = if (regimeType == "CRS") "dataErrors.listCRSLink" else "dataErrors.listFATCALink"

              Ok(view(errorViewHelper.generateTable(xmlErrors), fileName, regimeTypeMessage, regimeType, errorLength))
            case _ =>
              logger.warn("FileDataErrorController: Unable to retrieve either Invalid XML information or GenericErrors from UserAnswers")
              InternalServerError(errorView())
          }
      }
}
