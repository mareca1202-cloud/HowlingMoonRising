package com.howlingmoon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;

public class HMAdvancements {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, HowlingMoon.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, GenericTrigger> GENERIC = TRIGGERS.register("generic", GenericTrigger::new);

    public static void register(IEventBus modEventBus) {
        TRIGGERS.register(modEventBus);
    }

    public static void trigger(ServerPlayer player, String id) {
        GENERIC.get().trigger(player, id);
    }

    public static class GenericTrigger extends SimpleCriterionTrigger<GenericTrigger.Instance> {
        @Override
        public Codec<Instance> codec() {
            return Instance.CODEC;
        }

        public void trigger(ServerPlayer player, String id) {
            super.trigger(player, instance -> instance.id().equals(id));
        }

        public record Instance(Optional<ContextAwarePredicate> player, String id) implements SimpleCriterionTrigger.SimpleInstance {
            public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                    Codec.STRING.fieldOf("id").forGetter(Instance::id)
            ).apply(instance, Instance::new));
        }
    }
}
