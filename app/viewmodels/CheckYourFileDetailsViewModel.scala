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
import models.{CRS, FATCA, UserAnswers, name}
import pages.ReportElectionsPage
import pages.elections.crs.*
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

  def getFIDetailsRows: SummaryList =
    userAnswers.get(ReportElectionsPage) match
      case Some(value) =>
        getMessageSpecData(userAnswers) {
          messageSpecData =>
            val reportingYear = messageSpecData.reportingPeriod.getYear.toString
            messageSpecData.messageType match
              case CRS => SummaryList(rows = Seq(getReportElectionsPage(CRS.name, reportingYear)) ++ getCRSElectionData(value, reportingYear))
              case FATCA => SummaryList(rows = Seq(getReportElectionsPage(CRS.name, reportingYear)) ++ getCRSElectionData(value, reportingYear))
        }
      case None => SummaryList(rows = Seq.empty)


  private def getCRSElectionData(requireElection: Boolean, reportingYear: String): Seq[SummaryListRow] =
    if requireElection then Seq(getCRSContracts,
      getCRSDormants, getCRSThreshold) ++ getGrossProceedPages(reportingYear.toInt) else Seq.empty


  private def getReportElectionsPage(regime: String, reportingYear: String) =
    userAnswers.get(ReportElectionsPage) match
      case Some(value) => SummaryListRow(
      key = Key(content = Text(messages("reportElections.title", regime, reportingYear))),
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
      case None => throw new IllegalStateException("ReportElectionsPage is missing")

  private def getCRSContracts =
    userAnswers.get(ElectCrsContractPage) match
      case Some(value) => SummaryListRow(
      key = Key(content = Text(messages("checkYourFileDetails.crs.contracts"))),
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
      case None => throw new IllegalStateException("ElectCrsContractPage is missing")

  private def getCRSDormants =
    userAnswers.get(DormantAccountsPage) match
      case Some(value) => SummaryListRow(
      key = Key(content = Text(messages("checkYourFileDetails.crs.dormantAccounts"))),
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
      case None => throw new IllegalStateException("DormantAccountsPage is missing")

  private def getCRSThreshold =
    userAnswers.get(ThresholdsPage) match
      case Some(value) => SummaryListRow(
      key = Key(content = Text(messages("checkYourFileDetails.crs.threshold"))),
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
      case None => throw new IllegalStateException("ThresholdsPage is missing")

  private def getGrossProceedPages(reportingPeriod: Int) : Seq[SummaryListRow] =
    if reportingPeriod >= thresholdDate.getYear then getCRSCarfGrossProceed else Seq.empty

  private def getCRSCarfGrossProceed: Seq[SummaryListRow] =
    userAnswers.get(ElectCrsCarfGrossProceedsPage) match
      case Some(value) =>
        Seq(SummaryListRow(
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
        )) ++ getCRSGrossProceed(value)
      case None => throw new IllegalStateException("ElectCrsCarfGrossProceedsPage is missing")

  private def getCRSGrossProceed(crsCarfGrossProceedValue: Boolean) =
    if crsCarfGrossProceedValue then Seq(getCRSGrossProceedValue) else Seq.empty

  private def getCRSGrossProceedValue =
    userAnswers.get(ElectCrsGrossProceedsPage) match
      case Some(value) => SummaryListRow(
        key = Key(content = Text(messages("checkYourFileDetails.crs.reportingGrossProceed"))),
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
      case None => throw new IllegalStateException("ElectCrsGrossProceedsPage is missing")