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
import models.ReportElectionState.*
import models.{CRS, FATCA, ReportElectionState, SendYourFileAdditionalText}
import models.submission.*
import models.upscan.URL
import pages.{ConversationIdPage, GiinAndElectionStatusPage, ReportElectionsPage, ValidXMLPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SendYourFileView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SendYourFileController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: SendYourFileView,
  submissionService: SubmissionService,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(ValidXMLPage) match {
        case Some(validatedFileData) =>
          val reportElections = request.userAnswers
            .get(ReportElectionsPage)
            .map(if (_) ElectionsReported else ElectionsNotReported)
            .getOrElse(ElectionsNotReported)

          val messageSpecData = validatedFileData.messageSpecData
          (messageSpecData.messageType, messageSpecData.giin, reportElections) match {
            case (CRS, _, ElectionsNotReported) | (FATCA, Some(_), ElectionsNotReported) =>
              Ok(view(SendYourFileAdditionalText.NONE))
            case (CRS, _, ElectionsReported) | (FATCA, Some(_), ElectionsReported) =>
              Ok(view(SendYourFileAdditionalText.ELECTIONS))
            case (FATCA, None, ElectionsReported) =>
              Ok(view(SendYourFileAdditionalText.BOTH))
            case (FATCA, None, ElectionsNotReported) =>
              Ok(view(SendYourFileAdditionalText.GIIN))
          }
        case _ =>
          Redirect(controllers.routes.PageUnavailableController.onPageLoad().url)
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(ValidXMLPage) match {
        case Some(validatedFileData) =>
          submissionService.submitElectionsAndGiin(request.userAnswers).flatMap {
            giinAndElectionStatus =>
              giinAndElectionStatus match {
                case giinUpdateFailed: GiinUpdateFailed =>
                  val giinAndElectionStatus = GiinAndElectionDBStatus(giinUpdateFailed.giinStatus, giinUpdateFailed.electionStatus)
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(GiinAndElectionStatusPage, giinAndElectionStatus))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(routes.GiinNotSentController.onPageLoad())
                case electionFailed: ElectionsSubmitFailed =>
                  val giinAndElectionStatus = GiinAndElectionDBStatus(electionFailed.giinStatus, electionFailed.electionStatus)
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(GiinAndElectionStatusPage, giinAndElectionStatus))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(routes.ElectionsNotSentController.onPageLoad())
                case GiinAndElectionSubmittedSuccessful =>
                  // added a dummy conversationId to ensure ticket DAC6-3967 happy path and javascript journey  can be tested
                  // full implementation of the code below will be in https://jira.tools.tax.service.gov.uk/browse/DAC6-3829
                  val conversationId = "dummy-conversation Id"
                  for {
                    userAnswers <- Future.fromTry(request.userAnswers.set(ConversationIdPage, ConversationId(conversationId)))
                    _           <- sessionRepository.set(userAnswers)
                  } yield Redirect(routes.StillCheckingYourFileController.onPageLoad())
              }
          }
        case _ =>
          Future.successful(Redirect(controllers.routes.PageUnavailableController.onPageLoad().url))
      }
  }

  def getStatus: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(ConversationIdPage) match {
        case Some(conversationId) =>
          Future.successful(Ok(Json.toJson(URL(routes.StillCheckingYourFileController.onPageLoad().url))))
        case None =>
          request.userAnswers.get(GiinAndElectionStatusPage) match {
            case Some(giinAndElectionStatus) =>
              if (!giinAndElectionStatus.giinStatus) {
                Future.successful(Ok(Json.toJson(URL(routes.GiinNotSentController.onPageLoad().url))))
              } else if (!giinAndElectionStatus.electionStatus) {
                Future.successful(Ok(Json.toJson(URL(routes.ElectionsNotSentController.onPageLoad().url))))
              } else {
                logger.warn("UserAnswers.ConversationId is empty")
                Future.successful(InternalServerError)
              }
            case None =>
              logger.warn("UserAnswers.ConversationId is empty")
              Future.successful(InternalServerError)
          }
      }
  }
}
