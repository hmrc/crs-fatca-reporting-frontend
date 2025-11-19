package controllers.elections.crs

import models.{CRS, MessageSpecData}
import pages.ValidXMLPage
import utils.ISpecBehaviours

import java.time.LocalDate

class ElectCrsGrossProceedsControllerISpec extends ISpecBehaviours {

  private val path = "/report/elections/fatca/us-treasury-regulations"
  val fiNameFM = "testFIFromFM"
  val messageSpecData = MessageSpecData(CRS, "testFI", "testRefId", "testReportingName", LocalDate.of(2000, 1, 1), giin = None, fiNameFM)
  val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(messageSpecData))

  "GET TreasuryRegulationsController.onPageLoad" must {
    behave like pageLoads(path = path, pageTitle = "treasuryRegulations.title", userAnswers = userAnswers)
    behave like pageRedirectsWhenNotAuthorised(path)
  }

  "Post TreasuryRegulationsController.onSubmit" must {
    val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("true"))

    behave like standardOnSubmit(path, requestBody)
    behave like pageSubmits(path, "/report-for-crs-and-fatca/report/elections/fatca/thresholds", userAnswers, requestBody)
  }

}
