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

import connectors.{UpscanConnector, ValidationConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.requests.DataRequest
import models.upscan.*
import models.{
  FIIDNotMatchingError,
  IncorrectMessageTypeError,
  InvalidXmlFileError,
  NormalMode,
  ReportingPeriodError,
  SchemaValidationErrors,
  UserAnswers,
  ValidatedFileData
}
import pages.*
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ThereIsAProblemView

import java.time.{LocalDate, ZoneId}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileValidationController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  val sessionRepository: SessionRepository,
  upscanConnector: UpscanConnector,
  requireData: DataRequiredAction,
  validationConnector: ValidationConnector,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      extractIds(request.userAnswers) match {
        case Some((uploadId, fileReference)) =>
          val subscriptionId = request.fatcaId

          upscanConnector.getUploadDetails(uploadId) flatMap {
            uploadSessions =>
              getDownloadUrl(uploadSessions).fold {
                logger.error(s"Failed to upload file with upload Id: [${uploadId.value}]")
                Future.successful(InternalServerError(errorView()))
              } {
                downloadDetails =>
                  val downloadUrl = downloadDetails.downloadUrl
                  val fileName    = downloadDetails.name

                  handleFileValidation(downloadDetails, uploadId, fileReference, downloadUrl)
              }
          }

        case None =>
          logger.error("Missing Upload ID or File Reference from user answers")
          Future.successful(InternalServerError(errorView()))
      }

  }

  private def isReportingYearValid(reportingYear: Int): Boolean = {
    val currentYear = LocalDate.now(ZoneId.of("Europe/London")).getYear
    reportingYear >= (currentYear - 12) && reportingYear <= currentYear
  }

  private def hasElectionsHappened(): Boolean = false

  private def extractIds(answers: UserAnswers): Option[(UploadId, Reference)] =
    for {
      uploadId      <- answers.get(UploadIDPage)
      fileReference <- answers.get(FileReferencePage)
    } yield (uploadId, fileReference)

  private def getDownloadUrl(uploadSessions: Option[UploadSessionDetails]): Option[ExtractedFileStatus] =
    uploadSessions match {
      case Some(uploadDetails) =>
        uploadDetails.status match {
          case UploadedSuccessfully(name, downloadUrl, size, checksum) =>
            Option(ExtractedFileStatus(name, downloadUrl, size, checksum))
          case _ => None
        }
      case _ => None
    }

  private def handleFileValidation(
    downloadDetails: ExtractedFileStatus,
    uploadId: UploadId,
    fileReference: Reference,
    downloadUrl: String
  )(implicit request: DataRequest[_]) =
    validationConnector
      .sendForValidation(
        FileValidateRequest(downloadUrl, uploadId.value, request.fatcaId, fileReference.value)
      )
      .flatMap {
        case Right(messageSpecData) =>
          val validatedFileData = ValidatedFileData(downloadDetails.name, messageSpecData, downloadDetails.size, downloadDetails.checksum)
          val reportingYear     = messageSpecData.reportingPeriod.getYear
          for {
            updatedAnswers        <- Future.fromTry(request.userAnswers.set(ValidXMLPage, validatedFileData))
            updatedAnswersWithURL <- Future.fromTry(updatedAnswers.set(URLPage, downloadUrl))
            _                     <- sessionRepository.set(updatedAnswersWithURL)
          } yield
            if (isReportingYearValid(reportingYear) && !hasElectionsHappened()) {
              Redirect(controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode))
            } else {
              Redirect(routes.IndexController.onPageLoad())
            }
        case Left(SchemaValidationErrors(validationErrors, messageType)) =>
          for {
            updatedAnswers            <- Future.fromTry(request.userAnswers.set(InvalidXMLPage, downloadDetails.name))
            updatedAnswersWithMsgType <- Future.fromTry(updatedAnswers.set(MessageTypePage, messageType))
            updatedAnswersWithErrors  <- Future.fromTry(updatedAnswersWithMsgType.set(GenericErrorPage, validationErrors.errors))
            _                         <- sessionRepository.set(updatedAnswersWithErrors)
          } yield Redirect(routes.DataErrorsController.onPageLoad())
        case Left(ReportingPeriodError) =>
          Future.successful(Redirect(routes.ReportingPeriodErrorController.onPageLoad()))
        case Left(FIIDNotMatchingError) =>
          Future.successful(Redirect(routes.FINotMatchingController.onPageLoad()))
        case Left(IncorrectMessageTypeError) =>
          Future.successful(Redirect(routes.InvalidMessageTypeErrorController.onPageLoad()))
        case Left(InvalidXmlFileError) =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(InvalidXMLPage, downloadDetails.name))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(routes.FileErrorController.onPageLoad())
        case Left(_) =>
          logger.error("Other validation error occurred during file validation")
          Future.successful(InternalServerError(errorView()))
      }

}
