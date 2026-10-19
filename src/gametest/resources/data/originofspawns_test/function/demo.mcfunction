# Sets up a look at some zombie genes. Run it as a player: /function originofspawns_test:demo
time set midnight
originofspawns pool set minecraft:zombie temperate originofspawns:death_throes 1
originofspawns pool set minecraft:zombie hot originofspawns:death_throes 1
originofspawns pool set minecraft:zombie cold originofspawns:death_throes 1
originofspawns pool set minecraft:zombie wet originofspawns:death_throes 1
# From left to right: Death Throes, Burrower, Burrower with Bonerunner, Lurker, and Shade.
execute at @s rotated ~ 0 positioned ^4 ^ ^4 run summon minecraft:zombie ~ ~ ~ {NoAI:1b,PersistenceRequired:1b,Tags:["oos_demo"],"fabric:attachments":{"originofspawns:genes":["originofspawns:death_throes"]}}
execute at @s rotated ~ 0 positioned ^2 ^ ^4 run summon minecraft:zombie ~ ~ ~ {NoAI:1b,PersistenceRequired:1b,Tags:["oos_demo"],"fabric:attachments":{"originofspawns:genes":["originofspawns:burrower"]}}
execute at @s rotated ~ 0 positioned ^0 ^ ^4 run summon minecraft:zombie ~ ~ ~ {NoAI:1b,PersistenceRequired:1b,Tags:["oos_demo"],"fabric:attachments":{"originofspawns:genes":["originofspawns:burrower","originofspawns:bonerunner"]}}
execute at @s rotated ~ 0 positioned ^-2 ^ ^4 run summon minecraft:zombie ~ ~ ~ {NoAI:1b,PersistenceRequired:1b,Tags:["oos_demo"],"fabric:attachments":{"originofspawns:genes":["originofspawns:lurker"]}}
execute at @s rotated ~ 0 positioned ^-4 ^ ^4 run summon minecraft:zombie ~ ~ ~ {NoAI:1b,PersistenceRequired:1b,Tags:["oos_demo"],"fabric:attachments":{"originofspawns:genes":["originofspawns:shade"]}}
execute as @e[tag=oos_demo] at @s run tp @s ~ ~ ~ facing entity @p
tag @e[tag=oos_demo] remove oos_demo
