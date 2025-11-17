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

package controllers.crs

import models.{CRS, MessageSpecData}
import pages.ValidXMLPage
import utils.ISpecBehaviours

import java.time.LocalDate


class DormantAccountsControllerISpec extends ISpecBehaviours {

  private val path = "/report/elections/crs/dormant-accounts"
  val fiNameFM = "testFIFromFM"
  val messageSpecData = MessageSpecData(CRS, "testFI", "testRefId", "testReportingName", LocalDate.of(2000, 1, 1), giin = None, fiNameFM)
  val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(messageSpecData))

  "GET DormantAccountsController.onPageLoad" must {
    behave like pageLoads(path = path, pageTitle = "dormantAccounts.title", userAnswers = userAnswers)
    behave like pageRedirectsWhenNotAuthorised(path)
  }

  "Post DormantAccountsController.onSubmit" must {
    val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("true"))

    behave like standardOnSubmit(path, requestBody)
    behave like pageSubmits(path, "/report/elections/crs/thresholds", userAnswers, requestBody)
  }
}
