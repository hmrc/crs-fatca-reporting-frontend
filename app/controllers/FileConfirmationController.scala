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
import models.fileDetails.FileDetails

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats
import utils.DateTimeFormats.dateFormatterForFileConfirmation
import viewmodels.FileConfirmationViewModel
import views.html.FileConfirmationView

import java.time.LocalDateTime

class FileConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: FileConfirmationView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val submittedTime = LocalDateTime.parse("2025-09-12T12:01:00")
      val date          = submittedTime.format(dateFormatterForFileConfirmation())
      val time          = DateTimeFormats.formatTimeForFileConfirmation(submittedTime)
      val fileDetails   = FileDetails("name.xml", "c-8-new-f-va", "CRS", "EFG Bank plc", "New information", submittedTime, LocalDateTime.now())
      val fileSummary   = FileConfirmationViewModel.getSummaryRows(fileDetails)
//      val paraContent = FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com",Some("user2@email.com"),"fi1@email.com",Some("f12@email.com"))
//      val paraContent = FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com",Some("user2@email.com"),"fi1@email.com",None)
//      val paraContent = FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com",None,"fi1@email.com",Some("f12@email.com"))
//      val paraContent = FileConfirmationViewModel.getEmailParagraphForNonFI("user1@email.com",None,"fi1@email.com",None)
//      val paraContent = FileConfirmationViewModel.getEmailParagraphForFI("user1@email.com",Some("user2@email.com"))
      val paraContent = FileConfirmationViewModel.getEmailParagraphForFI("user1@email.com", None)
      Ok(view(fileSummary, paraContent, date, time, true))
  }
}
