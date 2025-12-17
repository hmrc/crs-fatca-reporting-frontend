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
import models.MessageSpecData.name
import models.UserAnswers.extractMessageSpecData
import models.{CRS, CheckMode, FATCA, MessageType, UserAnswers}
import pages.elections.crs.*
import pages.elections.fatca.{ElectFatcaThresholdsPage, TreasuryRegulationsPage}
import pages.{QuestionPage, ReportElectionsPage, RequiredGiinPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key
import utils.Extension.toYesNo
import utils.ReportingConstants.*
import viewmodels.govuk.all.ActionItemViewModel

class CheckYourFileDetailsViewModel(userAnswers: UserAnswers)(using messages: Messages):

  def fileDetailsSummary: SummaryList =
    extractMessageSpecData(userAnswers) {
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
              actionItem = Some(
                ActionItem(
                  href = routes.IndexController.onPageLoad().url,
                  content = Text(messages("checkYourFileDetails.fileInformation.change"))
                )
              )
            )
          )
        )
    }

  def financialInstitutionDetailsSummary: SummaryList = SummaryList(rows = requiredGIINRow.toSeq ++ reportElectionRow)

  private def reportElectionRow: Seq[SummaryListRow] =
    userAnswers.get(ReportElectionsPage) match
      case Some(reportElectionValue) =>
        extractMessageSpecData(userAnswers) {
          messageSpecData =>
            val reportingYear = messageSpecData.reportingPeriod.getYear.toString
            Seq(
              summaryListRowHelper(
                key = messages("reportElections.title", messageSpecData.messageType.name, reportingYear),
                value = reportElectionValue.toYesNo,
                actionItem = Some(
                  singleActionItemForChangeLink(
                    controllers.elections.routes.ReportElectionsController.onPageLoad(CheckMode).url,
                    messages("change.election.hidden.text", messageSpecData.messageType.name)
                  )
                )
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
                  singleActionItemForChangeLink(
                    routes.RequiredGiinController.onPageLoad(mode = CheckMode).url,
                    messages("change.giin.hidden.text")
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

  private def electCRSContractRow = summaryRowForBooleanPages(
    ElectCrsContractPage,
    messages("checkYourFileDetails.crs.contracts"),
    actionUrl = controllers.elections.crs.routes.ElectCrsContractController.onPageLoad(CheckMode).url,
    messages("change.crs.contract.hidden.text")
  )

  private def dormantAccountRow = summaryRowForBooleanPages(
    DormantAccountsPage,
    messages("checkYourFileDetails.crs.dormantAccounts"),
    actionUrl = controllers.elections.crs.routes.DormantAccountsController.onPageLoad(CheckMode).url,
    messages("change.crs.dormant.accounts.hidden.text")
  )

  private def thresholdsRow = summaryRowForBooleanPages(
    ThresholdsPage,
    messages("checkYourFileDetails.crs.threshold"),
    actionUrl = controllers.elections.crs.routes.ThresholdsController.onPageLoad(CheckMode).url,
    messages("change.crs.apply.threshold.hidden.text")
  )

  private def grossProceedRow(reportingPeriod: Int): Seq[SummaryListRow] =
    if reportingPeriod >= ThresholdDate.getYear then electCRSCarfGrossProceedRows else Seq.empty

  private def electCRSCarfGrossProceedRows: Seq[SummaryListRow] =
    extractMessageSpecData(userAnswers) {
      messageSpecData =>
        userAnswers
          .get(ElectCrsCarfGrossProceedsPage)
          .map {
            value =>
              Seq(
                summaryListRowHelper(
                  messages("checkYourFileDetails.crs.grossProceed"),
                  value.toYesNo,
                  actionItem = Some(
                    singleActionItemForChangeLink(
                      controllers.elections.crs.routes.ElectCrsCarfGrossProceedsController.onPageLoad(CheckMode).url,
                      messages("change.crs.reporting.gross.proceeds.cryptoasset.hidden.text", messageSpecData.reportingPeriod.getYear.toString)
                    )
                  )
                )
              ) ++ electCRSGrossProceedRows(value)
          }
          .getOrElse(Seq.empty)
    }

  private def electCRSGrossProceedRows(crsCarfGrossProceedValue: Boolean): Seq[SummaryListRow] =
    electCRSGrossProceedsRow
      .filter(
        _ => crsCarfGrossProceedValue
      )
      .toSeq

  private def electCRSGrossProceedsRow = summaryRowForBooleanPages(
    ElectCrsGrossProceedsPage,
    messages("checkYourFileDetails.crs.reportingGrossProceed"),
    actionUrl = controllers.elections.crs.routes.ElectCrsGrossProceedsController.onPageLoad(CheckMode).url,
    messages("change.crs.reporting.gross.proceeds.for.carf.hidden.text")
  )

  private def treasuryRegulationsRow = summaryRowForBooleanPages(
    TreasuryRegulationsPage,
    messages("checkYourFileDetails.fatca.treasuryRegulation"),
    actionUrl = controllers.elections.fatca.routes.TreasuryRegulationsController.onPageLoad(CheckMode).url,
    messages("change.fatca.treasuryRegulation.hidden.text")
  )

  private def electFatcaThresholdsRow = summaryRowForBooleanPages(
    ElectFatcaThresholdsPage,
    messages("checkYourFileDetails.fatca.threshold"),
    actionUrl = controllers.elections.fatca.routes.ElectFatcaThresholdsController.onPageLoad(CheckMode).url,
    messages("change.fatca.threshold.hidden.text")
  )

  private def summaryRowForBooleanPages(page: QuestionPage[Boolean], keyValue: String, actionUrl: String, hiddenText: String): Option[SummaryListRow] =
    userAnswers
      .get(page)
      .map(
        value => summaryListRowHelper(keyValue, value.toYesNo, actionItem = Some(singleActionItemForChangeLink(actionUrl, hiddenText)))
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

  private def singleActionItemForChangeLink(hrefUrl: String, hiddenText: String) =
    ActionItemViewModel(
      content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span><span class="govuk-visually-hidden">$hiddenText</span>"""),
      href = hrefUrl
    )
