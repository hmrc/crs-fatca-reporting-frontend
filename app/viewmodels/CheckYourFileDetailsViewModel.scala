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
import utils.thresholdDate

class CheckYourFileDetailsViewModel(userAnswers: UserAnswers)(using messages: Messages):

  extension (b: Boolean)

    private def toYesNo: String =
      if b then "Yes" else "No"

  def getYourFileDetailsRows: SummaryList =
    getMessageSpecData(userAnswers) {
      messageSpecData =>
        SummaryList(
          rows = Seq(
            SummaryListRow(
              key = Key(content = Text(messages("checkYourFileDetails.fileId.key"))),
              value = Value(content = Text(messageSpecData.messageRefId)),
              classes = "no-border-bottom"
            ),
            SummaryListRow(
              key = Key(content = Text(messages("checkYourFileDetails.reportingRegime.key"))),
              value = Value(content = Text(messageSpecData.messageType.name)),
              classes = "no-border-bottom"
            ),
            SummaryListRow(
              key = Key(content = Text(messages("checkYourFileDetails.fiId.key"))),
              value = Value(content = Text(messageSpecData.sendingCompanyIN)),
              classes = "no-border-bottom"
            ),
            SummaryListRow(
              key = Key(content = Text(messages("checkYourFileDetails.financialInstitution.key"))),
              value = Value(content = Text(messageSpecData.reportingFIName)),
              classes = "no-border-bottom"
            ),
            SummaryListRow(
              key = Key(content = Text(messages("checkYourFileDetails.row5"))),
              value = Value(content = Text("New information")),
              actions = Some(
                Actions(
                  items = Seq(
                    ActionItem(
                      href = routes.IndexController.onPageLoad().url,
                      content = Text(messages("checkYourFileDetails.fileInformation.change")),
                      visuallyHiddenText = Some(messages("checkYourFileDetails.fileInformation.change"))
                    )
                  )
                )
              )
            )
          )
        )
    }

  def getFIDetailsRows: SummaryList = SummaryList(rows = getGIIN.toSeq ++ reportElection)

  private def reportElection: Seq[SummaryListRow] =
    userAnswers
      .get(ReportElectionsPage)
      .map(
        value =>
          getMessageSpecData(userAnswers) {
            messageSpecData =>
              val reportingYear = messageSpecData.reportingPeriod.getYear.toString
              Seq(
                SummaryListRow(
                  key = Key(content = Text(messages("reportElections.title", messageSpecData.messageType.name, reportingYear))),
                  value = Value(content = Text(value.toYesNo)),
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
              ) ++ getMessageTypeData(value, messageSpecData.messageType, reportingYear)
          }
      )
      .getOrElse(Seq.empty)

  private def getMessageTypeData(requireData: Boolean, messageType: MessageType, reportingYear: String) = if requireData then
    messageType match
      case CRS   => getCRSElectionData(reportingYear)
      case FATCA => getFATCAElectionData
  else Seq.empty

  private def getGIIN: Option[SummaryListRow] =
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

  private def getCRSElectionData(reportingYear: String): Seq[SummaryListRow] =
    Seq(getCRSContracts, getCRSDormants, getCRSThreshold) ++ getGrossProceedPages(reportingYear.toInt)

  private def getFATCAElectionData: Seq[SummaryListRow] = Seq(getFATCAUSTreasuryRegulation, getFATCAThreshold)

  private def getCRSContracts = getSummaryRowForBooleanPage(ElectCrsContractPage, messages("checkYourFileDetails.crs.contracts"))

  private def getCRSDormants = getSummaryRowForBooleanPage(DormantAccountsPage, messages("checkYourFileDetails.crs.dormantAccounts"))

  private def getCRSThreshold = getSummaryRowForBooleanPage(ThresholdsPage, messages("checkYourFileDetails.crs.threshold"))

  private def getGrossProceedPages(reportingPeriod: Int): Seq[SummaryListRow] =
    if reportingPeriod >= thresholdDate.getYear then getCRSCarfGrossProceed else Seq.empty

  private def getCRSCarfGrossProceed: Seq[SummaryListRow] =
    userAnswers
      .get(ElectCrsCarfGrossProceedsPage)
      .fold(throw new IllegalStateException("ElectCrsCarfGrossProceedsPage is missing")) {
        value =>
          Seq(
            SummaryListRow(
              key = Key(content = Text(messages("checkYourFileDetails.crs.grossProceed"))),
              value = Value(content = Text(value.toYesNo)),
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
          ) ++ getCRSGrossProceed(value)
      }

  private def getCRSGrossProceed(crsCarfGrossProceedValue: Boolean): Seq[SummaryListRow] =
    if crsCarfGrossProceedValue then Seq(getCRSGrossProceedValue) else Seq.empty

  private def getCRSGrossProceedValue = getSummaryRowForBooleanPage(ElectCrsGrossProceedsPage, messages("checkYourFileDetails.crs.reportingGrossProceed"))

  private def getFATCAUSTreasuryRegulation = getSummaryRowForBooleanPage(TreasuryRegulationsPage, messages("checkYourFileDetails.fatca.treasuryRegulation"))

  private def getFATCAThreshold = getSummaryRowForBooleanPage(ElectFatcaThresholdsPage, messages("checkYourFileDetails.fatca.threshold"))

  private def getSummaryRowForBooleanPage(page: QuestionPage[Boolean], keyValue: String): SummaryListRow =
    userAnswers
      .get(page)
      .map(
        value =>
          SummaryListRow(
            key = Key(content = Text(keyValue)),
            value = Value(content = Text(value.toYesNo)),
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
      .getOrElse(throw new IllegalStateException(s"$page is missing"))
