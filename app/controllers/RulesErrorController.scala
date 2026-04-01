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
import models.fileDetails.BusinessRuleErrorCode.{CorrDocRefIdUnknown, InvalidMessageRefIDFormat}
import models.fileDetails.{FileErrors, FileValidationErrors, RecordError}
import models.submission.ConversationId
import models.submission.fileDetails.Rejected
import pages.ValidXMLPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FileDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.FileRejectedViewModel
import views.html.RulesErrorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RulesErrorController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  fileDetailsService: FileDetailsService,
  val controllerComponents: MessagesControllerComponents,
  view: RulesErrorView
)(implicit ec: ExecutionContext) extends FrontendBaseController
    with I18nSupport with Logging {

  def onPageLoad(conversationId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      fileDetailsService.getFileDetails(ConversationId(conversationId)).map {
        case Some(fileDetails) =>
          fileDetails.status match {
            case r: Rejected =>
              val fileValidationErrors: FileValidationErrors = r.error
              println("File validation errors: " + fileValidationErrors)
              val fileRejectedViewModel = FileRejectedViewModel(fileValidationErrors)
              request.userAnswers.get(ValidXMLPage) match {
                case Some(validXmlData) =>
                  val fileName = validXmlData.fileName
                  val regimeType = validXmlData.messageSpecData.messageType
                  val errorLength = 101
                  Ok(view(fileName, regimeType.toString, errorLength, fileRejectedViewModel))
                case None =>
                  logger.warn("File details found for conversation ID: " + conversationId + " but no valid XML data found in user answers")
                  Redirect(controllers.routes.PageUnavailableController.onPageLoad())
              }
            case _ =>
              logger.warn("File details found for conversation ID: " + conversationId + " but status is not Rejected")
              Redirect(controllers.routes.PageUnavailableController.onPageLoad())
          }

        case None =>
          logger.warn("No file details (for Business rule errors) found for conversation ID: " + conversationId)
          Redirect(controllers.routes.PageUnavailableController.onPageLoad())
      }
  }

  // Todo This will be replaced with real data from the connector when the backend is done
  // Keeping this here to allow the view to be completed
  private def createFileRejectedViewModel() = {
    val fileErrors: Seq[FileErrors] = Seq(FileErrors(CorrDocRefIdUnknown, None))
    val recordErrors: Seq[RecordError] = Seq(
      RecordError(
        InvalidMessageRefIDFormat,
        Some("GB2026GB-FIID123456789-CRSReport2026001-ReportingFI-001"),
        Some(Seq("GB2026GB-FIID123456789-CRSReport2026001-ReportingFI-001"))
      )
    )
    val validationErrors = FileValidationErrors(
      fileError = Some(fileErrors),
      recordError = Some(recordErrors)
    )

    FileRejectedViewModel(validationErrors)
  }
}
