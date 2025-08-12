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
import forms.elections.fatca.ElectFatcaThresholdsFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.ElectFatcaThresholdsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.elections.fatca.ElectFatcaThresholdsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ElectFatcaThresholdsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ElectFatcaThresholdsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ElectFatcaThresholdsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form   = formProvider()
  val fiName = "EFG Bank plc"

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(ElectFatcaThresholdsPage)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(fiName, preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(fiName, formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(ElectFatcaThresholdsPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ElectFatcaThresholdsPage, mode, updatedAnswers))
        )
  }
}
