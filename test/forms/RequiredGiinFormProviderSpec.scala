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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class RequiredGiinFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "requiredGiin.error.required"
  val lengthKey = "requiredGiin.error.length"
  val invalidKey = "requiredGiin.error.invalid"
  val giinLength = 19

  val form = new RequiredGiinFormProvider()()

  val validGiinRegex = "^[0-9A-NP-Z]{6}\\.[0-9A-NP-Z]{5}\\.[A-NP-Z]{2}\\.[0-9]{3}$"

  ".requiredGiin" - {

    val fieldName = "requiredGiin"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(validGiinRegex)
    )

    "not bind strings that are too long" in {
      val tooLongString = RegexpGen.from(validGiinRegex).sample.get + "A"
      form.bind(Map(fieldName -> tooLongString)).errors should contain(
        FormError(fieldName, lengthKey)
      )
    }

    "not bind strings that are too short" in {
      val tooShortString = RegexpGen.from(validGiinRegex).sample.get.drop(1)
      form.bind(Map(fieldName -> tooShortString)).errors should contain(
        FormError(fieldName, lengthKey)
      )
    }

    "not bind strings that do not match the GIIN regex" in {
      val invalidGiin = "ABCDEF.12345.XY.123x" // Example: last char is 'x' instead of digit
      form.bind(Map(fieldName -> invalidGiin)).errors should contain(
        FormError(fieldName, invalidKey)
      )

      val invalidGiin2 = "ABCDEF.12345.XY.123" // Correct length but wrong format (e.g., lowercase in first block if not allowed)
      form.bind(Map(fieldName -> invalidGiin2)).errors should contain(
        FormError(fieldName, invalidKey)
      )
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}