package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class TileEntityEnchantmentTableRenderer extends TileEntitySpecialRenderer<TileEntityEnchantmentTable> {
  private static final ResourceLocation TEXTURE_BOOK = new ResourceLocation("textures/entity/enchanting_table_book.png");
  
  private ModelBook field_147541_c = new ModelBook();
  
  public void renderTileEntityAt(TileEntityEnchantmentTable te, double x, double y, double z, float partialTicks, int destroyStage) {
    GlStateManager.pushMatrix();
    GlStateManager.translate((float)x + 0.5F, (float)y + 0.75F, (float)z + 0.5F);
    float f = te.tickCount + partialTicks;
    GlStateManager.translate(0.0F, 0.1F + MathHelper.sin(f * 0.1F) * 0.01F, 0.0F);
    float f1;
    for (f1 = te.bookRotation - te.bookRotationPrev; f1 >= 3.1415927F; f1 -= 6.2831855F);
    while (f1 < -3.1415927F)
      f1 += 6.2831855F; 
    float f2 = te.bookRotationPrev + f1 * partialTicks;
    GlStateManager.rotate(-f2 * 180.0F / 3.1415927F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(80.0F, 0.0F, 0.0F, 1.0F);
    bindTexture(TEXTURE_BOOK);
    float f3 = te.pageFlipPrev + (te.pageFlip - te.pageFlipPrev) * partialTicks + 0.25F;
    float f4 = te.pageFlipPrev + (te.pageFlip - te.pageFlipPrev) * partialTicks + 0.75F;
    f3 = (f3 - MathHelper.truncateDoubleToInt(f3)) * 1.6F - 0.3F;
    f4 = (f4 - MathHelper.truncateDoubleToInt(f4)) * 1.6F - 0.3F;
    if (f3 < 0.0F)
      f3 = 0.0F; 
    if (f4 < 0.0F)
      f4 = 0.0F; 
    if (f3 > 1.0F)
      f3 = 1.0F; 
    if (f4 > 1.0F)
      f4 = 1.0F; 
    float f5 = te.bookSpreadPrev + (te.bookSpread - te.bookSpreadPrev) * partialTicks;
    GlStateManager.enableCull();
    this.field_147541_c.render((Entity)null, f, f3, f4, f5, 0.0F, 0.0625F);
    GlStateManager.popMatrix();
  }
}
