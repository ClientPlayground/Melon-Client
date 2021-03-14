package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import java.util.Arrays;

final class Bzip2HuffmanStageEncoder {
  private static final int HUFFMAN_HIGH_SYMBOL_COST = 15;
  
  private final Bzip2BitWriter writer;
  
  private final char[] mtfBlock;
  
  private final int mtfLength;
  
  private final int mtfAlphabetSize;
  
  private final int[] mtfSymbolFrequencies;
  
  private final int[][] huffmanCodeLengths;
  
  private final int[][] huffmanMergedCodeSymbols;
  
  private final byte[] selectors;
  
  Bzip2HuffmanStageEncoder(Bzip2BitWriter writer, char[] mtfBlock, int mtfLength, int mtfAlphabetSize, int[] mtfSymbolFrequencies) {
    this.writer = writer;
    this.mtfBlock = mtfBlock;
    this.mtfLength = mtfLength;
    this.mtfAlphabetSize = mtfAlphabetSize;
    this.mtfSymbolFrequencies = mtfSymbolFrequencies;
    int totalTables = selectTableCount(mtfLength);
    this.huffmanCodeLengths = new int[totalTables][mtfAlphabetSize];
    this.huffmanMergedCodeSymbols = new int[totalTables][mtfAlphabetSize];
    this.selectors = new byte[(mtfLength + 50 - 1) / 50];
  }
  
  private static int selectTableCount(int mtfLength) {
    if (mtfLength >= 2400)
      return 6; 
    if (mtfLength >= 1200)
      return 5; 
    if (mtfLength >= 600)
      return 4; 
    if (mtfLength >= 200)
      return 3; 
    return 2;
  }
  
  private static void generateHuffmanCodeLengths(int alphabetSize, int[] symbolFrequencies, int[] codeLengths) {
    int[] mergedFrequenciesAndIndices = new int[alphabetSize];
    int[] sortedFrequencies = new int[alphabetSize];
    int i;
    for (i = 0; i < alphabetSize; i++)
      mergedFrequenciesAndIndices[i] = symbolFrequencies[i] << 9 | i; 
    Arrays.sort(mergedFrequenciesAndIndices);
    for (i = 0; i < alphabetSize; i++)
      sortedFrequencies[i] = mergedFrequenciesAndIndices[i] >>> 9; 
    Bzip2HuffmanAllocator.allocateHuffmanCodeLengths(sortedFrequencies, 20);
    for (i = 0; i < alphabetSize; i++)
      codeLengths[mergedFrequenciesAndIndices[i] & 0x1FF] = sortedFrequencies[i]; 
  }
  
  private void generateHuffmanOptimisationSeeds() {
    int[][] huffmanCodeLengths = this.huffmanCodeLengths;
    int[] mtfSymbolFrequencies = this.mtfSymbolFrequencies;
    int mtfAlphabetSize = this.mtfAlphabetSize;
    int totalTables = huffmanCodeLengths.length;
    int remainingLength = this.mtfLength;
    int lowCostEnd = -1;
    for (int i = 0; i < totalTables; i++) {
      int targetCumulativeFrequency = remainingLength / (totalTables - i);
      int lowCostStart = lowCostEnd + 1;
      int actualCumulativeFrequency = 0;
      while (actualCumulativeFrequency < targetCumulativeFrequency && lowCostEnd < mtfAlphabetSize - 1)
        actualCumulativeFrequency += mtfSymbolFrequencies[++lowCostEnd]; 
      if (lowCostEnd > lowCostStart && i != 0 && i != totalTables - 1 && (totalTables - i & 0x1) == 0)
        actualCumulativeFrequency -= mtfSymbolFrequencies[lowCostEnd--]; 
      int[] tableCodeLengths = huffmanCodeLengths[i];
      for (int j = 0; j < mtfAlphabetSize; j++) {
        if (j < lowCostStart || j > lowCostEnd)
          tableCodeLengths[j] = 15; 
      } 
      remainingLength -= actualCumulativeFrequency;
    } 
  }
  
  private void optimiseSelectorsAndHuffmanTables(boolean storeSelectors) {
    char[] mtfBlock = this.mtfBlock;
    byte[] selectors = this.selectors;
    int[][] huffmanCodeLengths = this.huffmanCodeLengths;
    int mtfLength = this.mtfLength;
    int mtfAlphabetSize = this.mtfAlphabetSize;
    int totalTables = huffmanCodeLengths.length;
    int[][] tableFrequencies = new int[totalTables][mtfAlphabetSize];
    int selectorIndex = 0;
    int groupStart;
    for (groupStart = 0; groupStart < mtfLength; ) {
      int groupEnd = Math.min(groupStart + 50, mtfLength) - 1;
      short[] cost = new short[totalTables];
      for (int j = groupStart; j <= groupEnd; j++) {
        int value = mtfBlock[j];
        for (int m = 0; m < totalTables; m++)
          cost[m] = (short)(cost[m] + huffmanCodeLengths[m][value]); 
      } 
      byte bestTable = 0;
      int bestCost = cost[0];
      byte b1;
      for (b1 = 1; b1 < totalTables; b1 = (byte)(b1 + 1)) {
        int tableCost = cost[b1];
        if (tableCost < bestCost) {
          bestCost = tableCost;
          bestTable = b1;
        } 
      } 
      int[] bestGroupFrequencies = tableFrequencies[bestTable];
      for (int k = groupStart; k <= groupEnd; k++)
        bestGroupFrequencies[mtfBlock[k]] = bestGroupFrequencies[mtfBlock[k]] + 1; 
      if (storeSelectors)
        selectors[selectorIndex++] = bestTable; 
      groupStart = groupEnd + 1;
    } 
    for (int i = 0; i < totalTables; i++)
      generateHuffmanCodeLengths(mtfAlphabetSize, tableFrequencies[i], huffmanCodeLengths[i]); 
  }
  
  private void assignHuffmanCodeSymbols() {
    int[][] huffmanMergedCodeSymbols = this.huffmanMergedCodeSymbols;
    int[][] huffmanCodeLengths = this.huffmanCodeLengths;
    int mtfAlphabetSize = this.mtfAlphabetSize;
    int totalTables = huffmanCodeLengths.length;
    for (int i = 0; i < totalTables; i++) {
      int[] tableLengths = huffmanCodeLengths[i];
      int minimumLength = 32;
      int maximumLength = 0;
      for (int j = 0; j < mtfAlphabetSize; j++) {
        int length = tableLengths[j];
        if (length > maximumLength)
          maximumLength = length; 
        if (length < minimumLength)
          minimumLength = length; 
      } 
      int code = 0;
      for (int k = minimumLength; k <= maximumLength; k++) {
        for (int m = 0; m < mtfAlphabetSize; m++) {
          if ((huffmanCodeLengths[i][m] & 0xFF) == k) {
            huffmanMergedCodeSymbols[i][m] = k << 24 | code;
            code++;
          } 
        } 
        code <<= 1;
      } 
    } 
  }
  
  private void writeSelectorsAndHuffmanTables(ByteBuf out) {
    Bzip2BitWriter writer = this.writer;
    byte[] selectors = this.selectors;
    int totalSelectors = selectors.length;
    int[][] huffmanCodeLengths = this.huffmanCodeLengths;
    int totalTables = huffmanCodeLengths.length;
    int mtfAlphabetSize = this.mtfAlphabetSize;
    writer.writeBits(out, 3, totalTables);
    writer.writeBits(out, 15, totalSelectors);
    Bzip2MoveToFrontTable selectorMTF = new Bzip2MoveToFrontTable();
    for (byte selector : selectors)
      writer.writeUnary(out, selectorMTF.valueToFront(selector)); 
    for (int[] tableLengths : huffmanCodeLengths) {
      int currentLength = tableLengths[0];
      writer.writeBits(out, 5, currentLength);
      for (int j = 0; j < mtfAlphabetSize; j++) {
        int codeLength = tableLengths[j];
        int value = (currentLength < codeLength) ? 2 : 3;
        int delta = Math.abs(codeLength - currentLength);
        while (delta-- > 0)
          writer.writeBits(out, 2, value); 
        writer.writeBoolean(out, false);
        currentLength = codeLength;
      } 
    } 
  }
  
  private void writeBlockData(ByteBuf out) {
    Bzip2BitWriter writer = this.writer;
    int[][] huffmanMergedCodeSymbols = this.huffmanMergedCodeSymbols;
    byte[] selectors = this.selectors;
    char[] mtf = this.mtfBlock;
    int mtfLength = this.mtfLength;
    int selectorIndex = 0;
    for (int mtfIndex = 0; mtfIndex < mtfLength; ) {
      int groupEnd = Math.min(mtfIndex + 50, mtfLength) - 1;
      int[] tableMergedCodeSymbols = huffmanMergedCodeSymbols[selectors[selectorIndex++]];
      while (mtfIndex <= groupEnd) {
        int mergedCodeSymbol = tableMergedCodeSymbols[mtf[mtfIndex++]];
        writer.writeBits(out, mergedCodeSymbol >>> 24, mergedCodeSymbol);
      } 
    } 
  }
  
  void encode(ByteBuf out) {
    generateHuffmanOptimisationSeeds();
    for (int i = 3; i >= 0; i--)
      optimiseSelectorsAndHuffmanTables((i == 0)); 
    assignHuffmanCodeSymbols();
    writeSelectorsAndHuffmanTables(out);
    writeBlockData(out);
  }
}
