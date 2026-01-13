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
  Errors,
  FIIDNotMatchingError,
  GenericError,
  IncorrectMessageTypeError,
  InvalidXmlFileError,
  MessageSpecData,
  NormalMode,
  ReportingPeriodError,
  SchemaValidationErrors,
  UserAnswers,
  ValidatedFileData
}
import navigation.Navigator
import pages.*
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ThereIsAProblemView

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
  navigator: Navigator,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val maxFileNameLength         = 100
  private val disallowedCharactersRegex = "[<>:\"'&/\\\\|?*]".r

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      extractIds(request.userAnswers) match {
        case Some((uploadId, fileReference)) =>
          upscanConnector.getUploadDetails(uploadId) flatMap {
            uploadSessions =>
              getDownloadUrl(uploadSessions).fold {
                logger.error(s"Failed to upload file with upload Id: [${uploadId.value}]")
                Future.successful(InternalServerError(errorView()))
              } {
                downloadDetails =>
                  val trimmedFileName = downloadDetails.name.stripSuffix(".xml")

                  (isFileNameLengthInvalid(trimmedFileName), isDisallowedCharactersPresent(trimmedFileName)) match {
                    case (true, _) => navigateToErrorPage(uploadId, "invalidfilenamelength")
                    case (_, true) => navigateToErrorPage(uploadId, "disallowedcharacters")
                    case _         => handleFileValidation(downloadDetails, uploadId, fileReference, downloadDetails.downloadUrl)
                  }
              }
          }

        case None =>
          logger.error("Missing Upload ID or File Reference from user answers")
          Future.successful(InternalServerError(errorView()))
      }
  }

  private def handleFileValidation(
    downloadDetails: ExtractedFileStatus,
    uploadId: UploadId,
    fileReference: Reference,
    downloadUrl: String
  )(implicit request: DataRequest[_]): Future[Result] =
    validationConnector
      .sendForValidation(
        FileValidateRequest(downloadUrl, uploadId.value, request.fatcaId, fileReference.value)
      )
      .flatMap {
        case Right(messageSpecData) =>
          handleSuccessfulValidation(downloadDetails, downloadUrl, messageSpecData)
        case Left(error) =>
          handleValidationErrors(downloadDetails, error)
      }

  private def handleSuccessfulValidation(
    downloadDetails: ExtractedFileStatus,
    downloadUrl: String,
    messageSpecData: MessageSpecData
  )(implicit request: DataRequest[_]): Future[Result] = {

    val validatedFileData = ValidatedFileData(downloadDetails.name, messageSpecData, downloadDetails.size, downloadDetails.checksum)

    val resultFuture = Future
      .fromTry {
        request.userAnswers
          .set(ValidXMLPage, validatedFileData)
          .flatMap(_.set(URLPage, downloadUrl))
          .flatMap(_.set(RequiresElectionsPage, messageSpecData.electionsRequired))
      }
      .flatMap {
        updatedAnswersWithFileData =>
          sessionRepository.set(updatedAnswersWithFileData).map {
            _ =>
              Redirect(navigator.nextPage(ValidXMLPage, NormalMode, updatedAnswersWithFileData))
          }
      }

    resultFuture.recoverWith {
      case e: Throwable =>
        logger.error(s"Error during successful validation flow (session update): ${e.getMessage}", e)
        Future.successful(InternalServerError(errorView()))
    }
  }

  private def handleValidationErrors(
    downloadDetails: ExtractedFileStatus,
    error: Errors
  )(implicit request: DataRequest[_]): Future[Result] = error match {

    case SchemaValidationErrors(validationErrors, messageType) =>
      handleSchemaValidationErrors(downloadDetails.name, validationErrors.errors, messageType)

    case ReportingPeriodError =>
      Future.successful(Redirect(routes.ReportingPeriodErrorController.onPageLoad()))

    case FIIDNotMatchingError =>
      Future.successful(Redirect(routes.FINotMatchingController.onPageLoad()))

    case IncorrectMessageTypeError =>
      Future.successful(Redirect(routes.InvalidMessageTypeErrorController.onPageLoad()))

    case InvalidXmlFileError =>
      handleInvalidXmlFileError(downloadDetails.name)

    case other =>
      logger.error(s"Other validation error occurred during file validation: $other")
      Future.successful(InternalServerError(errorView()))
  }

  private def handleSchemaValidationErrors(
    fileName: String,
    errors: Seq[GenericError],
    messageType: String
  )(implicit request: DataRequest[_]): Future[Result] =
    for {
      updatedAnswers            <- Future.fromTry(request.userAnswers.set(InvalidXMLPage, fileName))
      updatedAnswersWithMsgType <- Future.fromTry(updatedAnswers.set(MessageTypePage, messageType))
      updatedAnswersWithErrors  <- Future.fromTry(updatedAnswersWithMsgType.set(GenericErrorPage, errors))
      _                         <- sessionRepository.set(updatedAnswersWithErrors)
    } yield Redirect(routes.DataErrorsController.onPageLoad())

  private def handleInvalidXmlFileError(
    fileName: String
  )(implicit request: DataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(InvalidXMLPage, fileName))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(routes.FileErrorController.onPageLoad())

  private def extractIds(answers: UserAnswers): Option[(UploadId, Reference)] =
    for {
      uploadId      <- answers.get(UploadIDPage)
      fileReference <- answers.get(FileReferencePage)
    } yield (uploadId, fileReference)

  private def getDownloadUrl(uploadSessions: Option[UploadSessionDetails]): Option[ExtractedFileStatus] =
    uploadSessions.flatMap {
      uploadDetails =>
        uploadDetails.status match {
          case UploadedSuccessfully(name, downloadUrl, size, checksum) =>
            Some(ExtractedFileStatus(name, downloadUrl, size, checksum))
          case _ => None
        }
    }

  private def isFileNameLengthInvalid(fileName: String): Boolean =
    fileName.length > maxFileNameLength

  private def isDisallowedCharactersPresent(fileName: String): Boolean =
    disallowedCharactersRegex.findFirstIn(fileName).isDefined

  private def navigateToErrorPage(uploadId: UploadId, errorMessage: String): Future[Result] =
    Future.successful(
      Redirect(
        routes.IndexController
          .showError("invalidargument", errorMessage, uploadId.value)
          .url
      )
    )
}
