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

package models

import com.github.tomakehurst.wiremock.client.WireMock.*
import controllers.actions.AuthenticatedIdentifierAction
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.ISpecBase

class AuthenticatedIdentifierActionISpec extends PlaySpec with ISpecBase {

  class Harness(authAction: AuthenticatedIdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { request =>
      val fatcaId = request.fatcaId
      play.api.mvc.Results.Ok(s"FATCA ID: $fatcaId")
    }
  }

  val testFatcaId = "XAFATCA0000123456"

  "Authenticated Identifier Action" when {

    "the user is authenticated and has a FATCA ID" must {
      "successfully authenticate and execute the request block" in {
        stubFor(post(urlEqualTo(authUrl)).willReturn(aResponse().withStatus(OK).withBody(authOKResponse(testFatcaId))))
        val request = FakeRequest("GET", "/")
        val harness = new Harness(app.injector.instanceOf[AuthenticatedIdentifierAction])
        val result  = await(harness.onPageLoad()(request))

        result.header.status mustBe OK
        result.body.toString must include(s"FATCA ID: $testFatcaId")
      }
    }

    "the user is not authenticated" must {
      "redirect to the government gateway sign-in page" in {
        stubFor(
          post(urlEqualTo(authUrl))
            .willReturn(aResponse().withStatus(401).withHeader("WWW-Authenticate", """MDTP-GG-Absence of bearer token"""))
        )
        val request = buildFakeRequest()
        val harness = new Harness(app.injector.instanceOf[AuthenticatedIdentifierAction])
        val result  = await(harness.onPageLoad()(request))

        result.header.status mustBe SEE_OTHER
        result.header.headers.get("Location") mustBe Some(s"http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9949%2Freport-for-crs-and-fatca")
      }
    }

    "the user is authenticated but does not have the FATCA enrolment" must {
      "redirect to the registration page" in {
        val json = """
                     |{
                     |  "internalId": "some-id",
                     |  "affinityGroup": "Organisation",
                     |  "allEnrolments": []
                     |}
                     |""".stripMargin
        stubFor(post(urlEqualTo(authUrl)).willReturn(aResponse().withStatus(OK).withBody(json)))

        val request = FakeRequest("GET", "/")
        val harness = new Harness(app.injector.instanceOf[AuthenticatedIdentifierAction])
        val result  = await(harness.onPageLoad()(request))

        result.header.status mustBe SEE_OTHER
        result.header.headers.get("Location") mustBe Some("http://localhost:10031/crs-fatca-registration")
      }
    }

    "the user is authenticated but the FATCA ID is empty" must {
      "redirect to the registration page" in {
        val json = """
                     |{
                     |  "internalId": "some-id",
                     |  "affinityGroup": "Organisation",
                     |  "allEnrolments": [ {
                     |    "key": "HMRC-FATCA-ORG",
                     |    "identifiers": [ {
                     |      "key": "FATCAID",
                     |      "value": ""
                     |    } ],
                     |    "state": "Activated"
                     |  } ]
                     |}
                     |""".stripMargin
        stubFor(post(urlEqualTo(authUrl)).willReturn(aResponse().withStatus(OK).withBody(json)))

        val request = FakeRequest("GET", "/")
        val harness = new Harness(app.injector.instanceOf[AuthenticatedIdentifierAction])
        val result = await(harness.onPageLoad()(request))

        result.header.status mustBe SEE_OTHER
        result.header.headers.get("Location") mustBe Some("http://localhost:10031/crs-fatca-registration")
      }
    }
  }
}