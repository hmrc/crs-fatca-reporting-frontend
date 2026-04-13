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

package viewmodels

import base.SpecBase
import models.fileDetails.*

class FileRejectedViewModelSpec extends SpecBase {

  val fileError   = FileErrors(BusinessRuleErrorCode.FailedSchemaValidationCrs, None)
  val recordError = RecordError(BusinessRuleErrorCode.CRSMultipleNilReports, None, Some(Seq("docA")))

  "getErrors" - {
    "must return file errors as FileRejectedError" in {
      val validationErrors = FileValidationErrors(Some(Seq(fileError)), None)
      val viewModel        = FileRejectedViewModel(validationErrors)
      viewModel.getErrors mustBe Seq(FileRejectedError("CRS Error Code 2", Nil))
    }

    "must return record errors as FileRejectedError with docRefIds" in {
      val recordError      = RecordError(BusinessRuleErrorCode.DocRefIDFormat, Some("details"), Some(Seq("doc1", "doc2")))
      val validationErrors = FileValidationErrors(None, Some(Seq(recordError)))
      val viewModel        = FileRejectedViewModel(validationErrors)
      viewModel.getErrors mustBe Seq(FileRejectedError("80001", Seq("doc1", "doc2")))
    }

    "must return both file and record errors as FileRejectedError" in {
      val validationErrors = FileValidationErrors(Some(Seq(fileError)), Some(Seq(recordError)))
      val viewModel        = FileRejectedViewModel(validationErrors)
      viewModel.getErrors must contain theSameElementsAs Seq(
        FileRejectedError("CRS Error Code 2", Nil),
        FileRejectedError("CRS Error Code 7", Seq("docA"))
      )
    }

    "must return empty Seq when there are no errors" in {
      val validationErrors = FileValidationErrors(None, None)
      val viewModel        = FileRejectedViewModel(validationErrors)
      viewModel.getErrors mustBe empty
    }
  }
}
