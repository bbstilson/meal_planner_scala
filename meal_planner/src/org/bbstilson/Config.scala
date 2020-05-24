package org.bbstilson

case class StorageConfig(bucket: String, key: String)

case class SesConfig(recipients: List[String])

case class TrelloConfig(listId: String)

case class Config(trello: TrelloConfig, storage: StorageConfig, ses: SesConfig)
