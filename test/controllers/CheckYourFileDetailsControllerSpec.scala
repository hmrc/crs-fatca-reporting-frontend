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
import models.{CRS, MessageSpecData, ValidatedFileData}
import org.scalatest.matchers.must.Matchers
import pages.ValidXMLPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.CheckYourFileDetailsViewModel
import views.html.CheckYourFileDetailsView

import java.time.LocalDate

class CheckYourFileDetailsControllerSpec extends SpecBase with Matchers {

  "CheckYourFileDetails Controller" - {

    "must return OK and the correct view for a GET" in {
      val expectedFiName      = "fi-name"
      val fileName            = "test-file.xml"
      val FileSize            = 100L
      val FileChecksum        = "checksum"
      val reportingPeriodYear = 2025
      val messageSpecData = MessageSpecData(
        messageType = CRS,
        sendingCompanyIN = "sendingCompanyIN",
        messageRefId = "messageRefId",
        reportingFIName = "reportingFIName",
        reportingPeriod = LocalDate.of(reportingPeriodYear, 1, 1),
        giin = None,
        fiNameFromFim = expectedFiName
      )
      val crsValidatedFileData = ValidatedFileData(fileName, messageSpecData, FileSize, FileChecksum)
      val answers              = emptyUserAnswers.withPage(ValidXMLPage, crsValidatedFileData)

      val application         = applicationBuilder(userAnswers = Some(answers)).build()
      val messagesApi         = messages(application)
      val helperModel         = CheckYourFileDetailsViewModel(answers)(using messagesApi)
      val expectedFileDetails = helperModel.getYourFileDetailsRows
      val expectedFIDetails   = helperModel.getFIDetailsRows

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourFileDetailsController.onPageLoad().url)
        val view    = application.injector.instanceOf[CheckYourFileDetailsView]
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(expectedFileDetails, expectedFIDetails, crsValidatedFileData.messageSpecData.fiNameFromFim)(request, messagesApi).toString
      }
    }
  }
}
