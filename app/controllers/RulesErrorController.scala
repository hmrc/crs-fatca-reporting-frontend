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
import models.fileDetails.BusinessRuleErrorCode.{CorrDocRefIdUnknown, DocRefIDFormat, InvalidMessageRefIDFormat}
import models.fileDetails.{FileErrors, FileValidationErrors, RecordError}

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.FileRejectedViewModel
import views.html.RulesErrorView

class RulesErrorController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: RulesErrorView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val fileName     = "filename.xml"
      val regimentType = "CRS"
      val errorLength  = 101
      Ok(view(fileName, regimentType, errorLength, createFileRejectedViewModel()))
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
