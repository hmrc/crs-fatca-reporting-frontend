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

import base.SpecBase
import connectors.{UpscanConnector, ValidationConnector}
import helpers.FakeUpscanConnector
import models.upscan.{Reference, UploadId, UploadSessionDetails, UploadedSuccessfully}
import models.{
  CRS,
  FATCA,
  FIIDNotMatchingError,
  IncorrectMessageTypeError,
  InvalidXmlFileError,
  NormalMode,
  ReportingPeriodError,
  UserAnswers,
  ValidatedFileData
}
import org.bson.types.ObjectId
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.*
import play.api
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{inject, Application}
import repositories.SessionRepository
import views.html.ThereIsAProblemView

import java.time.LocalDate
import scala.concurrent.Future

class FileValidationControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockValidationConnector: ValidationConnector = mock[ValidationConnector]
  private val fakeUpscanConnector: FakeUpscanConnector     = app.injector.instanceOf[FakeUpscanConnector]

  private val currentYear               = 2025
  private val reportingPeriodLowerBound = currentYear - 12
  private val downloadURL               = "http://dummy-url.com"
  private val FileSize                  = 100L
  private val fileName                  = "testFile"
  private val fileReferenceId           = Reference("fileReferenceId")
  private val uploadId                  = UploadId("123")
  private val checksum                  = "testCheckSum"

  private val uploadDetails = UploadSessionDetails(
    new ObjectId(),
    uploadId,
    fileReferenceId,
    UploadedSuccessfully(fileName, downloadURL, FileSize, checksum)
  )

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    super.beforeEach()
  }

  "FileValidationController" - {
    val userAnswers              = UserAnswers(userAnswersId).withPage(UploadIDPage, uploadId).withPage(FileReferencePage, fileReferenceId)
    val application: Application = getApplication(userAnswers)

    "must redirect to ReportElectionsController and save data for a valid file with a valid reporting period for FATCA" in {
      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val reportingPeriod                                = LocalDate.of(currentYear - 1, 1, 1)

      val messageSpecData = getMessageSpecData(FATCA, reportingPeriod = reportingPeriod, giin = Some("giin"))

      val validatedFileData = getValidatedFileData(messageSpecData)

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Right(messageSpecData)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      fakeUpscanConnector.setDetails(uploadDetails)

      val request                = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode).url
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.data mustEqual getExpectedData(validatedFileData)
    }

    "must redirect to ReportElectionsController and save data for a valid file with a valid reporting period for CRS" in {
      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val reportingPeriod                                = LocalDate.of(currentYear - 1, 1, 1)

      val messageSpecData = getMessageSpecData(CRS, reportingPeriod = reportingPeriod, giin = Some("giin"))

      val validatedFileData = getValidatedFileData(messageSpecData)

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Right(messageSpecData)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      fakeUpscanConnector.setDetails(uploadDetails)

      val request                = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode).url
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.data mustEqual getExpectedData(validatedFileData)
    }

    "must redirect to check-your-file-details when invalid reporting period (outside 12-year window) is provided" in {
      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val reportingPeriod                                = LocalDate.of(reportingPeriodLowerBound - 1, 1, 1)

      val messageSpecData = getMessageSpecData(FATCA, reportingPeriod = reportingPeriod, giin = Some("giin"), electionsRequired = false)

      val validatedFileData = getValidatedFileData(messageSpecData)

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Right(messageSpecData)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      fakeUpscanConnector.setDetails(uploadDetails)

      val request                = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual routes.CheckYourFileDetailsController.onPageLoad().url
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.data mustEqual getExpectedData(validatedFileData)
    }

    "must redirect to required giin page if giin missing for FATCA" in {
      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val reportingPeriod                                = LocalDate.of(reportingPeriodLowerBound - 1, 1, 1)

      val messageSpecData = getMessageSpecData(FATCA, reportingPeriod = reportingPeriod)

      val validatedFileData = getValidatedFileData(messageSpecData)

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Right(messageSpecData)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      fakeUpscanConnector.setDetails(uploadDetails)

      val request                = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual routes.RequiredGiinController.onPageLoad(NormalMode).url
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.data mustEqual getExpectedData(validatedFileData)
    }

    "must redirect to invalid reporting period page if an invalid reporting period error is returned" in {
      fakeUpscanConnector.setDetails(uploadDetails)

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Left(ReportingPeriodError)))

      val controller             = application.injector.instanceOf[FileValidationController]
      val result: Future[Result] = controller.onPageLoad()(FakeRequest("", ""))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual routes.ReportingPeriodErrorController.onPageLoad().url
    }

    "must return ThereIsAProblemPage when a valid UploadId cannot be found" in {

      val application: _root_.play.api.Application = getApplication(emptyUserAnswers)

      running(application) {
        val request = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThereIsAProblemView]

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to invalid message type if an invalid message type error is returned" in {

      fakeUpscanConnector.setDetails(uploadDetails)

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Left(IncorrectMessageTypeError)))

      val controller             = application.injector.instanceOf[FileValidationController]
      val result: Future[Result] = controller.onPageLoad()(FakeRequest("", ""))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual routes.InvalidMessageTypeErrorController.onPageLoad().url
    }

    "must return ThereIsAProblemPage when meta data cannot be found" in {

      fakeUpscanConnector.resetDetails()

      running(application) {
        val request = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThereIsAProblemView]

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to fi not found page when an FI ID not matching error returned from backend" in {

      fakeUpscanConnector.setDetails(uploadDetails)

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Left(FIIDNotMatchingError)))

      val controller             = application.injector.instanceOf[FileValidationController]
      val result: Future[Result] = controller.onPageLoad()(FakeRequest("", ""))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual routes.FINotMatchingController.onPageLoad().url
    }

    "must redirect to file error page if XML parser fails" in {

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val expectedData = Json.obj(
        "invalidXML"               -> fileName,
        UploadIDPage.toString      -> UploadId("123"),
        FileReferencePage.toString -> fileReferenceId
      )

      fakeUpscanConnector.setDetails(uploadDetails)

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Left(InvalidXmlFileError)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val controller             = application.injector.instanceOf[FileValidationController]
      val result: Future[Result] = controller.onPageLoad()(FakeRequest("", ""))

      status(result) mustBe SEE_OTHER
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())
      redirectLocation(result) mustBe Some(routes.FileErrorController.onPageLoad().url)
      userAnswersCaptor.getValue.data mustEqual expectedData
    }
  }

  private def getApplication(userAnswers: UserAnswers) =
    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[UpscanConnector].toInstance(fakeUpscanConnector),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[ValidationConnector].toInstance(mockValidationConnector)
      )
      .build()

  private def getExpectedData(validatedFileData: ValidatedFileData) =
    Json.obj(
      UploadIDPage.toString          -> uploadId,
      FileReferencePage.toString     -> fileReferenceId,
      ValidXMLPage.toString          -> validatedFileData,
      URLPage.toString               -> downloadURL,
      RequiresElectionsPage.toString -> validatedFileData.messageSpecData.electionsRequired
    )
}
