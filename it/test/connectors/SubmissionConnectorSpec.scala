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
import models.CRS
import models.submission.{CrsElectionsDetails, ElectionsGiinSubmissionResults, ElectionsSubmissionDetails, FatcaElectionsDetails, GiinAndElectionSubmissionRequest, GiinUpdateRequest}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.http.Status.*
import submissions.createFatcaElectionsDetails
import utils.ISpecBase

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class SubmissionConnectorSpec extends AnyFreeSpec with ISpecBase {

  lazy val connector: SubmissionConnector = app.injector.instanceOf[SubmissionConnector]

  "SubmissionConnector" - {
    val updateGiinUrl           = "/crs-fatca-reporting/update/giin"
    val submitElectionsUrl      = "/crs-fatca-reporting/elections/submit"

    "updateGiin" - {

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

      val crsDetails: Option[CrsElectionsDetails] = Some(CrsElectionsDetails(Some(true), Some(true), Some(true), Some(true)))
      val electionsSubmissionRequest =
        ElectionsSubmissionDetails(getMessageSpecData(CRS).sendingCompanyIN,
                                   getMessageSpecData(CRS).reportingPeriod.getYear.toString,
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

    "Submit Giin and Elections" - {

      "should return a ElectionsGiinSubmissionResults with giinUpdated true and electionsSubmitted true for a request payload that does not require the two updates" in {

        val payload = GiinAndElectionSubmissionRequest(None, None)

        val result = Await.result(connector.submitGinAndElections(payload), 2.seconds)

        result mustBe ElectionsGiinSubmissionResults(Some(true), Some(true))
      }

      "should return giinUpdated true and electionsSubmitted true for a request that successfully calls both updates" in {
        stubPostResponse(updateGiinUrl, NO_CONTENT)
        stubPostResponse(submitElectionsUrl, NO_CONTENT)

        val payload = GiinAndElectionSubmissionRequest(
          Some(
            GiinUpdateRequest(
              subscriptionId = "12345",
              fiid = "testFI",
              giin = "testGin"
            )
          ),
          Some(
            ElectionsSubmissionDetails(fiId = "testFI",
              reportingPeriod = LocalDate.now().toString,
              crsDetails = None,
              fatcaDetails = Some(createFatcaElectionsDetails())
            )
          )
        )

        val result = Await.result(connector.submitGinAndElections(payload), 2.seconds)

        result mustBe ElectionsGiinSubmissionResults(Some(true), Some(true))
      }

      "should return giinUpdated false and electionsSubmitted true for a request that fails on giin update and passes election update" in {
        stubPostResponse(updateGiinUrl, BAD_REQUEST)
        stubPostResponse(submitElectionsUrl, NO_CONTENT)

        val payload = GiinAndElectionSubmissionRequest(
          Some(
            GiinUpdateRequest(
              subscriptionId = "12345",
              fiid = "testFI",
              giin = "testGin"
            )
          ),
          Some(
            ElectionsSubmissionDetails(fiId = "testFI",
              reportingPeriod = LocalDate.now().toString,
              crsDetails = None,
              fatcaDetails = Some(createFatcaElectionsDetails())
            )
          )
        )

        val result = Await.result(connector.submitGinAndElections(payload), 2.seconds)

        result mustBe ElectionsGiinSubmissionResults(Some(false), Some(true))
      }

      "should return giinUpdated true and electionsSubmitted false for a request that succeeds on giin update and fails election update" in {
        stubPostResponse(updateGiinUrl, NO_CONTENT)
        stubPostResponse(submitElectionsUrl, INTERNAL_SERVER_ERROR)

        val payload = GiinAndElectionSubmissionRequest(
          Some(
            GiinUpdateRequest(
              subscriptionId = "12345",
              fiid = "testFI",
              giin = "testGin"
            )
          ),
          Some(
            ElectionsSubmissionDetails(fiId = "testFI",
              reportingPeriod = LocalDate.now().toString,
              crsDetails = None,
              fatcaDetails = Some(createFatcaElectionsDetails())
            )
          )
        )

        val result = Await.result(connector.submitGinAndElections(payload), 2.seconds)

        result mustBe ElectionsGiinSubmissionResults(Some(true), Some(false))
      }

      "should return giinUpdated false and electionsSubmitted false for a request that fails on both updates" in {
        stubPostResponse(updateGiinUrl, INTERNAL_SERVER_ERROR)
        stubPostResponse(submitElectionsUrl, INTERNAL_SERVER_ERROR)

        val payload = GiinAndElectionSubmissionRequest(
          Some(
            GiinUpdateRequest(
              subscriptionId = "12345",
              fiid = "testFI",
              giin = "testGin"
            )
          ),
          Some(
            ElectionsSubmissionDetails(fiId = "testFI",
              reportingPeriod = LocalDate.now().toString,
              crsDetails = None,
              fatcaDetails = Some(createFatcaElectionsDetails())
            )
          )
        )

        val result = Await.result(connector.submitGinAndElections(payload), 2.seconds)

        result mustBe ElectionsGiinSubmissionResults(Some(false), Some(false))
      }

      "should return giinUpdated true and electionsSubmitted false for a request that does not require a giin update and a failed election update" in {
        stubPostResponse(submitElectionsUrl, INTERNAL_SERVER_ERROR)

        val payload = GiinAndElectionSubmissionRequest(
          None,
          Some(
            ElectionsSubmissionDetails(fiId = "testFI",
              reportingPeriod = LocalDate.now().toString,
              crsDetails = None,
              fatcaDetails = Some(createFatcaElectionsDetails())
            )
          )
        )

        val result = Await.result(connector.submitGinAndElections(payload), 2.seconds)

        result mustBe ElectionsGiinSubmissionResults(Some(true), Some(false))
      }

      "should return giinUpdated false and electionsSubmitted true for a request giin update fails and an election update is not required" in {
        stubPostResponse(updateGiinUrl, INTERNAL_SERVER_ERROR)

        val payload = GiinAndElectionSubmissionRequest(
          Some(
            GiinUpdateRequest(
              subscriptionId = "12345",
              fiid = "testFI",
              giin = "testGin"
            )
          ),
          None
        )

        val result = Await.result(connector.submitGinAndElections(payload), 2.seconds)

        result mustBe ElectionsGiinSubmissionResults(Some(false), Some(true))
      }
    }
  }
}
