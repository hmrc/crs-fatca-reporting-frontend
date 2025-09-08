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
import models.fileDetails.BusinessRuleErrorCode.{CorrDocRefIdUnknown, InvalidMessageRefIDFormat}
import models.fileDetails.{FileErrors, FileValidationErrors, RecordError}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.RulesErrorHelper
import viewmodels.FileRejectedViewModel
import views.html.RulesErrorView

class RulesErrorControllerSpec extends SpecBase with RulesErrorHelper {

  "RulesError Controller" - {

    "must return OK and the correct view for a GET" in {
      val regimentType = "CRS"
      val fileName     = "filename.xml"

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RulesErrorController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RulesErrorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fileName, regimentType, errorLength = 1200, createFileRejectedViewModel())(request,
                                                                                                                          messages(application)
        ).toString
      }
    }
  }

}
