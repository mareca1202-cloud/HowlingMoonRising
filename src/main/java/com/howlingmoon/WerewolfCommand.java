// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("werewolf")
                        .requires(source -> source.hasPermission(2)) // Solo OP
                        .then(Commands.literal("transform").executes(WerewolfCommand::transform))
                        .then(Commands.literal("set").executes(WerewolfCommand::setWerewolf))
                        .then(Commands.literal("reset").executes(WerewolfCommand::resetWerewolf))
                        .then(Commands.literal("setlevel")
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 20))
                                        .executes(WerewolfCommand::setLevel)))
                        .then(Commands.literal("addxp")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10000))
                                        .executes(WerewolfCommand::addXp)))
                        .then(Commands.literal("stats").executes(WerewolfCommand::showStats)));
    }

    private static int setLevel(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            int newLevel = IntegerArgumentType.getInteger(ctx, "level");

            cap.setWerewolf(true);
            cap.forceSetLevel(newLevel);

            syncToClient(player, cap);
            ctx.getSource().sendSuccess(() -> Component.literal("§aNivel de hombre lobo ajustado a: " + newLevel),
                    true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int resetWerewolf(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            cap.reset();
            WerewolfAttributeHandler.removeAllModifiers(player);
            syncToClient(player, cap);
            ctx.getSource().sendSuccess(() -> Component.literal("§cProgresión de hombre lobo reiniciada."), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int transform(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            if (!cap.isWerewolf())
                return 0;

            boolean nowTransformed = !cap.isTransformed();
            cap.setTransformed(nowTransformed);
            if (nowTransformed)
                WerewolfAttributeHandler.applyAllModifiers(player, cap);
            else
                WerewolfAttributeHandler.removeAllModifiers(player);

            syncToClient(player, cap);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int setWerewolf(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            cap.setWerewolf(true);
            syncToClient(player, cap);
            ctx.getSource().sendSuccess(() -> Component.literal("§aAhora eres un hombre lobo."), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int addXp(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            int amount = IntegerArgumentType.getInteger(ctx, "amount");
            cap.addExperience(amount);
            syncToClient(player, cap);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int showStats(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6--- Werewolf Stats ---\n§eLevel: §f" + cap.getLevel() + "\n§eXP: §f" + cap.getExperience()),
                    false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            return 0;
        }
    }

    public static void syncToClient(ServerPlayer player, WerewolfCapability cap) {
        PacketDistributor.sendToPlayer(player, SyncWerewolfPacket.fromCap(cap));
    }
}