# Sets up a look at Death Throes. Run it as a player: /function originofspawns_test:demo
time set midnight
originofspawns pool set minecraft:zombie temperate originofspawns:death_throes 1
originofspawns pool set minecraft:zombie hot originofspawns:death_throes 1
originofspawns pool set minecraft:zombie cold originofspawns:death_throes 1
originofspawns pool set minecraft:zombie wet originofspawns:death_throes 1
execute at @s positioned ^ ^ ^3 run summon minecraft:zombie ~ ~ ~ {NoAI:1b,PersistenceRequired:1b,Tags:["oos_demo"],"fabric:attachments":{"originofspawns:genes":["originofspawns:death_throes"]}}
execute as @e[tag=oos_demo] at @s run tp @s ~ ~ ~ facing entity @p
tag @e[tag=oos_demo] remove oos_demo
