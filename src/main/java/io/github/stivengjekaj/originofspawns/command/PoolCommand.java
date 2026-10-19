package io.github.stivengjekaj.originofspawns.command;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import io.github.stivengjekaj.originofspawns.OriginOfSpawns;
import io.github.stivengjekaj.originofspawns.definition.Gene;
import io.github.stivengjekaj.originofspawns.evolution.BiomeCategory;
import io.github.stivengjekaj.originofspawns.evolution.GenePools;
import io.github.stivengjekaj.originofspawns.registry.ModRegistries;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Debug commands that show and set gene pools.
 *
 * <pre>
 * /originofspawns pool get &lt;species&gt; &lt;category&gt;
 * /originofspawns pool set &lt;species&gt; &lt;category&gt; &lt;gene&gt; &lt;frequency&gt;
 * </pre>
 */
public final class PoolCommand {
	private static final DynamicCommandExceptionType UNKNOWN_CATEGORY =
			new DynamicCommandExceptionType(name -> Component.literal("Unknown biome category: " + name));
	private static final DynamicCommandExceptionType UNKNOWN_GENE =
			new DynamicCommandExceptionType(id -> Component.literal("Unknown gene: " + id));

	private PoolCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(Commands.literal(OriginOfSpawns.MOD_ID)
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("pool")
						.then(Commands.literal("get")
								.then(Commands.argument("species", ResourceArgument.resource(context, Registries.ENTITY_TYPE))
										.then(category().executes(PoolCommand::get))))
						.then(Commands.literal("set")
								.then(Commands.argument("species", ResourceArgument.resource(context, Registries.ENTITY_TYPE))
										.then(category()
												.then(Commands.argument("gene", ResourceKeyArgument.key(ModRegistries.GENE))
														.then(Commands.argument("frequency", FloatArgumentType.floatArg(0.0F, 1.0F))
																.executes(PoolCommand::set))))))));
	}

	private static RequiredArgumentBuilder<CommandSourceStack, String> category() {
		return Commands.argument("category", StringArgumentType.word())
				.suggests((context, builder) -> SharedSuggestionProvider.suggest(
						Arrays.stream(BiomeCategory.values()).map(BiomeCategory::getSerializedName), builder));
	}

	private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Identifier species = species(context);
		BiomeCategory category = category(context);
		Map<Identifier, Float> pool = GenePools.get(context.getSource().getServer()).pool(species, category);
		String genes = pool.isEmpty() ? "no genes" : pool.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> entry.getKey() + " = " + entry.getValue())
				.collect(Collectors.joining(", "));
		context.getSource().sendSuccess(() -> Component.literal(
				"Pool of " + species + " in " + category.getSerializedName() + ": " + genes), false);
		return pool.size();
	}

	private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Identifier species = species(context);
		BiomeCategory category = category(context);
		ResourceKey<Gene> gene = ResourceKeyArgument.getRegistryKey(context, "gene", ModRegistries.GENE, UNKNOWN_GENE);
		if (!context.getSource().registryAccess().lookupOrThrow(ModRegistries.GENE).containsKey(gene)) {
			throw UNKNOWN_GENE.create(gene.identifier());
		}
		float frequency = FloatArgumentType.getFloat(context, "frequency");
		GenePools.get(context.getSource().getServer()).setFrequency(species, category, gene.identifier(), frequency);
		context.getSource().sendSuccess(() -> Component.literal("Set " + gene.identifier() + " to " + frequency
				+ " in the pool of " + species + " in " + category.getSerializedName()), true);
		return 1;
	}

	private static Identifier species(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		return ResourceArgument.getResource(context, "species", Registries.ENTITY_TYPE).key().identifier();
	}

	private static BiomeCategory category(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		String name = StringArgumentType.getString(context, "category");
		return BiomeCategory.byName(name).orElseThrow(() -> UNKNOWN_CATEGORY.create(name));
	}
}
