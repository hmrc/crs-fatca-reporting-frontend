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
import models.submission.{GiinAndElectionDBStatus, GiinUpdateFailed}
import pages.{GiinAndElectionStatusPage, ReportElectionsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.GiinNotSentView

import javax.inject.Inject
import scala.concurrent.Future

class GiinNotSentController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: GiinNotSentView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      def handleNoElection                                       = Future.successful(Ok(view(None)))
      def handleWithStatus(statusValue: GiinAndElectionDBStatus) = Future.successful(Ok(view(Some(statusValue.electionStatus))))
      def handleNoData                                           = Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

      val userAnswers = request.userAnswers
      (userAnswers.get(ReportElectionsPage), userAnswers.get(GiinAndElectionStatusPage)) match {
        case (None, _)                       => handleNoElection
        case (Some(false), _)                => handleNoElection
        case (Some(true), Some(statusValue)) => handleWithStatus(statusValue)
        case (_, None)                       => handleNoData
      }
  }
}
