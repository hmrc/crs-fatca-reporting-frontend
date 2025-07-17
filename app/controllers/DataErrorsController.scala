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

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ErrorViewHelper
import views.html.DataErrorsView

class DataErrorsController @Inject() (
  override val messagesApi: MessagesApi,
  errorViewHelper: ErrorViewHelper,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: DataErrorsView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify {
    implicit request =>
      //Pass file errors
      val errors: Seq[GenericError] = Seq(GenericError(12345, Message("error1")), GenericError(2, Message("error2")))
      val errorLength: Int = errors.length
      //Pass file name
      val fileName: String = "PlaceHolder file name"
      //compare regimeTypes and pass "CRS" or "FATCA"
      val regimeType: String = "CRS"
      //compare regimeTypes and pass messageKey listCRSLink | listFATCALink
      val regimeTypeMessage: String = if (regimeType == "CRS") "dataErrors.listCRSLink" else "dataErrors.listFATCALink"

      Ok(view(errorViewHelper.generateTable(errors), fileName, regimeTypeMessage, regimeType, errorLength))
  }
}
