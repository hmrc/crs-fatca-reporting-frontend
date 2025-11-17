package controllers.fatca

import models.{FATCA, MessageSpecData}
import pages.ValidXMLPage
import utils.ISpecBehaviours

import java.time.LocalDate

class ElectFatcaThresholdsControllerISpec extends ISpecBehaviours {

  private val path = "/report/elections/fatca/thresholds"
  val fiNameFM = "testFIFromFM"
  val messageSpecData = MessageSpecData(FATCA, "testFI", "testRefId", "testReportingName", LocalDate.of(2000, 1, 1), giin = None, fiNameFM)
  val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, getValidatedFileData(messageSpecData))

  "GET ElectFatcaThresholdsController.onPageLoad" must {
    behave like pageLoads(path = path, pageTitle = "election.fatca.thresholds.title", userAnswers = userAnswers)
    behave like pageRedirectsWhenNotAuthorised(path)
  }

  "Post ElectFatcaThresholdsController.onSubmit" must {
    val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("true"))

    behave like standardOnSubmit(path, requestBody)
    behave like pageSubmits(path, "/report-for-crs-and-fatca/check-your-answers", userAnswers, requestBody)
  }
}
