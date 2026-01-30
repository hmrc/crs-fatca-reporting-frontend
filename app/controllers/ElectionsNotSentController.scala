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
import models.submission.{ConversationId, SubmissionDetails}
import models.{UserAnswers, ValidatedFileData}
import pages.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ElectionsNotSentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ElectionsNotSentController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ElectionsNotSentView,
  submissionService: SubmissionService,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      def handleGIINSent    = Future.successful(Ok(view(true)))
      def handleGIINNotSent = Future.successful(Ok(view(false)))
      def handleNoData      = Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

      val answers = request.userAnswers

      (answers.get(RequiredGiinPage), answers.get(GiinAndElectionStatusPage)) match {
        case (None, _)          => handleGIINNotSent
        case (Some(_), Some(_)) => handleGIINSent
        case (Some(_), None)    => handleNoData
      }
  }

  def finishSendingFile: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val answers: UserAnswers = request.userAnswers

      (answers.get(ValidXMLPage), answers.get(URLPage), answers.get(UploadIDPage), answers.get(FileReferencePage)) match {
        case (Some(ValidatedFileData(fileName, messageSpecData, fileSize, checksum)), Some(fileUrl), Some(uploadId), Some(fileReference)) =>
          val submissionDetails = SubmissionDetails(fileName, uploadId, request.fatcaId, fileSize, fileUrl, checksum, messageSpecData, fileReference)

          submissionService.submitDocument(submissionDetails) flatMap {
            case Some(conversationId: ConversationId) =>
              for {
                userAnswers <- Future.fromTry(request.userAnswers.set(ConversationIdPage, conversationId))
                _           <- sessionRepository.set(userAnswers)
              } yield Redirect(controllers.routes.StillCheckingYourFileController.onPageLoad())
            case _ =>
              Future.successful(InternalServerError)
          }
        case _ =>
          Future.successful(InternalServerError)
      }
  }
}
