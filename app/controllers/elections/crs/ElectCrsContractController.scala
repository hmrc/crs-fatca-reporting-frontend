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

package controllers.elections.crs

import controllers.actions.*
import forms.ElectCrsContractFormProvider
import models.Mode
import navigation.Navigator
import pages.ValidXMLPage
import pages.elections.crs.ElectCrsContractPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.elections.crs.ElectCrsContractView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ElectCrsContractController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ElectCrsContractFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ElectCrsContractView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(ValidXMLPage) match {
        case Some(validXmlData) =>
          val messageSpecData = validXmlData.messageSpecData
          val preparedForm = request.userAnswers.get(ElectCrsContractPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(messageSpecData.fiNameFromFim, preparedForm, mode))
        case _ => Redirect(controllers.routes.PageUnavailableController.onPageLoad().url)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(ValidXMLPage) match {
        case Some(validXmlData) =>
          val messageSpecData = validXmlData.messageSpecData
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(messageSpecData.fiNameFromFim, formWithErrors, mode))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ElectCrsContractPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(ElectCrsContractPage, mode, updatedAnswers))
            )
        case _ => Future.successful(Redirect(controllers.routes.PageUnavailableController.onPageLoad().url))
      }
  }
}
