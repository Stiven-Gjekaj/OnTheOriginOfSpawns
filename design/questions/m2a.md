# Questions for milestone 2a

This file lists the open decisions for milestone 2a, the complete zombie.
Each question has a recommendation.
Answer with the number and "yes" to take the recommendation, or give a
different answer.
When a question has an answer, the answer goes into
[DESIGN.md](../DESIGN.md) or [species/zombie.md](../species/zombie.md), and
the question leaves this file.

## Gene mechanics

1. **Frenzy and Thickskull change the attack speed.**
   The attack speed attribute does nothing for mobs.
   A zombie attacks on a fixed timer of 1 second.
   Recommendation: Frenzy makes the timer 30% shorter, and Thickskull makes it
   15% longer.

2. **Lunger leaps at a player in 4 blocks.**
   Recommendation: it leaps when the player is 3 to 5 blocks away, with a
   5 second wait between leaps.
   A leap that touches the player does a normal hit.
   After a missed leap, the zombie cannot move for 1 second.

3. **Bloodscent sees invisible players in 8 blocks.**
   Recommendation: it can target an invisible player in 8 blocks.
   Its double follow range applies only to players under half health.

4. **Rabid sometimes attacks other mobs.**
   Recommendation: each 10 seconds, it has a 10% chance to target the nearest
   mob in 8 blocks that is not a zombie, for example a cow or a skeleton.

5. **Death Throes damages players in 2 blocks when it dies.**
   Recommendation: 4 damage to players only, not to mobs or blocks, with a
   small burst of particles.

6. **Hush has no idle sounds and no hurt sounds.**
   Recommendation: it keeps its death sound, so that a kill still feels
   complete.

7. **Shade cannot be seen from more than 12 blocks at night.**
   Recommendation: the client does not draw it at more than 12 blocks when its
   light level is 7 or less.
   It is not the invisibility effect, so other mobs still see it.

8. **Burrower comes up from the ground near a player.**
   Recommendation: when a Burrower zombie spawns at night in 24 blocks of a
   player, it starts 2 blocks under the surface and rises in 2 seconds, with
   dirt particles.
   It moves through the blocks, and breaks none.
   It rises only through dirt, grass, sand, and gravel.

9. **Lurker waits in the dark.**
   Recommendation: at light level 7 or less, it stands still and targets no
   player until a player is in 6 blocks.
   After that, it acts as a normal zombie.

10. **Stoneblood sinks in water.**
    Zombies already sink in water in vanilla, so this downside does nothing.
    Recommendation: change it to "50% slower in water".

11. **Packcaller calls the zombies in 24 blocks.**
    Recommendation: when it first sees a player, all zombies in 24 blocks
    target that player.
    It can call again after 30 seconds.
    Its groans can be heard from two times farther.

12. **Brood adds 1 zombie to its spawn group.**
    Recommendation: when a Brood zombie spawns in a natural way, 1 more zombie
    spawns near it, with the same genes.

13. **Grave Call calls a new zombie when hit.**
    Recommendation: the new zombie is neutral, with no genes, so that the calls
    do not grow out of control.
    Each Grave Call zombie can call a maximum of 2 zombies.

14. **Siege breaks glass slowly.**
    Recommendation: glass blocks and glass panes only, 10 seconds for each
    block.
    Siege obeys the gamerule that stops mobs from breaking blocks.

15. **Runt babies and chicken jockeys.**
    Recommendation: Runt babies can still ride chickens, at the vanilla chance.

16. **Leech takes XP with each hit.**
    Recommendation: 3 XP points for each hit.
    When it dies, it drops the XP that it took, and no other XP.

17. **Miasma glows, so players see it through walls.**
    Recommendation: it has the Glowing effect all the time.
    Its aura gives Weakness I to players in 3 blocks, renewed each 2 seconds.

18. **Plague Carrier makes a killed player rise as a zombie.**
    Recommendation: a zombie with the genes of the killer spawns where the
    player died.
    It wears the head of the player.
    The items of the player drop on the ground as in vanilla, so the new
    zombie does not take them.

## Pressures

19. **Spotted: the player sees the zombie from more than 8 blocks.**
    Recommendation: the death counts when the zombie was in the view of the
    killer, at more than 8 blocks, for 2 seconds or more before the death.

20. **Crowd: 3 or more zombies die in 8 blocks in 5 seconds.**
    Recommendation: the death counts for each of these zombies, including the
    first two.

21. **Player death: a zombie kills a player.**
    The design says: "Mobs that kill the player get more of the genes that did
    the kill."
    Recommendation: each gene that the killer carries rises by its selection
    rate.
    The `player_death` pressure also counts one time, for Plague Carrier.

## Families

22. **Stalker effects: "the first hit on a player".**
    Recommendation: the first hit after 10 seconds with no combat.

23. **Horde effects: "all zombies in N blocks attack the attacker".**
    Recommendation: all zombies, of any family.

24. **How the player sees the family of a zombie.**
    Recommendation: only through the gene marks and the notebook.
    No name over the head.

25. **The notebook page for families.**
    Recommendation: the zombie page gets a list of the families that the
    player found.
    A player finds a family when a zombie of that family is in 16 blocks.
    The list shows the name, the major and sub family, and the effects.
    Families that the player did not find show as "???".
    This list is the start of the knowledge percentage of milestone 3.

## Modelling

26. **Gene marks on the same region.**
    With 30 genes and 5 genes on each zombie, two marks often go on the same
    region.
    For example, Bloodscent, Rabid, Hush, Nightborn, Packcaller, and Leech all
    mark the face.
    Recommendation: the modeller adds smaller regions to the region map, for
    example mouth, eyes, brow, chest, belly, back, and sleeves.
    Each gene then marks a different small region where possible.

27. **Model changes in milestone 2a.**
    The design moves the model changes of Bloat, Thickskull, and Sporebearer to
    the beta stage.
    Recommendation: do them in milestone 2a, while the modeller works on these
    genes.
    All other genes stay texture only.

28. **Miasma has a green haze on its pants.**
    Recommendation: a green tint on the texture, and a few green particles near
    the zombie.

29. **The review of the visuals.**
    Recommendation: the modeller sends one preview sheet for each family,
    with 6 genes on white, front and back.
    You approve each sheet, or ask for changes.

## Other

30. **The mutation rate with 30 genes.**
    Each gene has a 1% mutation rate now.
    With 30 genes, about 26% of all zombies get one or more random genes, from
    the start of a world.
    Recommendation: lower the default to 0.3%, so that about 9% of zombies get a
    random gene.
    Then evolution, not chance, makes most of the changes.

31. **Zombies from older versions.**
    Zombies that spawned in 0.1.0-alpha keep their genes.
    Recommendation: no change is necessary.
