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

package controllers.elections

import controllers.actions.*
import forms.elections.ReportElectionsFormProvider
import models.UserAnswers.extractMessageSpecData
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.ReportElectionsPage
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ThereIsAProblemView
import views.html.elections.ReportElectionsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReportElectionsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ReportElectionsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ReportElectionsView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def form(regime: String) = formProvider(regime)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      extractMessageSpecData(request.userAnswers) {
        messageSpecData =>

          val reportingPeriod = messageSpecData.reportingPeriod.getYear.toString
          val regime          = messageSpecData.messageType.toString
          val name            = messageSpecData.fiNameFromFim

          val preparedForm = request.userAnswers.get(ReportElectionsPage) match {
            case None        => form(regime)
            case Some(value) => form(regime).fill(value)
          }
          Ok(view(reportingPeriod, regime, name, preparedForm, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      extractMessageSpecData(request.userAnswers) {
        messageSpecData =>
          val reportingPeriod = messageSpecData.reportingPeriod.getYear.toString
          val regime          = messageSpecData.messageType.toString
          val name            = messageSpecData.fiNameFromFim

          form(regime)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(reportingPeriod, regime, name, formWithErrors, mode))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ReportElectionsPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield
                  if (!value) {
                    Redirect(controllers.routes.CheckYourFileDetailsController.onPageLoad())
                  } else {
                    regime match {
                      case "FATCA" =>
                        Redirect(controllers.elections.fatca.routes.TreasuryRegulationsController.onPageLoad(mode)) // todo handle in navigator
                      case "CRS" =>
                        Redirect(controllers.elections.crs.routes.ElectCrsContractController.onPageLoad(mode))
                      case unknownRegime =>
                        logger.error(s"Unknown regime: $unknownRegime encountered during ReportElections submission.")
                        InternalServerError(errorView())
                    }
                  }
            )
      }
  }
}
