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

package models.fileDetails

import play.api.libs.json.*

enum BusinessRuleErrorCode(val code: String):
  case InvalidMessageRefIDFormat extends BusinessRuleErrorCode("50008")
  case DocRefIDFormat extends BusinessRuleErrorCode("80001")
  case CorrDocRefIdUnknown extends BusinessRuleErrorCode("80002")
  case FailedSchemaValidationCrs extends BusinessRuleErrorCode("Temp CRS Error Code 2")
  case FailedSchemaValidationFatca extends BusinessRuleErrorCode("Temp FATCA Error Code 2")

  case UnknownErrorCode(override val code: String) extends BusinessRuleErrorCode(code)

object BusinessRuleErrorCode:

  private val values: Seq[BusinessRuleErrorCode] = Seq(
    InvalidMessageRefIDFormat,
    DocRefIDFormat,
    CorrDocRefIdUnknown,
    FailedSchemaValidationCrs,
    FailedSchemaValidationFatca
  )

  private val lookup: Map[String, BusinessRuleErrorCode] =
    values
      .map(
        value => value.code -> value
      )
      .toMap

  implicit val format: Format[BusinessRuleErrorCode] = new Format[BusinessRuleErrorCode] {
    def reads(json: JsValue): JsResult[BusinessRuleErrorCode] =
      json
        .validate[String]
        .map(
          code => lookup.getOrElse(code, UnknownErrorCode(code))
        )

    def writes(x: BusinessRuleErrorCode): JsValue =
      JsString(x.code)
  }
