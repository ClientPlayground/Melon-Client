package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

public class S22PacketMultiBlockChange implements Packet<INetHandlerPlayClient> {
  private ChunkCoordIntPair chunkPosCoord;
  
  private BlockUpdateData[] changedBlocks;
  
  public S22PacketMultiBlockChange() {}
  
  public S22PacketMultiBlockChange(int p_i45181_1_, short[] crammedPositionsIn, Chunk chunkIn) {
    this.chunkPosCoord = new ChunkCoordIntPair(chunkIn.xPosition, chunkIn.zPosition);
    this.changedBlocks = new BlockUpdateData[p_i45181_1_];
    for (int i = 0; i < this.changedBlocks.length; i++)
      this.changedBlocks[i] = new BlockUpdateData(crammedPositionsIn[i], chunkIn); 
  }
  
  public void readPacketData(PacketBuffer buf) throws IOException {
    this.chunkPosCoord = new ChunkCoordIntPair(buf.readInt(), buf.readInt());
    this.changedBlocks = new BlockUpdateData[buf.readVarIntFromBuffer()];
    for (int i = 0; i < this.changedBlocks.length; i++)
      this.changedBlocks[i] = new BlockUpdateData(buf.readShort(), (IBlockState)Block.BLOCK_STATE_IDS.getByValue(buf.readVarIntFromBuffer())); 
  }
  
  public void writePacketData(PacketBuffer buf) throws IOException {
    buf.writeInt(this.chunkPosCoord.chunkXPos);
    buf.writeInt(this.chunkPosCoord.chunkZPos);
    buf.writeVarIntToBuffer(this.changedBlocks.length);
    for (BlockUpdateData s22packetmultiblockchange$blockupdatedata : this.changedBlocks) {
      buf.writeShort(s22packetmultiblockchange$blockupdatedata.func_180089_b());
      buf.writeVarIntToBuffer(Block.BLOCK_STATE_IDS.get(s22packetmultiblockchange$blockupdatedata.getBlockState()));
    } 
  }
  
  public void processPacket(INetHandlerPlayClient handler) {
    handler.handleMultiBlockChange(this);
  }
  
  public BlockUpdateData[] getChangedBlocks() {
    return this.changedBlocks;
  }
  
  public class BlockUpdateData {
    private final short chunkPosCrammed;
    
    private final IBlockState blockState;
    
    public BlockUpdateData(short p_i45984_2_, IBlockState state) {
      this.chunkPosCrammed = p_i45984_2_;
      this.blockState = state;
    }
    
    public BlockUpdateData(short p_i45985_2_, Chunk chunkIn) {
      this.chunkPosCrammed = p_i45985_2_;
      this.blockState = chunkIn.getBlockState(getPos());
    }
    
    public BlockPos getPos() {
      return new BlockPos((Vec3i)S22PacketMultiBlockChange.this.chunkPosCoord.getBlock(this.chunkPosCrammed >> 12 & 0xF, this.chunkPosCrammed & 0xFF, this.chunkPosCrammed >> 8 & 0xF));
    }
    
    public short func_180089_b() {
      return this.chunkPosCrammed;
    }
    
    public IBlockState getBlockState() {
      return this.blockState;
    }
  }
}
