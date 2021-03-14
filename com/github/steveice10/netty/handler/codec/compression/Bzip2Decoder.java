package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class Bzip2Decoder extends ByteToMessageDecoder {
  private enum State {
    INIT, INIT_BLOCK, INIT_BLOCK_PARAMS, RECEIVE_HUFFMAN_USED_MAP, RECEIVE_HUFFMAN_USED_BITMAPS, RECEIVE_SELECTORS_NUMBER, RECEIVE_SELECTORS, RECEIVE_HUFFMAN_LENGTH, DECODE_HUFFMAN_DATA, EOF;
  }
  
  private State currentState = State.INIT;
  
  private final Bzip2BitReader reader = new Bzip2BitReader();
  
  private Bzip2BlockDecompressor blockDecompressor;
  
  private Bzip2HuffmanStageDecoder huffmanStageDecoder;
  
  private int blockSize;
  
  private int blockCRC;
  
  private int streamCRC;
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (!in.isReadable())
      return; 
    Bzip2BitReader reader = this.reader;
    reader.setByteBuf(in);
    while (true) {
      int magicNumber;
      int blockSize;
      int magic1;
      int magic2;
      boolean blockRandomised;
      int bwtStartPointer;
      Bzip2BlockDecompressor blockDecompressor;
      int inUse16;
      int bitNumber;
      byte[] huffmanSymbolMap;
      int huffmanSymbolCount;
      int totalTables;
      int alphaSize;
      int totalSelectors;
      Bzip2HuffmanStageDecoder huffmanStageDecoder;
      byte[] selectors;
      Bzip2MoveToFrontTable tableMtf;
      int currSelector;
      byte[][] codeLength;
      int currLength;
      int currAlpha;
      boolean modifyLength;
      boolean saveStateAndReturn;
      int currGroup;
      int oldReaderIndex;
      boolean decoded;
      int blockLength;
      ByteBuf uncompressed;
      boolean success;
      switch (this.currentState) {
        case INIT:
          if (in.readableBytes() < 4)
            return; 
          magicNumber = in.readUnsignedMedium();
          if (magicNumber != 4348520)
            throw new DecompressionException("Unexpected stream identifier contents. Mismatched bzip2 protocol version?"); 
          blockSize = in.readByte() - 48;
          if (blockSize < 1 || blockSize > 9)
            throw new DecompressionException("block size is invalid"); 
          this.blockSize = blockSize * 100000;
          this.streamCRC = 0;
          this.currentState = State.INIT_BLOCK;
        case INIT_BLOCK:
          if (!reader.hasReadableBytes(10))
            return; 
          magic1 = reader.readBits(24);
          magic2 = reader.readBits(24);
          if (magic1 == 1536581 && magic2 == 3690640) {
            int storedCombinedCRC = reader.readInt();
            if (storedCombinedCRC != this.streamCRC)
              throw new DecompressionException("stream CRC error"); 
            this.currentState = State.EOF;
            continue;
          } 
          if (magic1 != 3227993 || magic2 != 2511705)
            throw new DecompressionException("bad block header"); 
          this.blockCRC = reader.readInt();
          this.currentState = State.INIT_BLOCK_PARAMS;
        case INIT_BLOCK_PARAMS:
          if (!reader.hasReadableBits(25))
            return; 
          blockRandomised = reader.readBoolean();
          bwtStartPointer = reader.readBits(24);
          this.blockDecompressor = new Bzip2BlockDecompressor(this.blockSize, this.blockCRC, blockRandomised, bwtStartPointer, reader);
          this.currentState = State.RECEIVE_HUFFMAN_USED_MAP;
        case RECEIVE_HUFFMAN_USED_MAP:
          if (!reader.hasReadableBits(16))
            return; 
          this.blockDecompressor.huffmanInUse16 = reader.readBits(16);
          this.currentState = State.RECEIVE_HUFFMAN_USED_BITMAPS;
        case RECEIVE_HUFFMAN_USED_BITMAPS:
          blockDecompressor = this.blockDecompressor;
          inUse16 = blockDecompressor.huffmanInUse16;
          bitNumber = Integer.bitCount(inUse16);
          huffmanSymbolMap = blockDecompressor.huffmanSymbolMap;
          if (!reader.hasReadableBits(bitNumber * 16 + 3))
            return; 
          huffmanSymbolCount = 0;
          if (bitNumber > 0)
            for (int i = 0; i < 16; i++) {
              if ((inUse16 & 32768 >>> i) != 0)
                for (int j = 0, k = i << 4; j < 16; j++, k++) {
                  if (reader.readBoolean())
                    huffmanSymbolMap[huffmanSymbolCount++] = (byte)k; 
                }  
            }  
          blockDecompressor.huffmanEndOfBlockSymbol = huffmanSymbolCount + 1;
          totalTables = reader.readBits(3);
          if (totalTables < 2 || totalTables > 6)
            throw new DecompressionException("incorrect huffman groups number"); 
          alphaSize = huffmanSymbolCount + 2;
          if (alphaSize > 258)
            throw new DecompressionException("incorrect alphabet size"); 
          this.huffmanStageDecoder = new Bzip2HuffmanStageDecoder(reader, totalTables, alphaSize);
          this.currentState = State.RECEIVE_SELECTORS_NUMBER;
        case RECEIVE_SELECTORS_NUMBER:
          if (!reader.hasReadableBits(15))
            return; 
          totalSelectors = reader.readBits(15);
          if (totalSelectors < 1 || totalSelectors > 18002)
            throw new DecompressionException("incorrect selectors number"); 
          this.huffmanStageDecoder.selectors = new byte[totalSelectors];
          this.currentState = State.RECEIVE_SELECTORS;
        case RECEIVE_SELECTORS:
          huffmanStageDecoder = this.huffmanStageDecoder;
          selectors = huffmanStageDecoder.selectors;
          totalSelectors = selectors.length;
          tableMtf = huffmanStageDecoder.tableMTF;
          currSelector = huffmanStageDecoder.currentSelector;
          for (; currSelector < totalSelectors; currSelector++) {
            if (!reader.hasReadableBits(6)) {
              huffmanStageDecoder.currentSelector = currSelector;
              return;
            } 
            int index = 0;
            while (reader.readBoolean())
              index++; 
            selectors[currSelector] = tableMtf.indexToFront(index);
          } 
          this.currentState = State.RECEIVE_HUFFMAN_LENGTH;
        case RECEIVE_HUFFMAN_LENGTH:
          huffmanStageDecoder = this.huffmanStageDecoder;
          totalTables = huffmanStageDecoder.totalTables;
          codeLength = huffmanStageDecoder.tableCodeLengths;
          alphaSize = huffmanStageDecoder.alphabetSize;
          currLength = huffmanStageDecoder.currentLength;
          currAlpha = 0;
          modifyLength = huffmanStageDecoder.modifyLength;
          saveStateAndReturn = false;
          label147: for (currGroup = huffmanStageDecoder.currentGroup; currGroup < totalTables; currGroup++) {
            if (!reader.hasReadableBits(5)) {
              saveStateAndReturn = true;
              break;
            } 
            if (currLength < 0)
              currLength = reader.readBits(5); 
            for (currAlpha = huffmanStageDecoder.currentAlpha; currAlpha < alphaSize; ) {
              if (!reader.isReadable()) {
                saveStateAndReturn = true;
                break label147;
              } 
              for (;; currAlpha++) {
                if (modifyLength || reader.readBoolean()) {
                  if (!reader.isReadable()) {
                    modifyLength = true;
                    saveStateAndReturn = true;
                    break;
                  } 
                  currLength += reader.readBoolean() ? -1 : 1;
                  modifyLength = false;
                  if (!reader.isReadable()) {
                    saveStateAndReturn = true;
                    break;
                  } 
                  continue;
                } 
                codeLength[currGroup][currAlpha] = (byte)currLength;
              } 
              break label147;
            } 
            currLength = -1;
            currAlpha = huffmanStageDecoder.currentAlpha = 0;
            modifyLength = false;
          } 
          if (saveStateAndReturn) {
            huffmanStageDecoder.currentGroup = currGroup;
            huffmanStageDecoder.currentLength = currLength;
            huffmanStageDecoder.currentAlpha = currAlpha;
            huffmanStageDecoder.modifyLength = modifyLength;
            return;
          } 
          huffmanStageDecoder.createHuffmanDecodingTables();
          this.currentState = State.DECODE_HUFFMAN_DATA;
        case DECODE_HUFFMAN_DATA:
          blockDecompressor = this.blockDecompressor;
          oldReaderIndex = in.readerIndex();
          decoded = blockDecompressor.decodeHuffmanData(this.huffmanStageDecoder);
          if (!decoded)
            return; 
          if (in.readerIndex() == oldReaderIndex && in.isReadable())
            reader.refill(); 
          blockLength = blockDecompressor.blockLength();
          uncompressed = ctx.alloc().buffer(blockLength);
          success = false;
          try {
            int uncByte;
            while ((uncByte = blockDecompressor.read()) >= 0)
              uncompressed.writeByte(uncByte); 
            int currentBlockCRC = blockDecompressor.checkCRC();
            this.streamCRC = (this.streamCRC << 1 | this.streamCRC >>> 31) ^ currentBlockCRC;
            out.add(uncompressed);
            success = true;
          } finally {
            if (!success)
              uncompressed.release(); 
          } 
          this.currentState = State.INIT_BLOCK;
          continue;
        case EOF:
          in.skipBytes(in.readableBytes());
          return;
      } 
      break;
    } 
    throw new IllegalStateException();
  }
  
  public boolean isClosed() {
    return (this.currentState == State.EOF);
  }
}
