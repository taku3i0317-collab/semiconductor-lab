package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.HowToGet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;

/**
 * ツールチップの「作り方」をアイコンの並びに（S-030 ユーザー「文章でなく、アイテムのアイコンが無いと分かりにくい」）：
 * 材料のアイコン ×数 ＋ … → 機械（作業台・かまど）のアイコン → できる物のアイコン。文の中に絵を出す AtlasSprite（26.x の object の文）を使う。
 * 元は assets/howto_get.json の first・icons（tools/check_howto.py）。バニラの物はブロックなら blocks の block/、ほかは items の item/ の絵
 */
public final class RecipeIcons {
    private RecipeIcons() {}
    private static final Identifier ITEMS = Identifier.withDefaultNamespace("items"), BLOCKS = Identifier.withDefaultNamespace("blocks");

    /** 1つの物のアイコン（絵が分からなければ null） */
    static MutableComponent icon(String id) {
        String ns = id.contains(":") ? id.substring(0, id.indexOf(':')) : "minecraft", path = id.substring(id.indexOf(':') + 1);
        if (ns.equals("semiconductor-lab")) {
            String[] ic = HowToGet.icon(path);
            if (ic == null) return null;
            return Component.object(new AtlasSprite(Identifier.parse(ic[0]), Identifier.parse(ic[1])));
        }
        var item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ns, path));
        boolean block = item instanceof BlockItem;
        return Component.object(new AtlasSprite(block ? BLOCKS : ITEMS, Identifier.fromNamespaceAndPath(ns, (block ? "block/" : "item/") + path)));
    }

    /** 「作り方：[材料]×2 + [材料] → [機械] → [できる物]」の1行（作り方が分からなければ null） */
    public static Component line(String id, String label) {
        var f = HowToGet.first(id);
        if (f == null) return null;
        MutableComponent out = Component.literal(label).withStyle(ChatFormatting.BLUE);
        boolean firstIn = true;
        for (var e : f.getAsJsonArray("in")) {
            var pair = e.getAsJsonArray();
            MutableComponent ic = icon(pair.get(0).getAsString());
            if (ic == null) continue;
            if (!firstIn) out.append(Component.literal(" + ").withStyle(ChatFormatting.GRAY));
            out.append(ic);
            int n = pair.get(1).getAsInt();
            if (n > 1) out.append(Component.literal("×" + n).withStyle(ChatFormatting.GRAY));
            firstIn = false;
        }
        String via = f.get("via").getAsString();
        String viaId = via.startsWith("machine:") ? "semiconductor-lab:" + via.substring(8)
                : via.equals("crafting") ? "minecraft:crafting_table" : via.equals("campfire") ? "minecraft:campfire" : via.equals("smelting") ? "minecraft:furnace" : null;
        out.append(Component.literal(" → ").withStyle(ChatFormatting.GRAY));
        MutableComponent m = viaId == null ? null : viaId.equals("minecraft:furnace") ? Component.object(new AtlasSprite(BLOCKS, Identifier.withDefaultNamespace("block/furnace_front")))
                : viaId.equals("minecraft:crafting_table") ? Component.object(new AtlasSprite(BLOCKS, Identifier.withDefaultNamespace("block/crafting_table_front")))
                : viaId.equals("minecraft:campfire") ? Component.object(new AtlasSprite(ITEMS, Identifier.withDefaultNamespace("item/campfire")))
                : icon(viaId);
        if (m != null) out.append(m).append(Component.literal(" → ").withStyle(ChatFormatting.GRAY));
        MutableComponent self = icon("semiconductor-lab:" + id);
        if (self != null) out.append(self);
        return out;
    }
}
