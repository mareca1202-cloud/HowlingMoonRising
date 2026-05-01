package com.howlingmoon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

public class WolfsbaneFlowerBlock extends FlowerBlock {
    public static final BooleanProperty PLAYER_PLACED = BooleanProperty.create("player_placed");

    public WolfsbaneFlowerBlock(Holder<MobEffect> effect, float effectDuration, Properties properties) {
        super(effect, effectDuration, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PLAYER_PLACED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PLAYER_PLACED);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            return state.setValue(PLAYER_PLACED, true);
        }
        return null;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            boolean isWerewolf = false;
            
            if (entity instanceof WerewolfEntity) {
                isWerewolf = true;
            } else if (entity instanceof Player player) {
                WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
                if (cap.isWerewolf()) {
                    isWerewolf = true;
                }
            }
            
            if (isWerewolf) {
                living.hurt(level.damageSources().magic(), 1.0F);
                living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 100, 1));
                living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                if (entity instanceof Player) {
                    living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 60, 0));
                    HMAdvancements.trigger((ServerPlayer) entity, "wolfsbane_burn");
                }
            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public @Nullable PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        if (mob instanceof WerewolfEntity) {
            if (!state.getValue(PLAYER_PLACED)) {
                return PathType.DAMAGE_OTHER;
            }
        }
        return super.getBlockPathType(state, level, pos, mob);
    }
}
