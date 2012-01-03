CREATE TABLE `player_tracker` (
  `name` varchar(64) NOT NULL,
  `group` varchar(64) NOT NULL,
  `dimension` int(8) NOT NULL,
  `x` int(32) NOT NULL,
  `y` int(32) NOT NULL,
  `z` int(32) NOT NULL,
  `hidden` boolean NOT NULL
);
