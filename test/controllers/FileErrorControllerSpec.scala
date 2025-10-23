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
import models.UserAnswers
import pages.InvalidXMLPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.{FileErrorView, ThereIsAProblemView}

class FileErrorControllerSpec extends SpecBase {

  "FileError Controller" - {

    val testFileName = "test_file.xml"

    val populatedUserAnswers: UserAnswers = emptyUserAnswers
      .set(InvalidXMLPage, testFileName)
      .success
      .value

    "must return OK and the correct view with the file name for a GET when data is available" in {

      val application = applicationBuilder(userAnswers = Some(populatedUserAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[FileErrorView]
        val request = FakeRequest(GET, routes.FileErrorController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testFileName)(request, messages(application)).toString
        contentAsString(result) must include(testFileName)
      }
    }

    "must return InternalServerError and the 'There is a problem' view for a GET when file name data is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val errorView = application.injector.instanceOf[ThereIsAProblemView]
        val request = FakeRequest(GET, routes.FileErrorController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual errorView()(request, messages(application)).toString
      }
    }
  }
}