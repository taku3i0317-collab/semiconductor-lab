package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.content.Power;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 変圧器 [4-10]（1885 ガンツ社のジペルノフスキー・ブラーティ・デーリ＝ZBD の閉じた鉄心）。鉄心に巻いた2つのコイルで、交流の電圧を変える
 * （巻き数の比＝電圧の比）。後ろの面に低圧の網・前の面に高圧の網なら昇圧、逆なら降圧。
 * 交流の網でしか働かない：直流では鉄心の磁束が変わらず、となりのコイルに電圧が出ない（入力・出力とも AC＝直流の網には入らない）。
 * 効率 98%（本物の大きな変圧器は 98〜99%、小さい物はもっと低い ⚠️）。
 */
public class TransformerBlockEntity extends PowerConverterBlockEntity {
    public TransformerBlockEntity(BlockPos pos, BlockState state) { super(Power.TRANSFORMER.type, pos, state); }
    @Override protected Current inputCurrent() { return Current.AC; }
    @Override protected Current outputCurrent() { return Current.AC; }
    @Override protected int efficiencyPercent() { return 98; }
}
