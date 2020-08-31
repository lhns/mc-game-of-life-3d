package de.lolhens.minecraft.gameoflife3d.game

case class GameRules(minEnvironment: Int,
                     maxEnvironment: Int,
                     minFertility: Int,
                     maxFertility: Int,
                     onlyHorizontal: Boolean = false) {
  def remainAlive(neighbors: Int): Boolean = neighbors >= minEnvironment && neighbors <= maxEnvironment

  def becomeAlive(neighbors: Int): Boolean = neighbors >= minFertility && neighbors <= maxFertility
}

object GameRules {
  val ConwaysRules: GameRules = GameRules(2, 3, 3, 3, onlyHorizontal = true)
  val ConwaysRules3d: GameRules = GameRules(2, 3, 3, 3)
  val BaysRules5766: GameRules = GameRules(5, 7, 6, 6)
  val Die: GameRules = GameRules(Int.MaxValue, Int.MaxValue, Int.MaxValue, Int.MaxValue)
  val Spread: GameRules = GameRules(Int.MinValue, Int.MaxValue, Int.MinValue, Int.MaxValue)
}
