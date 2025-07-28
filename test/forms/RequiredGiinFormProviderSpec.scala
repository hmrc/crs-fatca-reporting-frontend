package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class RequiredGiinFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "requiredGiin.error.required"
  val lengthKey = "requiredGiin.error.length"
  val maxLength = 119

  val form = new RequiredGiinFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
