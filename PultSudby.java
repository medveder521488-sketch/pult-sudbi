package ru.pultsudby;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static net.minecraft.commands.Commands.literal;

public class PultSudby implements ModInitializer {
    public static final String ID = "pult_sudby";
    private static final String TAG_KEY = "pult_sudby";
    private static final String TAG_VALUE = "remote";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // /pult — выдать пульт
            dispatcher.register(literal("pult").executes(ctx -> {
                if (ctx.getSource().getPlayer() instanceof ServerPlayer p) {
                    ItemStack stack = new ItemStack(Items.CLOCK);
                    stack.setHoverName(Component.literal("§bПульт судьбы"));
                    stack.getOrCreateTag().putString(TAG_KEY, TAG_VALUE);
                    if (!p.getInventory().add(stack)) {
                        p.drop(stack, false);
                    }
                    p.displayClientMessage(Component.literal("Пульт судьбы выдан. ПКМ — выбор режима."), false);
                }
                return 1;
            }));

            // /pult_blue — синий выбор: появляется преследователь
            dispatcher.register(literal("pult_blue").executes(ctx -> {
                if (ctx.getSource().getPlayer() instanceof ServerPlayer p) {
                    ServerLevel level = ctx.getSource().getLevel();
                    Zombie z = EntityType.ZOMBIE.create(level);
                    if (z != null) {
                        z.setCustomName(Component.literal("§bНеотступный"));
                        z.setCustomNameVisible(true);
                        z.setPersistenceRequired();
                        z.setInvulnerable(true);
                        z.setTarget(p);
                        z.setPos(p.getX() + 2, p.getY(), p.getZ() + 2);
                        level.addFreshEntity(z);
                        p.displayClientMessage(Component.literal("Синий выбор: преследователь появился."), false);
                    }
                }
                return 1;
            }));

            // /pult_red — красный выбор (пока только сообщение)
            dispatcher.register(literal("pult_red").executes(ctx -> {
                if (ctx.getSource().getPlayer() instanceof ServerPlayer p) {
                    p.displayClientMessage(Component.literal("Красный выбор: звуки и эффекты паранойи активированы."), false);
                }
                return 1;
            }));
        });

        // ПКМ пультом — подсказка с выбором
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            CompoundTag tag = stack.getTag();
            if (tag == null || !TAG_VALUE.equals(tag.getString(TAG_KEY))) {
                return InteractionResultHolder.pass(stack);
            }
            if (!world.isClientSide() && player instanceof ServerPlayer p) {
                p.displayClientMessage(Component.literal("Выбор: в чат введи /pult_blue или /pult_red"), false);
            }
            return InteractionResultHolder.success(stack);
        });
    }
}
