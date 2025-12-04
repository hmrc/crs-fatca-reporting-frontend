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

package viewmodels

import controllers.routes
import models.UserAnswers.getMessageSpecData
import models.{name, CRS, FATCA, MessageType, UserAnswers}
import pages.elections.crs.*
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}
import pages.{QuestionPage, ReportElectionsPage, RequiredGiinPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key
import utils.ReportingConstants.*

class CheckYourFileDetailsViewModel(userAnswers: UserAnswers)(using messages: Messages):

  def fileDetailsSummary: SummaryList =
    getMessageSpecData(userAnswers) {
      messageSpecData =>
        SummaryList(
          rows = Seq(
            summaryListRowHelper(messages("checkYourFileDetails.fileId.key"), messageSpecData.messageRefId, rowClasses = Some("no-border-bottom")),
            summaryListRowHelper(messages("checkYourFileDetails.reportingRegime.key"), messageSpecData.messageType.name, rowClasses = Some("no-border-bottom")),
            summaryListRowHelper(messages("checkYourFileDetails.fiId.key"), messageSpecData.sendingCompanyIN, rowClasses = Some("no-border-bottom")),
            summaryListRowHelper(messages("checkYourFileDetails.financialInstitution.key"),
                                 messageSpecData.reportingFIName,
                                 rowClasses = Some("no-border-bottom")
            ),
            summaryListRowHelper(
              messages("checkYourFileDetails.fileInformation.key"),
              messages("checkYourFileDetails.fileInformation.value"),
              actionItem = Some(singleActionItemForChangeLink(messages("checkYourFileDetails.fileInformation.change"), routes.IndexController.onPageLoad().url))
            )
          )
        )
    }

  def financialInstitutionDetailsSummary: SummaryList = SummaryList(rows = requiredGIINRow.toSeq ++ reportElectionRow)

  private def reportElectionRow: Seq[SummaryListRow] =
    userAnswers.get(ReportElectionsPage) match
      case Some(reportElectionValue) =>
        getMessageSpecData(userAnswers) {
          messageSpecData =>
            val reportingYear = messageSpecData.reportingPeriod.getYear.toString
            Seq(
              summaryListRowHelper(
                key = messages("reportElections.title", messageSpecData.messageType.name, reportingYear),
                value = reportElectionValue.toYesNo,
                actionItem = Some(singleActionItemForChangeLink(messages("site.change"), routes.IndexController.onPageLoad().url))
              )
            ) ++ messageTypeSpecificRows(reportElectionValue, messageSpecData.messageType, reportingYear)
        }
      case None => Seq.empty

  private def messageTypeSpecificRows(requireData: Boolean, messageType: MessageType, reportingYear: String) = if requireData then
    messageType match
      case CRS   => electionCRSRows(reportingYear)
      case FATCA => electionFATCARows
  else Seq.empty

  private def requiredGIINRow: Option[SummaryListRow] =
    userAnswers
      .get(RequiredGiinPage)
      .map(
        value =>
          SummaryListRow(
            key = Key(content = Text(messages("checkYourFileDetails.fatca.requireGIIN"))),
            value = Value(content = Text(value)),
            actions = Some(
              Actions(
                items = Seq(
                  ActionItem(
                    href = routes.IndexController.onPageLoad().url,
                    content = Text(messages("site.change")),
                    visuallyHiddenText = Some(messages("site.change"))
                  )
                )
              )
            )
          )
      )

  private def electionCRSRows(reportingYear: String): Seq[SummaryListRow] =
    Seq(
      electCRSContractRow,
      dormantAccountRow,
      thresholdsRow
    ).flatten ++ grossProceedRow(reportingYear.toInt)

  private def electionFATCARows: Seq[SummaryListRow] = Seq(treasuryRegulationsRow, electFatcaThresholdsRow).flatten

  private def electCRSContractRow = summaryRowForBooleanPages(ElectCrsContractPage, messages("checkYourFileDetails.crs.contracts"))

  private def dormantAccountRow = summaryRowForBooleanPages(DormantAccountsPage, messages("checkYourFileDetails.crs.dormantAccounts"))

  private def thresholdsRow = summaryRowForBooleanPages(ThresholdsPage, messages("checkYourFileDetails.crs.threshold"))

  private def grossProceedRow(reportingPeriod: Int): Seq[SummaryListRow] =
    if reportingPeriod >= ThresholdDate.getYear then electCRSCarfGrossProceedRows else Seq.empty

  private def electCRSCarfGrossProceedRows: Seq[SummaryListRow] =
    userAnswers
      .get(ElectCrsCarfGrossProceedsPage)
      .map {
        value =>
          Seq(
            summaryListRowHelper(
              messages("checkYourFileDetails.crs.grossProceed"),
              value.toYesNo,
              actionItem = Some(singleActionItemForChangeLink(messages("site.change"), routes.IndexController.onPageLoad().url))
            )
          ) ++ electCRSGrossProceedRows(value)
      }
      .getOrElse(Seq.empty)

  private def electCRSGrossProceedRows(crsCarfGrossProceedValue: Boolean): Seq[SummaryListRow] =
    electCRSGrossProceedsRow
      .filter(
        _ => crsCarfGrossProceedValue
      )
      .toSeq

  private def electCRSGrossProceedsRow = summaryRowForBooleanPages(ElectCrsGrossProceedsPage, messages("checkYourFileDetails.crs.reportingGrossProceed"))

  private def treasuryRegulationsRow = summaryRowForBooleanPages(TreasuryRegulationsPage, messages("checkYourFileDetails.fatca.treasuryRegulation"))

  private def electFatcaThresholdsRow = summaryRowForBooleanPages(ElectFatcaThresholdsPage, messages("checkYourFileDetails.fatca.threshold"))

  private def summaryRowForBooleanPages(page: QuestionPage[Boolean], keyValue: String): Option[SummaryListRow] =
    userAnswers
      .get(page)
      .map(
        value =>
          summaryListRowHelper(keyValue,
                               value.toYesNo,
                               actionItem = Some(singleActionItemForChangeLink(messages("site.change"), routes.IndexController.onPageLoad().url))
          )
      )

  private def summaryListRowHelper(key: String, value: String, rowClasses: Option[String] = None, actionItem: Option[ActionItem] = None) =
    SummaryListRow(
      key = Key(content = Text(key)),
      value = Value(content = Text(value)),
      classes = rowClasses.getOrElse(""),
      actions = actionItem.map(
        action =>
          Actions(
            items = Seq(action)
          )
      )
    )

  private def singleActionItemForChangeLink(changeLink: String, hrefUrl: String) =
    ActionItem(
      href = hrefUrl,
      content = Text(changeLink),
      visuallyHiddenText = Some(changeLink)
    )

extension (b: Boolean)

  private def toYesNo: String =
    if b then "Yes" else "No"
