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
import pages.{GiinAndElectionStatusPage, RequiredGiinPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ElectionsNotSentView

import javax.inject.Inject
import scala.concurrent.Future

class ElectionsNotSentController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ElectionsNotSentView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      def handleGIINSent    = Future.successful(Ok(view(true)))
      def handleGIINNotSent = Future.successful(Ok(view(false)))
      def handleNoData      = Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

      val answers = request.userAnswers
      (answers.get(RequiredGiinPage), answers.get(GiinAndElectionStatusPage)) match {
        case (None, _)          => handleGIINNotSent
        case (Some(_), Some(_)) => handleGIINSent
        case (Some(_), None)    => handleNoData
      }
  }
}
