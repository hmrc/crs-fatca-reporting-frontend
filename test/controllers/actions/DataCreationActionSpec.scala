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

package controllers.actions

import base.SpecBase
import models.UserAnswers
import models.requests.{DataRequest, OptionalDataRequest}
import org.scalatestplus.mockito.MockitoSugar
import pages.elections.crs.ThresholdsPage
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DataCreationActionSpec extends SpecBase with MockitoSugar {

  class Harness extends DataCreationActionImpl()(global) {

    override def transform[A](request: OptionalDataRequest[A]): Future[DataRequest[A]] = super.transform(request)

  }

  "DataCreationAction" - {
    "must clear all the data in the user answers" in {
      val action              = new Harness
      val userAnswers         = UserAnswers("id").withPage(ThresholdsPage, false)
      val optionalDataRequest = OptionalDataRequest(FakeRequest(), "id", Some(userAnswers), "fatcaId")

      optionalDataRequest.userAnswers mustEqual Some(userAnswers)

      val result = action.transform(optionalDataRequest).futureValue

      result.userAnswers.get(ThresholdsPage) mustEqual None
      result.userAnswers.data mustEqual Json.obj()
    }
  }
}
