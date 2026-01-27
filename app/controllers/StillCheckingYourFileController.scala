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

import config.FrontendAppConfig
import connectors.FileDetailsConnector
import controllers.actions.*
import models.submission.fileDetails.{Accepted as FileStatusAccepted, Pending}
import pages.{ConversationIdPage, ValidXMLPage}
import play.api.i18n.Lang.logger

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{StillCheckingYourFileView, ThereIsAProblemView}
import viewmodels.FileCheckViewModel.createFileSummary

import scala.concurrent.{ExecutionContext, Future}

class StillCheckingYourFileController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: StillCheckingYourFileView,
  errorView: ThereIsAProblemView,
  frontendAppConfig: FrontendAppConfig,
  fileDetailsConnector: FileDetailsConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (request.userAnswers.get(ValidXMLPage), request.userAnswers.get(ConversationIdPage)) match {
        case (Some(xmlDetails), Some(conversationId)) =>
          fileDetailsConnector.getStatus(conversationId) flatMap {
            case Some(FileStatusAccepted) =>
              Future.successful(Redirect(routes.FilePassedChecksController.onPageLoad()))
            case Some(Pending) =>
              val placeHolderMessageRefID = "MyFATCAReportMessageRefId1234567890"
              val placeHolderFileStatus   = "Pending"
              val placeHolderFIName       = "EFG Bank plc"
              val placeHolderIsFIUser     = true
              Future.successful(
                Ok(
                  view(createFileSummary(placeHolderMessageRefID, placeHolderFileStatus), frontendAppConfig.signOutUrl, placeHolderIsFIUser, placeHolderFIName)
                )
              )
            case None =>
              logger.warn("Unable to get Status")
              Future.successful(InternalServerError(errorView()))
            case _ =>
              // The other statuses are handled by subsequent Jira tickets
              logger.warn("Unexpected file status received")
              Future.successful(InternalServerError(errorView()))
          }
        case _ =>
          logger.warn("Unable to retrieve fileName & conversationId")
          Future.successful(InternalServerError(errorView()))
      }

  }
}
