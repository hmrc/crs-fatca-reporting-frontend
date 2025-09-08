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

import models.fileDetails.BusinessRuleErrorCode.{CorrDocRefIdUnknown, InvalidMessageRefIDFormat}
import models.fileDetails.{FileErrors, FileValidationErrors, RecordError}
import viewmodels.FileRejectedViewModel

trait RulesErrorHelper {

  def createFileRejectedViewModel() = {
    val fileErrors: Seq[FileErrors] = Seq(FileErrors(CorrDocRefIdUnknown, None))
    val recordErrors: Seq[RecordError] = Seq(
      RecordError(
        InvalidMessageRefIDFormat,
        Some("GB2026GB-FIID123456789-CRSReport2026001-ReportingFI-001"),
        Some(Seq("GB2026GB-FIID123456789-CRSReport2026001-ReportingFI-001"))
      )
    )
    val validationErrors = FileValidationErrors(
      fileError = Some(fileErrors),
      recordError = Some(recordErrors)
    )

    FileRejectedViewModel(validationErrors)
  }

}
