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

package models

import play.api.libs.json.*

sealed trait ReportType

enum CRSReportType extends ReportType:
  case TestData
  case NilReport
  case NewInformation
  case AdditionalInformationForExistingReport
  case DeletionOfExistingReport
  case CorrectedInformationForExistingReport
  case DeletedInformationForExistingReport
  case CorrectedAndDeletedInformationForExistingReport

object CRSReportType:

  private val fromJson: Map[String, CRSReportType] = Map(
    "TEST_DATA"                                             -> TestData,
    "NIL_REPORT"                                            -> NilReport,
    "NEW_INFORMATION"                                       -> NewInformation,
    "ADDITIONAL_INFORMATION_FOR_EXISTING_REPORT"            -> AdditionalInformationForExistingReport,
    "DELETION_OF_EXISTING_REPORT"                           -> DeletionOfExistingReport,
    "CORRECTED_INFORMATION_FOR_EXISTING_REPORT"             -> CorrectedInformationForExistingReport,
    "DELETED_INFORMATION_FOR_EXISTING_REPORT"               -> DeletedInformationForExistingReport,
    "CORRECTED_AND_DELETED_INFORMATION_FOR_EXISTING_REPORT" -> CorrectedAndDeletedInformationForExistingReport
  )

  private val toJson: Map[CRSReportType, String] = fromJson.map(_.swap)

  given Format[CRSReportType] = new Format[CRSReportType] {

    override def reads(json: JsValue): JsResult[CRSReportType] =
      json match {
        case JsString(value) =>
          fromJson
            .get(value)
            .map(JsSuccess(_))
            .getOrElse(JsError(s"Invalid CRSReportType value: $value"))
        case _ =>
          JsError("CRSReportType must be a string")
      }

    override def writes(value: CRSReportType): JsValue =
      JsString(toJson(value))
  }

enum FATCAReportType extends ReportType:
  case TestData

object FATCAReportType:

  private val fromJson: Map[String, FATCAReportType] = Map(
    "TEST_DATA" -> TestData
  )

  private val toJson: Map[FATCAReportType, String] = fromJson.map(_.swap)

  given Format[FATCAReportType] = new Format[FATCAReportType] {

    override def reads(json: JsValue): JsResult[FATCAReportType] =
      json match {
        case JsString(value) =>
          fromJson
            .get(value)
            .map(JsSuccess(_))
            .getOrElse(JsError(s"Invalid FATCAReportType value: $value"))
        case _ =>
          JsError("FATCAReportType must be a string")
      }

    override def writes(value: FATCAReportType): JsValue =
      JsString(toJson(value))
  }
