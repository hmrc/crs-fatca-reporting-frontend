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

package utils

import models.UserAnswers
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.ws.DefaultBodyWritables.writeableOf_urlEncodedForm
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSResponse}
import play.api.mvc.*
import play.api.test.Helpers.{await, defaultAwaitTimeout}

trait ISpecBehaviours extends PlaySpec with ISpecBase {

  lazy val wsClient: WSClient                = app.injector.instanceOf[WSClient]
  val session: Session                       = Session(Map("authToken" -> "abc123"))
  val sessionCookieBaker: SessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
  val sessionCookie: Cookie                  = sessionCookieBaker.encodeAsCookie(session)
  val wsSessionCookie: DefaultWSCookie       = DefaultWSCookie(sessionCookie.name, sessionCookie.value)
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)
  val testFatcaID = "XE2ATCA0009234567"

  def pageRedirectsWhenNotAuthorised(path: String): Unit = {
    userNotAuthenticated(path,None)
    userDoesNotHaveFatcaEnrollment(path,None)
    userDoesNotHaveFatcaID(path,None)
  }
  private def userDoesNotHaveFatcaID(path: String, requestBody: Option[Map[String, Seq[String]]]): Unit = {
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

        val response: WSResponse = getResponse(path, requestBody)

        response.status mustBe SEE_OTHER
        response.header("Location").value mustBe "http://localhost:10030/register-for-crs-and-fatca"
      }
    }
  }

  private def getResponse(path: String, requestBody: Option[Map[String, Seq[String]]]) = {
    val response = requestBody match {
      case Some(value) => await(buildClient(path)
        .withFollowRedirects(false).addHttpHeaders("Csrf-Token" -> "nocheck").addCookies(wsSessionCookie).post(value))
      case None => await(buildClient(path).withFollowRedirects(false).addCookies(wsSessionCookie).get())
    }
    response
  }

  private def userDoesNotHaveFatcaEnrollment(path: String, requestBody: Option[Map[String, Seq[String]]]): Unit = {
    "the user is authenticated but does not have the FATCA enrolment" must {
      "redirect to the registration page" in {
        val json =
          """
            |{
            |  "internalId": "some-id",
            |  "affinityGroup": "Organisation",
            |  "allEnrolments": []
            |}
            |""".stripMargin
        stubPost(authUrl, OK, authRequest, json)

        val response: WSResponse = getResponse(path, requestBody)

        response.status mustBe SEE_OTHER
        response.header("Location").value mustBe "http://localhost:10030/register-for-crs-and-fatca"
      }
    }
  }

  private def userNotAuthenticated(path: String,requestBody: Option[Map[String, Seq[String]]]): Unit = {
    "the user is not authenticated" must {
      "redirect to the government gateway sign-in page" in {
        val response = await(
          requestBody match {
            case Some(value) => buildClient(path).addHttpHeaders("Csrf-Token" -> "nocheck").withFollowRedirects(false).post(value)
            case None => buildClient(path).withFollowRedirects(false).get()
          }
        )

        response.status mustBe SEE_OTHER
        response.header("Location").value must include("gg-sign-in")
      }
    }
  }

  def pageLoads(path: String, pageTitle: String, userAnswers: UserAnswers = emptyUserAnswers): Unit =
    "load relative page" in {
      stubAuthorised(testFatcaID)
      await(repository.set(userAnswers))

      val response = await(
        buildClient(path)
          .withFollowRedirects(false)
          .addCookies(wsSessionCookie)
          .get()
      )

      response.status mustBe OK
      val responseBody: String = response.body
      responseBody must include(messages(pageTitle))
    }

  def pageSubmits(path: String,
                  redirectLocation: String,
                  ua: UserAnswers = emptyUserAnswers,
                  requestBody: Map[String, Seq[String]] = Map("value" -> Seq("testValue"))
                 ): Unit =
    "should submit form" in {
      stubAuthorised(testFatcaID)
      await(repository.set(ua))

      val response = await(
        buildClient(path)
          .addCookies(wsSessionCookie)
          .addHttpHeaders("Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(requestBody)
      )

      response.status mustBe SEE_OTHER
      response.header("Location").value must
        include(redirectLocation)
      verifyPost(authUrl)
    }

  def standardOnSubmit(path: String, requestBody: Map[String, Seq[String]]): Unit = {
    userNotAuthenticated(path, Some(requestBody))
    userDoesNotHaveFatcaEnrollment(path, Some(requestBody))
    userDoesNotHaveFatcaID(path,Some(requestBody))
  }
}
