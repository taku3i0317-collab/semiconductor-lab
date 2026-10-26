package com.taku3.semiconductorlab.machine;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * 手のテストを速くする（S-031 の2、ユーザー「テストがマジでやりにくい」）：/semilab speed &lt;1〜20&gt;（コマンドの権限がある人＝クリエイティブ・チートありだけ）。
 * 電気の機械（ConsumerBlockEntity）を1 tick に N 回動かす＝時間が 1/N。増やした分の電気はただ（テストで電気待ちにしない。ふつうの遊びでは使わない）。世界に保存しない（再起動で 1 に戻る）
 */
public final class TestSpeed {
    private TestSpeed() {}
    public static final int MAX = 20;
    private static int factor = 1;

    public static int factor() { return factor; }
    public static void set(int f) { factor = Math.max(1, Math.min(MAX, f)); }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, ctx, sel) -> dispatcher.register(Commands.literal("semilab")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("speed").then(Commands.argument("times", IntegerArgumentType.integer(1, MAX)).executes(c -> {
                    set(IntegerArgumentType.getInteger(c, "times"));
                    c.getSource().sendSuccess(() -> Component.translatable("message.semiconductor-lab.test_speed", factor), true);
                    return factor;
                })))));
    }
}
