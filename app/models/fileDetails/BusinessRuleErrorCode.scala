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

sealed abstract class BusinessRuleErrorCode(val code: String)

object BusinessRuleErrorCode {
  case object InvalidMessageRefIDFormat extends BusinessRuleErrorCode("50008")
  case object DocRefIDFormat extends BusinessRuleErrorCode("80001")
  case object CorrDocRefIdUnknown extends BusinessRuleErrorCode("80002")
  case object FailedSchemaValidationCrs extends BusinessRuleErrorCode("Temp CRS Error Code 2")
  case object FailedSchemaValidationFatca extends BusinessRuleErrorCode("Temp FATCA Error Code 2")

  case class UnknownErrorCode(override val code: String) extends BusinessRuleErrorCode(code)

  val values: Seq[BusinessRuleErrorCode] = Seq(InvalidMessageRefIDFormat, DocRefIDFormat)

  implicit val writes: Writes[BusinessRuleErrorCode] = Writes[BusinessRuleErrorCode] {
    x =>
      JsString(x.code)
  }

  implicit val reads: Reads[BusinessRuleErrorCode] = __.read[String].map {
    case "50008" => InvalidMessageRefIDFormat
    case "80001" => DocRefIDFormat
    case "80002" => CorrDocRefIdUnknown
    case "Temp CRS Error Code 2" => FailedSchemaValidationCrs
    case "Temp FATCA Error Code 2" => FailedSchemaValidationFatca
    case otherCode => UnknownErrorCode(otherCode)

  }
}
