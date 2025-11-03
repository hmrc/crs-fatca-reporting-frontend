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
import models.{CRS, FIIDNotMatchingError, IncorrectMessageTypeError, InvalidXmlFileError, MessageSpecData, NormalMode, ReportingPeriodError, UserAnswers, ValidatedFileData}
import org.bson.types.ObjectId
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.{FileReferencePage, UploadIDPage, URLPage, ValidXMLPage}
import play.api
import play.api.inject
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ThereIsAProblemView

import java.time.LocalDate
import scala.concurrent.{ExecutionContextExecutor, Future}

class FileValidationControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockValidationConnector: ValidationConnector = mock[ValidationConnector]
  val fakeUpscanConnector: FakeUpscanConnector     = app.injector.instanceOf[FakeUpscanConnector]

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  val currentYear = 2025
  val reportingPeriodLowerBound = currentYear - 12

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    super.beforeEach()
  }

  "FileValidationController" - {
    val uploadId        = UploadId("123")
    val fileReferenceId = Reference("fileReferenceId")
    val userAnswers     = UserAnswers(userAnswersId).withPage(UploadIDPage, uploadId).withPage(FileReferencePage, fileReferenceId)
    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[UpscanConnector].toInstance(fakeUpscanConnector),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[ValidationConnector].toInstance(mockValidationConnector)
      )
      .build()

    val downloadURL = "http://dummy-url.com"
    val FileSize    = 20L

    val uploadDetails = UploadSessionDetails(
      new ObjectId(),
      uploadId,
      Reference("123"),
      UploadedSuccessfully("afile", downloadURL, FileSize, "MD5:123")
    )

    "must redirect to ReportElectionsController and save data for a valid file with a valid reporting period" in {
      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val reportingPeriod = LocalDate.of(currentYear - 1, 1, 1)

      val messageSpecData = MessageSpecData(
        messageType = CRS,
        sendingCompanyIN = "sendingCompanyIN",
        messageRefId = "messageRefId",
        reportingFIName = "reportingFIName",
        reportingPeriod = reportingPeriod,
        giin = Some("giin"),
        fiNameFromFim = "fi-name"
      )

      val validatedFileData = ValidatedFileData("afile", messageSpecData, FileSize, "MD5:123")

      val expectedData: JsObject = Json.obj(
        UploadIDPage.toString      -> uploadId,
        FileReferencePage.toString -> fileReferenceId,
        ValidXMLPage.toString      -> validatedFileData,
        URLPage.toString           -> downloadURL
      )

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Right(messageSpecData)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      fakeUpscanConnector.setDetails(uploadDetails)

      val request                = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode).url
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.data mustEqual expectedData
    }

    "must redirect to IndexController and save data for a valid file with an invalid reporting period (outside 12-year window)" in {
      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val reportingPeriod = LocalDate.of(reportingPeriodLowerBound - 1, 1, 1)

      val messageSpecData = MessageSpecData(
        messageType = CRS,
        sendingCompanyIN = "sendingCompanyIN",
        messageRefId = "messageRefId",
        reportingFIName = "reportingFIName",
        reportingPeriod = reportingPeriod,
        giin = Some("giin"),
        fiNameFromFim = "fi-name"
      )

      val validatedFileData = ValidatedFileData("afile", messageSpecData, FileSize, "MD5:123")

      val expectedData: JsObject = Json.obj(
        UploadIDPage.toString      -> uploadId,
        FileReferencePage.toString -> fileReferenceId,
        ValidXMLPage.toString      -> validatedFileData,
        URLPage.toString           -> downloadURL
      )

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Right(messageSpecData)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      fakeUpscanConnector.setDetails(uploadDetails)

      val request                = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.data mustEqual expectedData
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

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UpscanConnector].toInstance(fakeUpscanConnector),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[ValidationConnector].toInstance(mockValidationConnector)
        )
        .build()

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
        "invalidXML" -> "afile",
        UploadIDPage.toString -> UploadId("123"),
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
}
