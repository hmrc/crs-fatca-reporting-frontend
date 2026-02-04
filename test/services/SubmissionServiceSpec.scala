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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.elections.crs.{DormantAccountsPage, ElectCrsCarfGrossProceedsPage, ElectCrsContractPage, ThresholdsPage}
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}
import pages.{ReportElectionsPage, RequiredGiinPage, ValidXMLPage}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SubmissionServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockConnector                     = mock[SubmissionConnector]
  private val service                           = new SubmissionService(mockConnector)
  implicit val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]
  val fakeRequest: FakeRequest[_]               = FakeRequest("PUT", "/test-url")
  implicit val request: DataRequest[_]          = DataRequest(fakeRequest, "userId", emptyUserAnswers, "fatcaId")

  override def beforeEach(): Unit =
    super.beforeEach()
    reset(mockConnector)

  lazy val baseUaFatca: UserAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(getMessageSpecData(FATCA, FATCAReportType.TestData)))
  lazy val baseUaCRS: UserAnswers   = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(getMessageSpecData(CRS, CRSReportType.TestData)))

  "submitElectionsAndGiin" - {

    "returns GiinAndElectionSubmittedSuccessful when there are no giin or elections to submit for Fatca regime" in {

      val result = service.submitElectionsAndGiin(baseUaFatca).futureValue

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateNeverCalled()
      verifyElectionsSubmitNeverCalled()
    }

    "returns GiinAndElectionSubmittedSuccessful when there are no giin or elections to submit for crs regime" in {
      val result = service.submitElectionsAndGiin(baseUaCRS).futureValue

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateNeverCalled()
      verifyElectionsSubmitNeverCalled()
    }

    "returns GiinAndElectionSubmittedSuccessful when there is no giin and user chooses not to submit elections for fatca" in {
      lazy val uaWithElectionsAndGiinNotGiven: UserAnswers = baseUaFatca.withPage(ReportElectionsPage, false)
      val result                                           = service.submitElectionsAndGiin(uaWithElectionsAndGiinNotGiven).futureValue

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateNeverCalled()
      verifyElectionsSubmitNeverCalled()
    }

    "returns GiinAndElectionSubmittedSuccessful when there is no giin and user chooses not to submit elections for CRS" in {
      lazy val uaWithElectionsAndGiinNotGiven: UserAnswers = baseUaCRS.withPage(ReportElectionsPage, false)
      val result                                           = service.submitElectionsAndGiin(uaWithElectionsAndGiinNotGiven).futureValue

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateNeverCalled()
      verifyElectionsSubmitNeverCalled()
    }

    "returns GiinAndElectionSubmittedSuccessful when both submissions are successful for Fatca" in {
      lazy val uaWithBoth: UserAnswers = baseUaFatca
        .withPage(RequiredGiinPage, "testGiin")
        .withPage(ElectFatcaThresholdsPage, false)
        .withPage(TreasuryRegulationsPage, true)
        .withPage(ReportElectionsPage, true)

      when(mockConnector.updateGiin(any[GiinUpdateRequest])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))
      when(mockConnector.submitElections(any[ElectionsSubmissionDetails])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))

      val result = service.submitElectionsAndGiin(uaWithBoth).futureValue

      val electionsSubmissionDetailsCaptor = ArgumentCaptor.forClass(classOf[ElectionsSubmissionDetails])
      val giinCaptor                       = ArgumentCaptor.forClass(classOf[GiinUpdateRequest])

      verify(mockConnector).updateGiin(giinCaptor.capture())(using any[HeaderCarrier], any[ExecutionContext])
      verify(mockConnector).submitElections(electionsSubmissionDetailsCaptor.capture())(using any[HeaderCarrier], any[ExecutionContext])

      val giinValue = giinCaptor.getValue
      giinValue.giin mustBe "testGiin"
      giinValue.fiid mustBe "testFI"
      giinValue.subscriptionId mustBe "fatcaId"

      val electionsValue = electionsSubmissionDetailsCaptor.getValue
      electionsValue.crsDetails mustBe None
      electionsValue.fatcaDetails.isDefined mustBe true
      electionsValue.fiId mustBe "testFI"
      electionsValue.reportingPeriod mustBe "2026"
      electionsValue.fatcaDetails.get mustBe FatcaElectionsDetails(Some(false), Some(true))

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateCalledOnce()
      verifyElectionsSubmitCalledOnce()
    }

    "returns GiinAndElectionSubmittedSuccessful for a successful CRS election submission" in {
      lazy val uaWithBoth: UserAnswers = baseUaCRS
        .withPage(ReportElectionsPage, true)
        .withPage(ElectCrsCarfGrossProceedsPage, false)
        .withPage(ElectCrsContractPage, true)
        .withPage(DormantAccountsPage, false)
        .withPage(ThresholdsPage, true)

      when(mockConnector.submitElections(any[ElectionsSubmissionDetails])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))

      val result = service.submitElectionsAndGiin(uaWithBoth).futureValue

      val electionsSubmissionDetailsCaptor = ArgumentCaptor.forClass(classOf[ElectionsSubmissionDetails])
      verify(mockConnector).submitElections(electionsSubmissionDetailsCaptor.capture())(using any[HeaderCarrier], any[ExecutionContext])
      val electionsValue = electionsSubmissionDetailsCaptor.getValue
      electionsValue.fiId mustBe "testFI"
      electionsValue.reportingPeriod mustBe "2026"
      electionsValue.fatcaDetails mustBe None
      electionsValue.crsDetails.isDefined mustBe true
      electionsValue.crsDetails.get mustBe CrsElectionsDetails(Some(false), Some(true), Some(false), Some(true))

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateNeverCalled()
      verifyElectionsSubmitCalledOnce()
    }

    "returns GiinAndElectionSubmittedSuccessful when GIIN is not needed and submitElections succeeds" in {
      val answersWithGiinNotRequired = emptyUserAnswers
        .withPage(ValidXMLPage, getValidatedFileData(getMessageSpecData(FATCA, FATCAReportType.TestData, giin = Some("has existing giin"))))
        .withPage(ElectFatcaThresholdsPage, false)
        .withPage(TreasuryRegulationsPage, true)
        .withPage(ReportElectionsPage, true)

      when(mockConnector.submitElections(any[ElectionsSubmissionDetails])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))

      val result = service.submitElectionsAndGiin(answersWithGiinNotRequired).futureValue

      val electionsSubmissionDetailsCaptor = ArgumentCaptor.forClass(classOf[ElectionsSubmissionDetails])
      verify(mockConnector).submitElections(electionsSubmissionDetailsCaptor.capture())(using any[HeaderCarrier], any[ExecutionContext])
      val electionsValue = electionsSubmissionDetailsCaptor.getValue
      electionsValue.crsDetails mustBe None
      electionsValue.fatcaDetails.isDefined mustBe true
      electionsValue.fiId mustBe "testFI"
      electionsValue.reportingPeriod mustBe "2026"
      electionsValue.fatcaDetails.get mustBe FatcaElectionsDetails(Some(false), Some(true))

      result mustBe GiinAndElectionSubmittedSuccessful
      verifyGiinUpdateNeverCalled()
      verifyElectionsSubmitCalledOnce()
    }

    "returns GiinUpdateFail when GIIN fails and submitElections is not needed" in {
      lazy val ua: UserAnswers = baseUaFatca
        .withPage(RequiredGiinPage, "testGiin")
        .withPage(ReportElectionsPage, false)

      when(mockConnector.updateGiin(any[GiinUpdateRequest])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(false))

      val result = service.submitElectionsAndGiin(ua).futureValue

      result mustBe GiinUpdateFailed(false, true)
      verifyGiinUpdateCalledOnce()
      verifyElectionsSubmitNeverCalled()
    }

    "returns ElectionsSubmitFail when GIIN succeeds and submitElections fails" in {
      lazy val ua: UserAnswers = baseUaFatca
        .withPage(RequiredGiinPage, "testGiin")
        .withPage(ElectFatcaThresholdsPage, false)
        .withPage(TreasuryRegulationsPage, true)
        .withPage(ReportElectionsPage, true)

      when(mockConnector.updateGiin(any[GiinUpdateRequest])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(true))
      when(mockConnector.submitElections(any[ElectionsSubmissionDetails])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(false))

      val result = service.submitElectionsAndGiin(ua).futureValue

      result mustBe ElectionsSubmitFailed(true, false)
      verifyGiinUpdateCalledOnce()
      verifyElectionsSubmitCalledOnce()
    }

    "returns ElectionsSubmitFail when GIIN is not requires and submitElections fails" in {
      lazy val ua: UserAnswers = baseUaCRS
        .withPage(ElectCrsCarfGrossProceedsPage, false)
        .withPage(ElectCrsContractPage, true)
        .withPage(DormantAccountsPage, false)
        .withPage(ThresholdsPage, true)
        .withPage(ReportElectionsPage, true)

      when(mockConnector.submitElections(any[ElectionsSubmissionDetails])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(false))

      val result = service.submitElectionsAndGiin(ua).futureValue

      result mustBe ElectionsSubmitFailed(true, false)
      verifyGiinUpdateNeverCalled()
      verifyElectionsSubmitCalledOnce()
    }

    "should propagate the exception if one of the connectors fail unexpectedly" in {
      lazy val uaWithBoth: UserAnswers = baseUaFatca
        .withPage(RequiredGiinPage, "testGiin")
        .withPage(ElectFatcaThresholdsPage, false)
        .withPage(TreasuryRegulationsPage, true)
        .withPage(ReportElectionsPage, true)

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
