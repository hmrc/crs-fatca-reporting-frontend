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

import forms.mappings.Mappings
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, Form, FormError, Mapping}
import play.api.data.Forms.of
import utils.RegexConstants

import javax.inject.Inject

class RequiredGiinFormProvider @Inject() extends Mappings with RegexConstants {

  private val giinExactLength = 19
  private val notRealGiinExample = "98O96B.00000.LE.350"

  def apply(): Form[String] =
    Form(
      "value" -> of(giinFormatter())
    )

  private def giinFormatter(): Formatter[String] =
    new Formatter[String] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
        data.get(key) match {
          case None | Some("") =>
            Left(Seq(FormError(key, "requiredGiin.error.required")))
          case Some(value) =>
            val strippedValue = value.replaceAll("\\s", "")

            if (strippedValue == notRealGiinExample.replaceAll("\\s", "")) {
              Left(Seq(FormError(key, "requiredGiin.error.notReal")))
            }
            else if (strippedValue.length != giinExactLength) {
              Left(Seq(FormError(key, "requiredGiin.error.length")))
            }
            else if (!strippedValue.matches("^[A-Za-z0-9.]*$")) {
              Left(Seq(FormError(key, "requiredGiin.error.invalidCharacters")))
            }
            else if (!strippedValue.matches(giinFormatRegex)) {
              Left(Seq(FormError(key, "requiredGiin.error.format")))
            }
            else {
              Right(strippedValue)
            }
        }
      }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    }
}
