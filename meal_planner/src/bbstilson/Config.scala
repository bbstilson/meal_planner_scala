package bbstilson

import pureconfig.*
import pureconfig.generic.derivation.default.*

case class StorageConfig(bucket: String, key: String) derives ConfigReader

case class SesConfig(recipients: List[String]) derives ConfigReader

case class TrelloConfig(listId: String) derives ConfigReader

case class Config(trello: TrelloConfig, storage: StorageConfig, ses: SesConfig) derives ConfigReader
