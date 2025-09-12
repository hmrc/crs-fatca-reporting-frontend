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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Lang
import utils.DateTimeFormats.{dateFormatterForFileConfirmation, dateTimeFormat, formatTimeForFileConfirmation}

import java.time.{LocalDate, LocalDateTime}

class DateTimeFormatsSpec extends AnyFreeSpec with Matchers {

  ".dateTimeFormatUtil" - {

    "dateTimeFormat" - {

      "must format dates in English" in {
        val formatter = dateTimeFormat()(Lang("en"))
        val result    = LocalDate.of(2023, 1, 1).format(formatter)
        result mustEqual "1 January 2023"
      }

      "must format dates in Welsh" in {
        val formatter = dateTimeFormat()(Lang("cy"))
        val result    = LocalDate.of(2023, 1, 1).format(formatter)
        result mustEqual "1 Ionawr 2023"
      }

      "must default to English format" in {
        val formatter = dateTimeFormat()(Lang("de"))
        val result    = LocalDate.of(2023, 1, 1).format(formatter)
        result mustEqual "1 January 2023"
      }
    }
    "dateFormatterForFileConfirmation" - {

      "must format dates" in {
        val formatter = dateFormatterForFileConfirmation()
        val result = LocalDate.of(2023, 1, 1).format(formatter)
        result mustEqual "1 January 2023"
      }

    }
    "formatTimeForFileConfirmation" - {

      "must format time as midnight if its 12:00 am" in {
        val result = formatTimeForFileConfirmation(LocalDateTime.parse("2025-09-12T00:00:00"))
        result mustEqual "midnight"
      }

      "must format time as midnight if its 12:00 pm" in {
        val result = formatTimeForFileConfirmation(LocalDateTime.parse("2025-09-12T12:00:00"))
        result mustEqual "midday"
      }

      "must format time if its 1:00 am" in {
        val result = formatTimeForFileConfirmation(LocalDateTime.parse("2025-09-12T01:00:00"))
        result mustEqual "1:00am"
      }

      "must format time if its 1:00 pm" in {
        val result = formatTimeForFileConfirmation(LocalDateTime.parse("2025-09-12T13:00:00"))
        result mustEqual "1:00pm"
      }
    }

  }
}
