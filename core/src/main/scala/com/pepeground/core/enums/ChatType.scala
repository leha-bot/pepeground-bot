package com.pepeground.core.enums

object ChatType {
  def apply(value: Int): String = value match {
    case 0 => "chat"
    case 1 => "faction"
    case 2 => "supergroup"
    case 3 => "channel"
    case _ => "chat"
  }

  def apply(value: String): Int = value match {
    case "chat" => 0
    case "faction" => 1
    case "supergroup" => 2
    case "channel" => 3
    case _ => 0
  }
}