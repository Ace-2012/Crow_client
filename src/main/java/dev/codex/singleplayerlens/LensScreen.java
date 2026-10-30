package dev.codex.singleplayerlens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class LensScreen extends Screen {
    private static final int ACCENT = 0xFFE276D8;
    private static final int PANEL = 0xE91A111D;
    private static final int TILE = 0xB9231827;
    private static final int MUTED = 0xFF9E94A2;
    private static final int WHITE = 0xFFF5F1F7;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;

    public LensScreen() {
        super(Text.literal("Singleplayer Lens"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x5206000A);
        panelWidth = Math.min(920, width - 36);
        panelLeft = (width - panelWidth) / 2;
        panelTop = Math.max(18, (height - 440) / 2);
        int panelHeight = Math.min(height - 36, 440);

        context.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, PANEL);
        context.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 48, 0xF20D0912);
        context.drawTextWithShadow(textRenderer, "SINGLEPLAYER LENS", panelLeft + 20, panelTop + 16, WHITE);
        context.drawTextWithShadow(textRenderer, "WORLD TOOLS  /  LOCAL SAVE", panelLeft + panelWidth - 190, panelTop + 18, ACCENT);

        int gap = 12;
        int colWidth = (panelWidth - 40 - gap * 2) / 3;
        String[] columns = {"RENDER", "WORLD", "SESSION"};
        for (int i = 0; i < columns.length; i++) {
            int x = panelLeft + 14 + i * (colWidth + gap);
            drawColumn(context, x, panelTop + 62, colWidth, columns[i], i);
        }
        context.drawTextWithShadow(textRenderer, "Right Shift  •  menu", panelLeft + 18, panelTop + panelHeight - 22, MUTED);
        context.drawTextWithShadow(textRenderer, "Local integrated worlds only", panelLeft + panelWidth - 188, panelTop + panelHeight - 22, MUTED);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawColumn(DrawContext context, int x, int y, int w, String title, int index) {
        context.fill(x, y, x + w, panelTop + 392, 0xA90D0911);
        context.fill(x, y, x + w, y + 38, 0xEF100B16);
        context.drawCenteredTextWithShadow(textRenderer, title, x + w / 2, y + 13, ACCENT);
        int rowY = y + 48;
        if (index == 0) {
            drawModule(context, x, rowY, w, "Storage ESP", "Outlines loaded chests, barrels and shulkers", SingleplayerLens.storageEsp);
            drawModule(context, x, rowY + 90, w, "Suspicious Chunks", "Marks loaded chunks with clustered storage", SingleplayerLens.suspectFinder);
        } else if (index == 1) {
            drawInfo(context, x, rowY, w, "Scan source", "Loaded block entities in this local save");
            drawInfo(context, x, rowY + 82, w, "Chunk rule", "3+ containers, or 2+ with a shulker");
            drawInfo(context, x, rowY + 164, w, "Range", "96 blocks for storage • 128 for chunks");
        } else {
            boolean local = MinecraftClient.getInstance().getServer() != null;
            drawInfo(context, x, rowY, w, "World access", local ? "LOCAL SINGLEPLAYER" : "WAITING FOR LOCAL WORLD");
            drawInfo(context, x, rowY + 82, w, "Current results", SingleplayerLens.loadedStorageCount() + " loaded containers");
        }
    }

    private void drawModule(DrawContext context, int x, int y, int w, String title, String detail, boolean enabled) {
        context.fill(x + 7, y, x + w - 7, y + 78, TILE);
        context.drawTextWithShadow(textRenderer, title, x + 16, y + 12, enabled ? ACCENT : WHITE);
        context.drawTextWithShadow(textRenderer, detail, x + 16, y + 33, MUTED);
        context.fill(x + w - 34, y + 12, x + w - 17, y + 22, enabled ? 0xFF60E878 : 0xFF655B68);
        context.fill(enabled ? x + w - 24 : x + w - 34, y + 13, enabled ? x + w - 18 : x + w - 28, y + 21, WHITE);
    }

    private void drawInfo(DrawContext context, int x, int y, int w, String title, String detail) {
        context.fill(x + 7, y, x + w - 7, y + 70, TILE);
        context.drawTextWithShadow(textRenderer, title, x + 16, y + 12, ACCENT);
        context.drawTextWithShadow(textRenderer, detail, x + 16, y + 34, MUTED);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int rowY = panelTop + 62 + 48;
            int colWidth = (panelWidth - 64) / 3;
            int firstX = panelLeft + 14;
            if (inside(mouseX, mouseY, firstX, rowY, colWidth, 78)) SingleplayerLens.storageEsp = !SingleplayerLens.storageEsp;
            if (inside(mouseX, mouseY, firstX, rowY + 90, colWidth, 78)) SingleplayerLens.suspectFinder = !SingleplayerLens.suspectFinder;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x + 7 && mx <= x + w - 7 && my >= y && my <= y + h;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
