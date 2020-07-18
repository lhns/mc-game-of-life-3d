package de.lolhens.gameoflife3d

case class GameOfLifeRules(minEnvironment: Int,
                           maxEnvironment: Int,
                           minFertility: Int,
                           maxFertility: Int,
                           onlyHorizontal: Boolean = false) {
  def remainAlive(neighbors: Int): Boolean = neighbors >= minEnvironment && neighbors <= maxEnvironment

  def becomeAlive(neighbors: Int): Boolean = neighbors >= minFertility && neighbors <= maxFertility
}

object GameOfLifeRules {
  val ConwaysRules: GameOfLifeRules = GameOfLifeRules(2, 3, 3, 3, onlyHorizontal = true)
  val ConwaysRules3d: GameOfLifeRules = GameOfLifeRules(2, 3, 3, 3)
  val BaysRules5766: GameOfLifeRules = GameOfLifeRules(5, 7, 6, 6)
  val Die: GameOfLifeRules = GameOfLifeRules(Int.MaxValue, Int.MaxValue, Int.MaxValue, Int.MaxValue)
  val Spread: GameOfLifeRules = GameOfLifeRules(Int.MinValue, Int.MaxValue, Int.MinValue, Int.MaxValue)
}
