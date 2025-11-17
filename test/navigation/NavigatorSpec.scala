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

package navigation

import base.SpecBase
import controllers.routes
import models.*
import pages.*

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from /file-validation" - {

        "to /required-giin" - {
          "when message type is FATCA and GIIN is not held" in {
            val msd         = MessageSpecData(FATCA, "testFI", "testRefId", "testReportingName", LocalDate.now(), giin = None, "testFiNameFromFim")
            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe routes.RequiredGiinController.onPageLoad(NormalMode)
          }
        }
        "to /check-your-answers" - {
          "when message type is FATCA and GIIN is held and does not require an election " in {
            val msd = MessageSpecData(FATCA, "testFI", "testRefId", "testReportingName", LocalDate.of(2000, 1, 1), giin = Some("giin"), "testFiNameFromFim")
            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
          }
          "when message type is CRS and does not require an election" in {
            val msd         = MessageSpecData(CRS, "testFI", "testRefId", "testReportingName", LocalDate.of(2000, 1, 1), giin = None, "testFiNameFromFim")
            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
          }
        }
        "to /report-elections" - {
          "when message type is FATCA and GIIN is held and requires an election " in {
            val msd         = MessageSpecData(FATCA, "testFI", "testRefId", "testReportingName", LocalDate.now(), giin = Some("giin"), "testFiNameFromFim")
            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode)
          }
          "when message type is CRS and requires an election" in {
            val msd         = MessageSpecData(CRS, "testFI", "testRefId", "testReportingName", LocalDate.now(), giin = None, "testFiNameFromFim")
            val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(msd))

            navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode)
          }
        }
      }

      "must go from /required-giin" - {
        "to /report-elections when requires an election" in {
          val msd = MessageSpecData(CRS, "testFI", "testRefId", "testReportingName", LocalDate.now(), giin = None, "testFiNameFromFim")
          val userAnswers = emptyUserAnswers
            .withPage(ValidXMLPage, getValidatedFileData(msd))
            .withPage(RequiredGiinPage, "testGIIN")

          navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe controllers.elections.routes.ReportElectionsController.onPageLoad(NormalMode)
        }
        "to /check-your-answers when elections made already" in {
          val msd = MessageSpecData(CRS, "testFI", "testRefId", "testReportingName", LocalDate.of(2000, 1, 1), giin = None, "testFiNameFromFim")
          val userAnswers = emptyUserAnswers
            .withPage(ValidXMLPage, getValidatedFileData(msd))
            .withPage(RequiredGiinPage, "testGIIN")

          navigator.nextPage(ValidXMLPage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "must go from /elections/fatca/thresholds" - {
        "to /check-your-answers when elections made already" in {
          val msd = MessageSpecData(FATCA, "testFI", "testRefId", "testReportingName", LocalDate.of(2000, 1, 1), giin = None, "testFiNameFromFim")
          val userAnswers = emptyUserAnswers
            .withPage(ValidXMLPage, getValidatedFileData(msd))
            .withPage(ElectFatcaThresholdsPage, true)

          navigator.nextPage(ElectFatcaThresholdsPage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to Index controller" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }
    }
  }

}
