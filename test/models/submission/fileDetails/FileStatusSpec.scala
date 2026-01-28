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

import base.SpecBase
import play.api.libs.json.Json
import models.fileDetails.FileValidationErrors

class FileStatusSpec extends SpecBase {

  "FileStatus" - {

    "must parse json to expected FileStatus" in {
      Json.parse("{" + "\"Pending\": {}" + "}").as[FileStatus] mustBe Pending
      Json.parse("{" + "\"Accepted\": {}" + "}").as[FileStatus] mustBe Accepted
      Json.parse("{" + "\"RejectedSDES\": {}" + "}").as[FileStatus] mustBe RejectedSDES
      Json.parse("{" + "\"RejectedSDESVirus\": {}" + "}").as[FileStatus] mustBe RejectedSDESVirus

      val errors       = FileValidationErrors(None, None)
      val rejectedJson = Json.obj("Rejected" -> Json.obj("error" -> Json.toJson(errors)))
      rejectedJson.as[FileStatus] mustBe Rejected(errors)
    }

    "must write json as expected" in {
      Json.toJson(Pending: FileStatus).toString must include("\"Pending\"")
      Json.toJson(Accepted: FileStatus).toString must include("\"Accepted\"")
      Json.toJson(RejectedSDES: FileStatus).toString must include("\"RejectedSDES\"")
      Json.toJson(RejectedSDESVirus: FileStatus).toString must include("\"RejectedSDESVirus\"")

      val errors = FileValidationErrors(None, None)
      Json.toJson(Rejected(errors): FileStatus).toString must include("\"Rejected\"")
    }

    "Rejected.toString must be 'Rejected'" in {
      Rejected(FileValidationErrors(None, None)).toString mustBe "Rejected"
    }
  }
}
