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
import pages.elections.crs.{DormantAccountsPage, ElectCrsCarfGrossProceedsPage, ElectCrsContractPage, ElectCrsGrossProceedsPage, ThresholdsPage}
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}
import pages.{InvalidXMLPage, MessageTypePage, ReportElectionsPage, RequiredGiinPage, ValidXMLPage}

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

  "set reportElectionPages false" - {
    "removes all election related pages" in new TestContext {
      electionPages.foreach {
        p =>
          electionUserAnswers.get(p) mustEqual Some(true)
      }

      val result = electionUserAnswers.withPage(ReportElectionsPage, false)

      electionPages.foreach {
        p =>
          result.get(p) mustEqual None
      }
    }
  }

  "set ElectCrsCarfGrossProceedsPage to false" - {
    "removes ElectCrsGrossProceedsPage value" in {
      val userAnswers = emptyUserAnswers.withPage(ElectCrsGrossProceedsPage, false)

      userAnswers.get(ElectCrsGrossProceedsPage) mustEqual Some(false)

      val result = userAnswers.withPage(ElectCrsCarfGrossProceedsPage, false)

      result.get(ElectCrsGrossProceedsPage) mustEqual None
      result.get(ElectCrsCarfGrossProceedsPage) mustEqual Some(false)
    }
  }

  "adding ValidXmlPage" - {
    "removes election pages" in new TestContext {
      val userAnswers: UserAnswers = electionUserAnswers.withPage(ReportElectionsPage, true).withPage(RequiredGiinPage, "some-gin")

      uploadXmlPages.foreach {
        p =>
          userAnswers.get(p).isDefined mustEqual true
      }
      userAnswers.get(RequiredGiinPage) mustEqual Some("some-gin")

      val result = userAnswers.withPage(ValidXMLPage, getValidatedFileData())

      uploadXmlPages.foreach {
        p =>
          result.get(p) mustEqual None
      }
      result.get(RequiredGiinPage) mustEqual None
    }

    "removes invalid xml page" in new TestContext {
      val userAnswers: UserAnswers = emptyUserAnswers.withPage(InvalidXMLPage, "some-string")

      userAnswers.get(InvalidXMLPage) mustEqual Some("some-string")

      val result = userAnswers.withPage(ValidXMLPage, getValidatedFileData())

      result.get(InvalidXMLPage) mustEqual None
    }
  }

  "adding InValidXmlPage" - {
    "removes election pages" in new TestContext {
      val userAnswers: UserAnswers = electionUserAnswers.withPage(ReportElectionsPage, true).withPage(RequiredGiinPage, "some-gin")

      uploadXmlPages.foreach {
        p =>
          userAnswers.get(p).isDefined mustEqual true
      }
      userAnswers.get(RequiredGiinPage) mustEqual Some("some-gin")

      val result = userAnswers.withPage(InvalidXMLPage, "some-string")

      uploadXmlPages.foreach {
        p =>
          result.get(p) mustEqual None
      }
      result.get(RequiredGiinPage) mustEqual None
    }

    "removes valid xml page" in new TestContext {
      val userAnswers: UserAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData())

      userAnswers.get(ValidXMLPage) mustEqual Some(getValidatedFileData())

      val result = userAnswers.withPage(InvalidXMLPage, "some-string")

      result.get(ValidXMLPage) mustEqual None
    }
  }

  trait TestContext {

    val electionPages = Seq(
      TreasuryRegulationsPage,
      ElectFatcaThresholdsPage,
      ElectCrsContractPage,
      DormantAccountsPage,
      ThresholdsPage,
      ElectCrsCarfGrossProceedsPage,
      ElectCrsGrossProceedsPage,
      ElectCrsCarfGrossProceedsPage
    )
    val uploadXmlPages = electionPages ++ Seq(ReportElectionsPage)
    val tuple          = electionPages.map((_, true))

    val electionUserAnswers = tuple.foldLeft(emptyUserAnswers) {
      case (acc, (x, y)) =>
        acc.withPage(x, y)
    }
  }
}
