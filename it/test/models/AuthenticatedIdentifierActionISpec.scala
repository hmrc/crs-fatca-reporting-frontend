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

import connectors.UpscanConnector
import models.upscan.{PreparedUpload, Reference, UploadForm}
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.libs.ws.{DefaultWSCookie, WSClient}
import play.api.mvc.*
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.ISpecBase

class AuthenticatedIdentifierActionISpec extends PlaySpec with ISpecBase {

  val testFatcaId = "XAFATCA0000123456"

  lazy val wsClient: WSClient                = app.injector.instanceOf[WSClient]
  val session: Session                       = Session(Map("authToken" -> "abc123"))
  val sessionCookieBaker: SessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
  val sessionCookie: Cookie                  = sessionCookieBaker.encodeAsCookie(session)
  val wsSessionCookie: DefaultWSCookie       = DefaultWSCookie(sessionCookie.name, sessionCookie.value)
  lazy val connector: UpscanConnector = app.injector.instanceOf[UpscanConnector]
  val path = "/report/upload-file"

  "Authenticated Identifier Action" when {

    "the user is authenticated and has a FATCA ID" must {
      "return OK when the user is authorised" in {
        stubAuthorised("cbc12345")
        val upscanBody = PreparedUpload(Reference("Reference"), UploadForm("downloadUrl", Map("formKey" -> "formValue")))

        stubPostResponse("/upscan/v2/initiate", OK, Json.toJson(upscanBody).toString())

        val response = await(
          buildClient(path)
            .withFollowRedirects(false)
            .addCookies(wsSessionCookie)
            .get()
        )
        response.status mustBe OK
        verifyPost(authUrl)
        val body: String = response.body
        body must include("Send a CRS or FATCA report")
      }

    }
    "the user is not authenticated" must {
      "redirect to the government gateway sign-in page" in {
        val response = await(
          buildClient(path)
            .withFollowRedirects(false)
            .get()
        )
        response.status mustBe SEE_OTHER
        response.header("Location").value must include("gg-sign-in")
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
        stubPost(authUrl, OK, authRequest, json)

        val response = await(
          buildClient(path)
            .withFollowRedirects(false)
            .addCookies(wsSessionCookie)
            .get()
        )

        response.status mustBe SEE_OTHER
        response.header("Location").value mustBe "http://localhost:10030/register-for-crs-and-fatca"
      }
    }
    "the user is authenticated but the FATCA ID is empty" must {
      "redirect to the registration page" in {
        val json =
          """
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
        stubPost(authUrl, OK, authRequest, json)

        val response = await(
          buildClient(path)
            .withFollowRedirects(false)
            .addCookies(wsSessionCookie)
            .get()
        )

        response.status mustBe SEE_OTHER
        response.header("Location").value mustBe "http://localhost:10030/register-for-crs-and-fatca"
      }
    }
  }
}
