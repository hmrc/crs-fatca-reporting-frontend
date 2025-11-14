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

package controllers.elections.fatca

import controllers.actions.*
import forms.elections.fatca.TreasuryRegulationsFormProvider
import models.UserAnswers.getMessageSpecData
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.elections.fatca.TreasuryRegulationsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.elections.fatca.TreasuryRegulationsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TreasuryRegulationsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TreasuryRegulationsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TreasuryRegulationsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form    = formProvider()
  val fiName_ = "Placeholder Financial Institution"

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getMessageSpecData(request.userAnswers) {
        messageSpecData =>
          val fiName = messageSpecData.fiNameFromFim
          val preparedForm = request.userAnswers.get(TreasuryRegulationsPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode, fiName))
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getMessageSpecData(request.userAnswers) {
        messageSpecData =>
          val fiName = messageSpecData.fiNameFromFim
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiName))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(TreasuryRegulationsPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(TreasuryRegulationsPage, mode, updatedAnswers))
            )
      }
  }
}
