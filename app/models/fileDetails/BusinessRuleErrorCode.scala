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

package models.fileDetails

import play.api.libs.json.*

enum BusinessRuleErrorCode(val code: String):
  case InvalidMessageRefIDFormat extends BusinessRuleErrorCode("50008")
  case DocRefIDFormat extends BusinessRuleErrorCode("80001")
//  case CorrDocRefIdUnknown extends BusinessRuleErrorCode("80002")
  case FailedSchemaValidationFatca extends BusinessRuleErrorCode("Temp FATCA Error Code 2")

  // crs
  case FailedSchemaValidationCrs extends BusinessRuleErrorCode("CRS Error Code 2")
  case CRSFailedThreatScan extends BusinessRuleErrorCode("CRS Error Code 1")
  case CRSEmojis extends BusinessRuleErrorCode("CRS Error Code 3")
  case CRSEmptyStringMin1Elements extends BusinessRuleErrorCode("CRS Error Code 4")
  case CRSTestData extends BusinessRuleErrorCode("CRS Error Code 5")
  case CRSBody extends BusinessRuleErrorCode("CRS Error Code 6")
  case CRSMultipleNilReports extends BusinessRuleErrorCode("CRS Error Code 7")
  case CRSMessageTypeIndic_CRS701_Or_CRS702 extends BusinessRuleErrorCode("CRS Error Code 8")
  case CRSMultipleReportingFis extends BusinessRuleErrorCode("CRS Error Code 9")
  case CRSSendingCompanyIN extends BusinessRuleErrorCode("CRS Error Code 10")
  case CRSMessageRefIdFormatInvalid extends BusinessRuleErrorCode("CRS Error Code 11")
  case CRSDuplicateMessageRefId extends BusinessRuleErrorCode("CRS Error Code 12")
  case CRSFileNameMessageRefIdMismatch extends BusinessRuleErrorCode("CRS Error Code 13")
  case CRSMessageSpecContainsCorrMessageRefId extends BusinessRuleErrorCode("CRS Error Code 14")
  case CRSReportingPeriodFormat extends BusinessRuleErrorCode("CRS Error Code 15")
  case CRSReportingPeriodEarly extends BusinessRuleErrorCode("CRS Error Code 16")
  case CRSReportingPeriodLaterThanCurrentYear extends BusinessRuleErrorCode("CRS Error Code 17")
  case CRSReportingPeriodMustPreviousSentFile extends BusinessRuleErrorCode("CRS Error Code 18")
  case CRSReportingFIResCountryCode extends BusinessRuleErrorCode("CRS Error Code 19")
  case CRSRegimeIncorrect extends BusinessRuleErrorCode("CRS Error Code 20")
  case CRSRegimeIncorrectDeuToAcctHolderTypeAndAccountReport extends BusinessRuleErrorCode("CRS Error Code 21")
  case CRSInMissing extends BusinessRuleErrorCode("CRS Error Code 22")
  case CRSInIssueByAttrMissing extends BusinessRuleErrorCode("CRS Error Code 23")
  case CRSIndividualNameTypeInvalid extends BusinessRuleErrorCode("CRS Error Code 24")
  case CRSAddress_AddressFixInvalid extends BusinessRuleErrorCode("CRS Error Code 25")
  case CRSDeleteParentRecord extends BusinessRuleErrorCode("CRS Error Code 26")
  case CRSDocTypeIndicCombinationInvalid extends BusinessRuleErrorCode("CRS Error Code 27")
  case CRSResendOption extends BusinessRuleErrorCode("CRS Error Code 28")
  case CRSDuplicateDocRefIds extends BusinessRuleErrorCode("CRS Error Code 29")
  case CRSDocRefIDInvalidFormat extends BusinessRuleErrorCode("CRS Error Code 30")
  case CRSDocRefIDUnknown extends BusinessRuleErrorCode("CRS Error Code 31")
  case CRSDocRefInvalid extends BusinessRuleErrorCode("CRS Error Code 32")
  case CRSDocSpeCorrMessageRefId extends BusinessRuleErrorCode("CRS Error Code 33")
  case CRSCorrDocRefIdUnknown extends BusinessRuleErrorCode("CRS Error Code 34")
  case CRSOECD1CorrDocRefId extends BusinessRuleErrorCode("CRS Error Code 35")
  case CRSCorrDocRefIdMissing extends BusinessRuleErrorCode("CRS Error Code 36")
  case CRSDuplicateCorrDocRefIds extends BusinessRuleErrorCode("CRS Error Code 37")
  case CRSCorrDocRefIdInvalid extends BusinessRuleErrorCode("CRS Error Code 38")
  case CRSInvalidCorrDocRefId extends BusinessRuleErrorCode("CRS Error Code 39")
  case CRSCorrDocRefIdForOECD0 extends BusinessRuleErrorCode("CRS Error Code 40")
  case CRSMultipleReportingGroup extends BusinessRuleErrorCode("CRS Error Code 41")
  case CRSSponsor extends BusinessRuleErrorCode("CRS Error Code 42")
  case CRSIntermediary extends BusinessRuleErrorCode("CRS Error Code 43")
  case CRSAccountReport extends BusinessRuleErrorCode("CRS Error Code 44")
  case CRSClosedAccount extends BusinessRuleErrorCode("CRS Error Code 45")
  case CRSIBANFormatInvalid extends BusinessRuleErrorCode("CRS Error Code 46")
  case CRSISINFormatInvalid extends BusinessRuleErrorCode("CRS Error Code 47")
  case CRSSpecifiedElectronicMoneyProduct extends BusinessRuleErrorCode("CRS Error Code 48")
  case CRSIBAN extends BusinessRuleErrorCode("CRS Error Code 49")
  case CRSCashValueInsuranceContractOrAnnuityContract extends BusinessRuleErrorCode("CRS Error Code 50")
  case CRSEquityInterestType extends BusinessRuleErrorCode("CRS Error Code 51")
  case CRSAccountHolderSelfCert extends BusinessRuleErrorCode("CRS Error Code 52")
  case CRSTINMissing extends BusinessRuleErrorCode("CRS Error Code 53")
  case CRSTINIssuedByAttributeMissing extends BusinessRuleErrorCode("CRS Error Code 54")
  case CRSNationality extends BusinessRuleErrorCode("CRS Error Code 55")
  case CRSBirthDateRange extends BusinessRuleErrorCode("CRS Error Code 56")
  case CRSOrganisationResCountryCode extends BusinessRuleErrorCode("CRS Error Code 57")
  case CRSControllingPerson extends BusinessRuleErrorCode("CRS Error Code 58")
  case CRSControllingPersonRequired extends BusinessRuleErrorCode("CRS Error Code 59")
  case CRSControllingPersonIndividual extends BusinessRuleErrorCode("CRS Error Code 60")
  case CRSCtrlgPersonType extends BusinessRuleErrorCode("CRS Error Code 61")
  case CRSControllingPersonSelfCert extends BusinessRuleErrorCode("CRS Error Code 62")
  case CRSAccountBalance extends BusinessRuleErrorCode("CRS Error Code 63")
  case CRSDepositoryAccount extends BusinessRuleErrorCode("CRS Error Code 64")
  case CRSDebtOrEquityInterestinInvestmentEntity extends BusinessRuleErrorCode("CRS Error Code 65")
  case CRSCashValueInsuranceContractOrAnnuityContract2 extends BusinessRuleErrorCode("CRS Error Code 66")
  case CRSDDProcedure extends BusinessRuleErrorCode("CRS Error Code 67")
  case CRSAccountType extends BusinessRuleErrorCode("CRS Error Code 68")
  case CRSPoolReport extends BusinessRuleErrorCode("CRS Error Code 69")

  case UnknownErrorCode(override val code: String) extends BusinessRuleErrorCode(code)

object BusinessRuleErrorCode:

  val values: Seq[BusinessRuleErrorCode] = Seq(
    InvalidMessageRefIDFormat,
    DocRefIDFormat,
//    CorrDocRefIdUnknown,
    FailedSchemaValidationCrs,
    FailedSchemaValidationFatca,
    CRSFailedThreatScan,
    CRSEmojis,
    CRSEmptyStringMin1Elements,
    CRSTestData,
    CRSBody,
    CRSMultipleNilReports,
    CRSMessageTypeIndic_CRS701_Or_CRS702,
    CRSMultipleReportingFis,
    CRSSendingCompanyIN,
    CRSMessageRefIdFormatInvalid,
    CRSDuplicateMessageRefId,
    CRSFileNameMessageRefIdMismatch,
    CRSMessageSpecContainsCorrMessageRefId,
    CRSReportingPeriodFormat,
    CRSReportingPeriodEarly,
    CRSReportingPeriodLaterThanCurrentYear,
    CRSReportingPeriodMustPreviousSentFile,
    CRSReportingFIResCountryCode,
    CRSRegimeIncorrect,
    CRSRegimeIncorrectDeuToAcctHolderTypeAndAccountReport,
    CRSInMissing,
    CRSIndividualNameTypeInvalid,
    CRSAddress_AddressFixInvalid,
    CRSDeleteParentRecord,
    CRSDocTypeIndicCombinationInvalid,
    CRSResendOption,
    CRSDuplicateDocRefIds,
    CRSDocRefIDInvalidFormat,
    CRSDocRefIDUnknown,
    CRSDocSpeCorrMessageRefId,
    CRSCorrDocRefIdUnknown,
    CRSOECD1CorrDocRefId,
    CRSCorrDocRefIdMissing,
    CRSDuplicateCorrDocRefIds,
    CRSCorrDocRefIdInvalid,
    CRSInvalidCorrDocRefId,
    CRSCorrDocRefIdForOECD0,
    CRSMultipleReportingGroup,
    CRSSponsor,
    CRSIntermediary,
    CRSAccountReport,
    CRSClosedAccount,
    CRSIBANFormatInvalid,
    CRSISINFormatInvalid,
    CRSSpecifiedElectronicMoneyProduct,
    CRSIBAN,
    CRSCashValueInsuranceContractOrAnnuityContract,
    CRSEquityInterestType,
    CRSAccountHolderSelfCert,
    CRSTINMissing,
    CRSTINIssuedByAttributeMissing,
    CRSNationality,
    CRSBirthDateRange,
    CRSOrganisationResCountryCode,
    CRSControllingPerson,
    CRSControllingPersonRequired,
    CRSControllingPersonIndividual,
    CRSCtrlgPersonType,
    CRSControllingPersonSelfCert,
    CRSAccountBalance,
    CRSDepositoryAccount,
    CRSDebtOrEquityInterestinInvestmentEntity,
    CRSCashValueInsuranceContractOrAnnuityContract2,
    CRSDDProcedure,
    CRSAccountType,
    CRSPoolReport
  )

  private val lookup: Map[String, BusinessRuleErrorCode] =
    values
      .map(
        value => value.code -> value
      )
      .toMap

  implicit val format: Format[BusinessRuleErrorCode] = new Format[BusinessRuleErrorCode] {
    def reads(json: JsValue): JsResult[BusinessRuleErrorCode] =
      json
        .validate[String]
        .map(
          code => lookup.getOrElse(code, UnknownErrorCode(code))
        )

    def writes(x: BusinessRuleErrorCode): JsValue =
      JsString(x.code)
  }
