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
import models.fileDetails.FileDetailsModel
import models.submission.ConversationId
import pages.GiinAndElectionStatusPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FileDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats
import utils.DateTimeFormats.dateFormatterForFileConfirmation
import viewmodels.FileConfirmationViewModel
import views.html.FileConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  fileDetailsService: FileDetailsService,
  val controllerComponents: MessagesControllerComponents,
  view: FileConfirmationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(conversationId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      fileDetailsService.getFileDetails(ConversationId(conversationId)) flatMap {
        case Some(fileDetails) =>
          val fileDetailsModel = FileDetailsModel(fileDetails)
          val date             = fileDetailsModel.submitted.format(dateFormatterForFileConfirmation())
          val time             = DateTimeFormats.formatTimeForFileConfirmation(fileDetailsModel.submitted)

          val fileSummary = FileConfirmationViewModel.getSummaryRows(fileDetailsModel)
          val paraContent =
            if (fileDetails.isFiUser) {
              FileConfirmationViewModel.getEmailParagraphForFI(fileDetails.subscriptionPrimaryContactEmail, fileDetails.subscriptionSecondaryContactEmail)
            } else {
              FileConfirmationViewModel.getEmailParagraphForNonFI(
                fileDetails.subscriptionPrimaryContactEmail,
                fileDetails.subscriptionSecondaryContactEmail,
                fileDetails.fiPrimaryContactEmail,
                fileDetails.fiSecondaryContactEmail
              )
            }
          val hasElectionFailed = request.userAnswers.get(GiinAndElectionStatusPage).exists(!_.electionStatus)

          Future.successful(Ok(view(fileSummary, paraContent, date, time, hasElectionFailed, fileDetails.fiNameFromFim)))
        case _ =>
          Future.successful(Redirect(controllers.routes.PageUnavailableController.onPageLoad().url))
      }
  }
}
