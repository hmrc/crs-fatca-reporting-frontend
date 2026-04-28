/*
 * Copyright 2026 HM Revenue & Customs
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

package models.submission.fileDetails

import models.submission.*
import play.api.libs.json.*

sealed trait FileStatus

case object Pending extends FileStatus
case object Accepted extends FileStatus
case object RejectedSDES extends FileStatus
case object RejectedSDESVirus extends FileStatus
case object NotAccepted extends FileStatus
case object Rejected extends FileStatus

object FileStatus {

  given Format[FileStatus] = Format(
    Reads {
      case JsString("Pending")           => JsSuccess(Pending)
      case JsString("Accepted")          => JsSuccess(Accepted)
      case JsString("RejectedSDES")      => JsSuccess(RejectedSDES)
      case JsString("RejectedSDESVirus") => JsSuccess(RejectedSDESVirus)
      case JsString("NotAccepted")       => JsSuccess(NotAccepted)
      case JsString("Rejected")          => JsSuccess(Rejected)
      case other                         => JsError(s"Invalid FileStatus JSON: $other")
    },
    Writes {
      case Pending           => JsString("Pending")
      case Accepted          => JsString("Accepted")
      case RejectedSDES      => JsString("RejectedSDES")
      case RejectedSDESVirus => JsString("RejectedSDESVirus")
      case NotAccepted       => JsString("NotAccepted")
      case Rejected          => JsString("Rejected")
    }
  )
}
