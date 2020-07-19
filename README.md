# Game of Life 3D (Minecraft Mod)
[![Release Notes](https://img.shields.io/github/release/LolHens/mc-game-of-life-3d.svg?maxAge=3600)](https://github.com/LolHens/mc-game-of-life-3d/releases/latest)
[![Apache License 2.0](https://img.shields.io/github/license/LolHens/mc-game-of-life-3d.svg?maxAge=3600)](https://www.apache.org/licenses/LICENSE-2.0)

This mod implements John Conway's Game of Life and a 3D variation by Carter Bays (variation 5766).
You can find out more about the variation here: https://wpmedia.wolfram.com/uploads/sites/13/2018/02/01-3-1.pdf

## Features
- Cell blocks that follow the rules of the Game of Life by John Conway (2333)
- Cell blocks that follow the rules of a Game of Life variation by Carter Bays (5766)
- Cell support blocks that can connect together separate cell structures to help you in activating them all at once

## Usage
- Place your cell blocks down in the desired structure.
- The cell blocks are still gray which means that the cells are not active yet.
- If you have multiple structures which are not directly connected but need to be activated together you can connect them using the cell support blocks. These blocks will disappear once you activate the structure.
- Right click one of the cell blocks or cell support blocks.
- The whole connected structure activates and the simulation starts.

## Screenshots
![](https://raw.githubusercontent.com/LolHens/mc-game-of-life-3d/master/screenshots/2020-07-18_23.18.23.png)
![](https://raw.githubusercontent.com/LolHens/mc-game-of-life-3d/master/screenshots/2020-07-18%20232058.png)

## Videos
[![](https://img.youtube.com/vi/sQOsDWcU1sc/0.jpg)](https://www.youtube.com/watch?v=sQOsDWcU1sc)
[![](https://img.youtube.com/vi/5bM4YJ2GlI8/0.jpg)](https://www.youtube.com/watch?v=5bM4YJ2GlI8)

## Carter Bays' Game of Life 3D (5766)
The numbers 5766 define the game rules in this context:

- A cell requires a minimum of 5 neighbors to survive to the next generation.
- The cell can have at most 7 neighbors or else it dies of overpopulation.
- 66 means that a new cell is born if it has a minimum and maximum of 6 neighbors.

This is in contrast to the normal game rules 2333 by John Conway and allows the system to work properly in three dimensions. The lifeforms in 5766 should look very similar to the ones found in 2333. Another nice property of 5766 is that every 2333 structure works the same when it is 2 layers thick and sandwiched between two layers of other blocks. Here are a few examples of native 3D lifeforms:

[![](https://raw.githubusercontent.com/LolHens/mc-game-of-life-3d/master/screenshots/small_stable_life_forms_5766.png)](https://wpmedia.wolfram.com/uploads/sites/13/2018/02/01-3-1.pdf)

## License
This project uses the Apache 2.0 License. See the file called LICENSE.
