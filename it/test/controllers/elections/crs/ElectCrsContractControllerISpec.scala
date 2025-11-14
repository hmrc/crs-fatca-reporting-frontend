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

package controllers.elections.crs

import models.{CRS, MessageSpecData}
import pages.ValidXMLPage
import utils.ISpecBehaviours

import java.time.LocalDate

class ElectCrsContractControllerISpec extends ISpecBehaviours {

  private val pageUrl = "/report/elections/crs/contracts"
  val fiNameFM = "testFIFromFM"
  val messageSpecData = MessageSpecData(CRS, "testFI", "testRefId", "testReportingName", LocalDate.of(2000, 1, 1), giin = None, fiNameFM)
  val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(messageSpecData))

  "GET ContactEmailController.onPageLoad" must {
    behave like pageLoads(pageUrl, "electCrsContract.title",userAnswers)
    behave like pageRedirectsWhenNotAuthorised(pageUrl)
  }

}
