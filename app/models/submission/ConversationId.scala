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

package models.submission

import play.api.libs.json.{JsString, Reads, Writes}
import play.api.mvc.PathBindable

opaque type ConversationId = String

object ConversationId {

  def apply(value: String): ConversationId = value

  extension (id: ConversationId) def value: String = id

  given Writes[ConversationId] = conversationId => JsString(conversationId.value)

  given Reads[ConversationId] = Reads.StringReads.map(ConversationId.apply)

  given pathBindable: PathBindable[ConversationId] = new PathBindable[ConversationId] {
    override def bind(key: String, value: String): Either[String, ConversationId] =
      implicitly[PathBindable[String]].bind(key, value).map(ConversationId(_))

    override def unbind(key: String, value: ConversationId): String =
      value.value
  }
}
