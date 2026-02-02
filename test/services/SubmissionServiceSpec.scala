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

package services

import base.SpecBase
import connectors.SubmissionConnector
import models.requests.DataRequest
import models.submission.*
import models.{CRS, CRSReportType, FATCA, FATCAReportType, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.{ReportElectionsPage, RequiredGiinPage, ValidXMLPage}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SubmissionServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockConnector                     = mock[SubmissionConnector]
  private val service                           = new SubmissionService(mockConnector)
  implicit val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]
  val fakeRequest: FakeRequest[_]               = FakeRequest("PUT", "/test-url")
  implicit val request: DataRequest[_]          = DataRequest(fakeRequest, "userId", baseUa, "fatcaId")

  override def beforeEach(): Unit =
    super.beforeEach()
    reset(mockConnector)

  lazy val baseUa: UserAnswers                  = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(getMessageSpecData(CRS, CRSReportType.TestData)))
  lazy val uaWithGiin: UserAnswers              = baseUa.withPage(RequiredGiinPage, "testGiin")
  lazy val uaWithElections: UserAnswers         = baseUa.withPage(ReportElectionsPage, true)
  lazy val uaWithElectionsNotGiven: UserAnswers = baseUa.withPage(ReportElectionsPage, false)
  lazy val uaWithBoth: UserAnswers              = uaWithGiin.withPage(ReportElectionsPage, true)

  "submitElectionsAndGiin" - {

    "returns GiinAndElectionSubmittedSuccessful when there are no giin or elections to submit" in {
      val result = service.submitElectionsAndGiin(baseUa).futureValue

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateNeverCalled()
      verifyElectionsSubmitNeverCalled()
    }

    "returns GiinAndElectionSubmittedSuccessful when there are no giin and user chose not to submit elections" in {
      val result = service.submitElectionsAndGiin(uaWithElectionsNotGiven).futureValue

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateNeverCalled()
      verifyElectionsSubmitNeverCalled()
    }

    "returns GiinAndElectionSubmittedSuccessful when both submissions are successful" in {
      when(mockConnector.updateGiin(any[GiinUpdateRequest])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))
      when(mockConnector.submitElections(any[ElectionsSubmissionDetails])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))

      val result = service.submitElectionsAndGiin(uaWithBoth).futureValue

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateCalledOnce()
      verifyElectionsSubmitCalledOnce()
    }

    "returns GiinAndElectionSubmittedSuccessful when GIIN is not needed and submitElections succeeds" in {
      when(mockConnector.submitElections(any[ElectionsSubmissionDetails])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))

      val result = service.submitElectionsAndGiin(uaWithElections).futureValue

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateNeverCalled()
      verifyElectionsSubmitCalledOnce()
    }

    "returns GiinUpdateFail when GIIN fails and submitElections is not needed" in {
      when(mockConnector.updateGiin(any[GiinUpdateRequest])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(false))

      val result = service.submitElectionsAndGiin(uaWithGiin).futureValue

      result mustBe GiinUpdateFailed(false, true)
      verifyGiinUpdateCalledOnce()
      verifyElectionsSubmitNeverCalled()
    }

    "returns ElectionsSubmitFail when GIIN succeeds and submitElections fails" in {
      when(mockConnector.updateGiin(any[GiinUpdateRequest])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))
      when(mockConnector.submitElections(any[ElectionsSubmissionDetails])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(false))

      val result = service.submitElectionsAndGiin(uaWithBoth).futureValue

      result mustBe ElectionsSubmitFailed(true, false)
      verifyGiinUpdateCalledOnce()
      verifyElectionsSubmitCalledOnce()
    }

    "should propagate the exception if one of the connectors fail unexpectedly" in {
      when(mockConnector.updateGiin(any[GiinUpdateRequest])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("Some exception")))
      when(mockConnector.submitElections(any[ElectionsSubmissionDetails])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))

      val thrown = service.submitElectionsAndGiin(uaWithBoth).failed.futureValue

      thrown.getMessage mustBe "Some exception"
      verifyGiinUpdateCalledOnce()
    }
  }

  private def verifyGiinUpdateNeverCalled(): Unit =
    verify(mockConnector, never()).updateGiin(any())(using any[HeaderCarrier], any[ExecutionContext])

  private def verifyElectionsSubmitNeverCalled(): Unit =
    verify(mockConnector, never()).submitElections(any())(using any[HeaderCarrier], any[ExecutionContext])

  private def verifyGiinUpdateCalledOnce(): Unit =
    verify(mockConnector).updateGiin(any())(using any[HeaderCarrier], any[ExecutionContext])

  private def verifyElectionsSubmitCalledOnce(): Unit =
    verify(mockConnector).submitElections(any())(using any[HeaderCarrier], any[ExecutionContext])

}
