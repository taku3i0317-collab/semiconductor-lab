package com.taku3.semiconductorlab.entity;

import com.taku3.semiconductorlab.SemiconductorLab;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;

/**
 * 家畜化 [0-17]。プレイヤーが繁殖させた牛・羊・山羊の子に「何世代目か」を付ける（エンティティのタグ semilab_gen_N、保存される）。
 * 親2頭のうち大きい方の世代＋1。最初の繁殖で進捗「家畜化のはじまり」、GENERATIONS 世代目で その動物の「家畜化」の進捗。
 * mixin/AnimalBreedingMixin が Animal.finalizeSpawnChildFromBreeding の最初に呼ぶ。新しい動物（野生種）は作らない。
 */
public final class Domestication {
    /** 家畜化の進捗に要る世代 */
    public static final int GENERATIONS = 3;
    private static final String TAG = "semilab_gen_";

    private Domestication() {}

    /** 家畜化の対象（牛・羊・山羊）なら、その名前（進捗の id）。違えば null */
    public static String species(EntityType<?> type) {
        if (type == EntityTypes.COW) return "cow";
        if (type == EntityTypes.SHEEP) return "sheep";
        if (type == EntityTypes.GOAT) return "goat";
        return null;
    }

    /** 何世代目か（野生＝0） */
    public static int generation(Entity e) {
        int g = 0;
        for (String t : e.entityTags())
            if (t.startsWith(TAG)) try { g = Math.max(g, Integer.parseInt(t.substring(TAG.length()))); } catch (NumberFormatException ignored) {}
        return g;
    }

    /** 子が生まれた：世代を付けて、育てたプレイヤーの進捗を進める。子の世代を返す（対象外は 0） */
    public static int onBred(ServerLevel level, Animal parent, Animal partner, AgeableMob child, ServerPlayer player) {
        String sp = species(child.getType());
        if (sp == null) return 0;
        int g = Math.max(generation(parent), generation(partner)) + 1;
        child.addTag(TAG + g);
        if (player != null) {
            award(level, player, "domestication/root");
            if (g >= GENERATIONS) award(level, player, "domestication/" + sp);
            player.sendOverlayMessage(g >= GENERATIONS
                    ? Component.translatable("message.semiconductor-lab.domestication.done", g)
                    : Component.translatable("message.semiconductor-lab.domestication.generation", g, GENERATIONS - g));
        }
        return g;
    }

    private static void award(ServerLevel level, ServerPlayer player, String path) {
        AdvancementHolder adv = level.getServer().getAdvancements().get(SemiconductorLab.id(path));
        if (adv != null) player.getAdvancements().award(adv, "bred");
    }
}
