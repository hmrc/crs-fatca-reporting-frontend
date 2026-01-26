package models.submission

import models.MessageSpecData
import models.upscan.{Reference, UploadId}
import play.api.libs.json.{Json, OFormat}

final case class SubmissionDetails(
                                    fileName: String,
                                    uploadId: UploadId,
                                    enrolmentId: String,
                                    fileSize: Long,
                                    documentUrl: String,
                                    checksum: String,
                                    messageSpecData: MessageSpecData,
                                    fileReference: Reference
                                  )

object SubmissionDetails {
  implicit val format: OFormat[SubmissionDetails] = Json.format[SubmissionDetails]
}