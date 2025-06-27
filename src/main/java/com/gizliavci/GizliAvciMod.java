package com.gizliavci;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.CommandSourceStack;
import org.slf4j.Logger;

import java.util.*;

import static net.minecraft.commands.Commands.literal;

@Mod("gizliavci")
public class GizliAvciMod {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<UUID> masumlar = new ArrayList<>();
    private static UUID avciUUID = null;
    private static boolean gucAktif = false;

    public GizliAvciMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("gizliavci")
            .then(literal("baslat").executes(ctx -> {
                List<ServerPlayer> players = ctx.getSource().getServer().getPlayerList().getPlayers();
                if (players.size() < 2) {
                    ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("En az 2 oyuncu gerekiyor!"));
                    return 0;
                }
                Collections.shuffle(players);
                ServerPlayer avci = players.get(0);
                avciUUID = avci.getUUID();
                for (ServerPlayer player : players) {
                    if (player.getUUID().equals(avciUUID)) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cAVCISIN"), false);
                    } else {
                        masumlar.add(player.getUUID());
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aMASUMSUN"), false);
                    }
                }
                ctx.getSource().sendSuccess(net.minecraft.network.chat.Component.literal("Avcı oyunu başlatıldı!"), true);
                return 1;
            }))
        );

        event.getDispatcher().register(literal("kuvvet").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayer();
            if (player != null && player.getUUID().equals(avciUUID)) {
                gucAktif = !gucAktif;
                if (gucAktif) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 1, false, false));
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cKuvvet aktif!"), false);
                } else {
                    player.removeEffect(MobEffects.DAMAGE_BOOST);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7Kuvvet kapatıldı!"), false);
                }
            }
            return 1;
        }));
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().getUUID().equals(avciUUID)) return;
        event.getEntity().kill();
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.getUUID().equals(avciUUID)) {
            masumlar.remove(player.getUUID());
        }
    }
}