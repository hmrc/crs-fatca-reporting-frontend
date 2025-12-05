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

import base.SpecBase
import models.UserAnswers.getMessageSpecData
import org.scalatestplus.mockito.MockitoSugar
import pages.{MessageTypePage, ReportElectionsPage, RequiredGiinPage, ValidXMLPage}

class UserAnswersSpec extends SpecBase with MockitoSugar {

  "getMessageSpecData" - {
    "return MessageSpecData when ValidXMLPage exists" in {
      val ua = emptyUserAnswers.set(ValidXMLPage, getValidatedFileData(testMsd)).get

      val result = getMessageSpecData(ua) {
        data =>
          data mustBe testMsd
          "ok"
      }

      result mustBe "ok"
    }
    "throw IllegalStateException when ValidXMLPage is missing" in {
      val ex = intercept[IllegalStateException] {
        getMessageSpecData(emptyUserAnswers) {
          _ => "should not reach here"
        }
      }

      ex.getMessage mustBe "ValidXMLPage is missing"
    }

  }

  "Remove" - {
    "a list of pages should be removed from UserAnswers" in {
      val ua = emptyUserAnswers
        .withPage(ReportElectionsPage, true)
        .withPage(RequiredGiinPage, "some data")
        .withPage(MessageTypePage, "msg type")

      val result = ua.removeAllFrom(Seq(ReportElectionsPage, MessageTypePage)).get

      result.get(ReportElectionsPage) mustBe None
      result.get(MessageTypePage) mustBe None
      result.get(RequiredGiinPage) mustBe Some("some data")
    }

    "a list of pages that do not exist should be ignored when removing from UserAnswers" in {
      val ua = emptyUserAnswers.withPage(RequiredGiinPage, "some data")

      val result = ua.removeAllFrom(Seq(ReportElectionsPage, MessageTypePage)).get

      result.get(RequiredGiinPage) mustBe Some("some data")
    }
  }

}
