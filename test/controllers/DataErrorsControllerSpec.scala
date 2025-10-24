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
import pages.{GenericErrorPage, InvalidXMLPage, MessageTypePage}
import models.{GenericError, Message, UserAnswers}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class DataErrorsControllerSpec extends SpecBase {

  val testFileName = "test_file.xml"

  val testErrors = Seq(
    GenericError(1, Message("Test Message", List("Arg")))
  )
  val testMessageType = "CRS"

  val populatedUserAnswers: UserAnswers = emptyUserAnswers
    .set(InvalidXMLPage, testFileName)
    .success
    .value
    .set(GenericErrorPage, testErrors)
    .success
    .value
    .set(MessageTypePage, testMessageType)
    .success
    .value

  "DataErrors Controller" - {

    "must return OK and the correct view for a GET when all data is present" in {

      val application = applicationBuilder(userAnswers = Some(populatedUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.DataErrorsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) must include(s"We cannot accept the file $testFileName because it does not meet the CRS data requirements.")
      }
    }

    "must return InternalServerError if required data is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.DataErrorsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }
  }
}
