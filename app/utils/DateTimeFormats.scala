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

package utils

import play.api.i18n.Lang

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeFormats {

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mma")

  private val localisedDateTimeFormatters = Map(
    "en" -> dateTimeFormatter,
    "cy" -> dateTimeFormatter.withLocale(new Locale("cy"))
  )

  def dateTimeFormat()(implicit lang: Lang): DateTimeFormatter =
    localisedDateTimeFormatters.getOrElse(lang.code, dateTimeFormatter)

  val dateTimeHintFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d M yyyy")

  def dateFormatterForFileConfirmation(): DateTimeFormatter = dateTimeFormatter
  def formatTimeForFileConfirmation(localDateTime: LocalDateTime): String = {
    val result = localDateTime.format(timeFormatter).toLowerCase()
    if(result.equalsIgnoreCase("12:00am")){
      "midnight"
    }else if (result.equalsIgnoreCase("12:00pm")){
      "midday"
    }else{
      result
    }
  }
}
