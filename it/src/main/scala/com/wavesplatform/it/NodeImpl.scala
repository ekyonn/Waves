package com.wavesplatform.it

import com.typesafe.config.Config
import com.wavesplatform.it.api._
import com.wavesplatform.it.util.GlobalTimer
import com.wavesplatform.settings.WavesSettings
import org.asynchttpclient._
import scorex.transaction.TransactionParser.TransactionType

import scala.concurrent.duration.FiniteDuration

class NodeImpl(val config: Config, var nodeInfo: NodeInfo, override val client: AsyncHttpClient) extends Node {
  val privateKey: String = config.getString("private-key")
  val publicKey: String = config.getString("public-key")
  val address: String = config.getString("address")
  val accountSeed: String = config.getString("account-seed")
  val settings: WavesSettings = WavesSettings.fromConfig(config)

  val chainId: Char = 'I'
  def name: String = settings.networkSettings.nodeName
  val restAddress: String = "localhost"

  def restPort: Int = nodeInfo.hostRestApiPort

  def matcherRestPort: Int = nodeInfo.hostMatcherApiPort

  def networkPort: Int = nodeInfo.hostNetworkPort

  val blockDelay: FiniteDuration = settings.blockchainSettings.genesisSettings.averageBlockDelay

  def fee(txValue: TransactionType.Value, asset: String): Long =
    settings.feesSettings.fees(txValue.id).find(_.asset == asset).get.fee
}

object NodeImpl {
  def apply(config: Config): Node = new NodeImpl(
    config,
    NodeInfo(
      config.getInt("rest-api-port"),
      config.getInt("network-port"),
      config.getInt("network-port"),
      config.getString("hostname"),
      config.getString("hostname"),
      "",
      config.getInt("matcher-api-port")
    ),
    Dsl.asyncHttpClient(Dsl.config().setNettyTimer(GlobalTimer.instance))
  ) {
    override val restAddress: String = config.getString("hostname")
  }
}