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

package connectors

import com.github.tomakehurst.wiremock.http.Fault
import models.{CRS, CRSReportType}
import models.submission.{CrsElectionsDetails, ElectionsSubmissionDetails, GiinUpdateRequest}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.http.Status.*
import utils.ISpecBase

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class SubmissionConnectorSpec extends AnyFreeSpec with ISpecBase {

  lazy val connector: SubmissionConnector = app.injector.instanceOf[SubmissionConnector]

  "SubmissionConnector" - {
    "updateGiin" - {

      val updateGiinUrl     = "/crs-fatca-reporting/update/giin"
      val giinUpdateRequest = GiinUpdateRequest("testSubId", "testFiid", "testGiin")

      "should return true on successful GIIN update (NO_CONTENT)" in {
        stubPostResponse(updateGiinUrl, NO_CONTENT)

        val result = Await.result(connector.updateGiin(giinUpdateRequest), 2.seconds)

        result mustBe true
      }

      "should return false when the backend returns BAD_REQUEST" in {
        stubPostResponse(updateGiinUrl, BAD_REQUEST)

        val result = Await.result(connector.updateGiin(giinUpdateRequest), 2.seconds)

        result mustBe false
      }

      "should return false when the backend returns INTERNAL_SERVER_ERROR" in {
        stubPostResponse(updateGiinUrl, INTERNAL_SERVER_ERROR)

        val result = Await.result(connector.updateGiin(giinUpdateRequest), 2.seconds)

        result mustBe false
      }

      "should return false when the backend returns an unexpected error status (e.g. FORBIDDEN)" in {
        stubPostResponse(updateGiinUrl, FORBIDDEN)

        val result = Await.result(connector.updateGiin(giinUpdateRequest), 2.seconds)

        result mustBe false
      }

      "should return false when there is a NonFatal connection failure (e.g timeout)" in {
        stubPostFault(
          updateGiinUrl,
          Fault.EMPTY_RESPONSE
        )

        val result = Await.result(connector.updateGiin(giinUpdateRequest), 2.seconds)

        result mustBe false
      }

    }
    "submitElections" - {
      val submitElectionsUrl                      = "/crs-fatca-reporting/elections/submit"
      val crsDetails: Option[CrsElectionsDetails] = Some(CrsElectionsDetails(Some(true), Some(true), Some(true), Some(true)))
      val electionsSubmissionRequest =
        ElectionsSubmissionDetails(getMessageSpecData(CRS,CRSReportType.TestData).sendingCompanyIN,
                                   getMessageSpecData(CRS,CRSReportType.TestData).reportingPeriod.getYear.toString,
                                   crsDetails,
                                   fatcaDetails = None
        )
      "should return true on successful submission (NO_CONTENT)" in {
        stubPostResponse(submitElectionsUrl, NO_CONTENT)

        val result = Await.result(connector.submitElections(electionsSubmissionRequest), 2.seconds)

        result mustBe true
      }
      "should return false when the backend returns INTERNAL_SERVER_ERROR" in {
        stubPostResponse(submitElectionsUrl, INTERNAL_SERVER_ERROR)

        val result = Await.result(connector.submitElections(electionsSubmissionRequest), 2.seconds)

        result mustBe false
      }

      "should return false when an UpstreamErrorResponse is encountered (e.g. FORBIDDEN)" in {

        stubPostResponse(submitElectionsUrl, FORBIDDEN)

        val result = Await.result(connector.submitElections(electionsSubmissionRequest), 2.seconds)

        result mustBe false
      }

      "should return false when there is a NonFatal connection failure (e.g timeout)" in {
        stubPostFault(submitElectionsUrl, Fault.EMPTY_RESPONSE)

        val result = Await.result(connector.submitElections(electionsSubmissionRequest), 2.seconds)

        result mustBe false
      }
    }
  }
}
