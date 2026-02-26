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
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.FileNotAcceptedView
import models.{CRS, FATCA}

class FileNotAcceptedControllerSpec extends SpecBase {

  "FileNotAccepted Controller" - {

    "must return OK and the correct CRS view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.FileNotAcceptedController.onPageLoad(CRS).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileNotAcceptedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CRS.toString)(request, messages(application)).toString
      }
    }

    "must return OK and the correct FATCA view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.FileNotAcceptedController.onPageLoad(FATCA).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileNotAcceptedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(FATCA.toString)(request, messages(application)).toString
      }
    }
  }
}
