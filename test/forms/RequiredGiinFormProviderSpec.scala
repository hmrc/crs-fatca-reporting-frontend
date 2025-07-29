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
import org.scalatest.matchers.should.Matchers.should
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class RequiredGiinFormProviderSpec extends StringFieldBehaviours {

  val requiredKey          = "requiredGiin.error.required"
  val lengthKey            = "requiredGiin.error.length"
  val invalidKey           = "requiredGiin.error.format"
  val invalidCharactersKey = "requiredGiin.error.invalidCharacters"
  val giinLength           = 19

  val form = new RequiredGiinFormProvider()()

  val validGiinRegex = "^[0-9A-NP-Z]{6}\\.[0-9A-NP-Z]{5}\\.[A-NP-Z]{2}\\.[0-9]{3}$"

  ".value" - {

    val fieldName = "value"

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

    "not bind strings that contain invalid characters" in {
      val invalidCharGiin = "ABCDEF.12345.XY.12!"
      form.bind(Map(fieldName -> invalidCharGiin)).errors should contain(
        FormError(fieldName, invalidCharactersKey)
      )
    }

    "not bind strings that do not match the GIIN format regex" in {
      val invalidFormatGiinActual = "ABCDEF.12345.XY.12A"
      form.bind(Map(fieldName -> invalidFormatGiinActual)).errors should contain(
        FormError(fieldName, invalidKey)
      )

      val invalidFormatGiin2 = "ABCDEF.12345.XO.123"
      form.bind(Map(fieldName -> invalidFormatGiin2)).errors should contain(
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
