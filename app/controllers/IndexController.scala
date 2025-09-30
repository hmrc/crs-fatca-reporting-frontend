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

import config.FrontendAppConfig
import connectors.UpscanConnector
import controllers.Execution.trampoline
import controllers.actions.{DataCreationAction, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.UploadXMLFormProvider
import models.requests.DataRequest
import models.upscan.*
import org.apache.pekko
import org.apache.pekko.actor.ActorSystem
import pages.{FileReferencePage, UploadIDPage, ValidXMLPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.i18n.Lang.logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UploadXMLView

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  setData: DataCreationAction,
  requireData: DataRequiredAction,
  actorSystem: ActorSystem,
  config: FrontendAppConfig,
  upscanConnector: UpscanConnector,
  formProvider: UploadXMLFormProvider,
  view: UploadXMLView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen setData).async {
    implicit request =>
      val form = formProvider()
      toResponse(form)
  }

  def showError(errorCode: String, errorMessage: String, errorRequestId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider()
      errorCode match {
        case "EntityTooLarge" =>
          val formWithErrors: Form[String] = form.withError("file-upload", "uploadFile.error.file.size.large")
          toResponse(formWithErrors)
        case "VirusFile" =>
          val formWithErrors: Form[String] = form.withError("file-upload", "uploadFile.error.file.content.virus")
          toResponse(formWithErrors)
        case "InvalidArgument" | "OctetStream" =>
          if (errorMessage.equalsIgnoreCase("InvalidFileNameLength")) {
            val formWithErrors: Form[String] = form.withError("file-upload", "uploadFile.error.file.name.length")
            toResponse(formWithErrors)
          } else if(errorMessage.equalsIgnoreCase("typeMismatch")){
            val formWithErrors: Form[String] = form.withError("file-upload", "uploadFile.error.file.type.invalid")
            toResponse(formWithErrors)
          }else if(errorMessage.equalsIgnoreCase("FileIsEmpty")){
            val formWithErrors: Form[String] = form.withError("file-upload", "uploadFile.error.file.content.empty")
            toResponse(formWithErrors)
          }else {
            val formWithErrors: Form[String] = form.withError("file-upload", "uploadFile.error.file.select")
            toResponse(formWithErrors)
          }
        case _ =>
          logger.warn(s"Upscan error $errorCode: $errorMessage, requestId is $errorRequestId")
          val formWithErrors: Form[String] = form.withError("file-upload", "uploadFile.error.file.content.unknown")
          toResponse(formWithErrors)
      }
  }

  private def toResponse(preparedForm: Form[String])(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Result] = {
    val uploadId: UploadId = UploadId.generate
    (for {
      upscanInitiateResponse <- upscanConnector.getUpscanFormData(uploadId)
      uploadId <- upscanConnector.requestUpload(uploadId, upscanInitiateResponse.fileReference)
      updatedAnswers <- Future.fromTry(
        request.userAnswers
          .set(UploadIDPage, uploadId)
          .flatMap(_.set(FileReferencePage, upscanInitiateResponse.fileReference))
          .flatMap(_.remove(ValidXMLPage))
      )
      _ <- sessionRepository.set(updatedAnswers)
    } yield Ok(view(preparedForm,upscanInitiateResponse)))
      .recover {
        case e: Exception =>
          logger.error(s"UploadFileController: An exception occurred when contacting Upscan: $e")
          Redirect(routes.IndexController.onPageLoad())
      }
  }

  def getStatus(uploadId: UploadId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      // Delay the call to make sure the backend db has been populated by the upscan callback first
      pekko.pattern.after(config.upscanCallbackDelayInSeconds.seconds, actorSystem.scheduler) {
        upscanConnector.getUploadStatus(uploadId) map {
          case Some(_: UploadedSuccessfully) =>
            Redirect(routes.IndexController.onPageLoad().url)
          case Some(r: UploadRejected) =>
            if (r.details.message.contains("octet-stream")) {
              logger.warn(s"Show errorForm on rejection $r")
              val errorReason = r.details.failureReason
              Redirect(routes.IndexController.showError("OctetStream", errorReason, "").url)
            } else {
              logger.warn(s"Upload rejected. Error details: ${r.details}")
              Redirect(routes.IndexController.showError("InvalidArgument", "typeMismatch", "").url)
            }
          case Some(Quarantined) =>
            Redirect(routes.IndexController.showError("VirusFile", "", "").url)
          case Some(Failed) =>
            logger.warn("File upload returned failed status")
            Redirect(routes.IndexController.showError("UploadFailed", "", "").url)
          case Some(_) =>
            Redirect(routes.IndexController.getStatus(uploadId).url)
          case None =>
            logger.warn("Unable to retrieve file upload status from Upscan")
            Redirect(routes.IndexController.showError("UploadFailed", "", "").url)
        }
      }
  }
}
