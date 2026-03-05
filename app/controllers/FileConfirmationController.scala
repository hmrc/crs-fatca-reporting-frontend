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
            FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com", Some("user2@email.com"), "fi1@email.com", Some("f12@email.com"))
          //      below commented code is for testers to test different test data and will be removed once we integrate with technical story
          //      val paraContent = FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com",Some("user2@email.com"),"fi1@email.com",None)
          //      val paraContent = FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com",None,"fi1@email.com",Some("f12@email.com"))
          //      val paraContent = FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com",None,"fi1@email.com",None)
          //      val paraContent = FileConfirmationViewModel.getEmailParagraphForFI("user1@email.com",Some("user2@email.com"))
          //      val paraContent = FileConfirmationViewModel.getEmailParagraphForFI("user1@email.com", None)
          Future.successful(Ok(view(fileSummary, paraContent, date, time, true)))
        case _ =>
          Future.successful(Redirect(controllers.routes.PageUnavailableController.onPageLoad().url))
      }
  }
}
