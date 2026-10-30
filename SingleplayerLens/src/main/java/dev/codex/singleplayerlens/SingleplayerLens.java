package dev.codex.singleplayerlens;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SingleplayerLens implements ClientModInitializer {
    public static final String MOD_ID = "singleplayerlens";
    private static final Map<ChunkPos, List<BlockPos>> STORAGE = new HashMap<>();
    private static final Map<ChunkPos, Integer> SUSPECT_CHUNKS = new HashMap<>();
    private static KeyBinding openMenu;
    private static int ticks;
    public static boolean storageEsp = true;
    public static boolean suspectFinder = true;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(net.minecraft.util.Identifier.of(MOD_ID, "main"));
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.singleplayerlens.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) client.setScreen(new LensScreen());
            if (++ticks % 20 == 0) refreshLocalWorld(client);
        });
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(SingleplayerLens::renderLocalWorld);
    }

    private static boolean localWorld(MinecraftClient client) {
        return client != null && client.world != null && client.player != null && client.getServer() != null;
    }

    private static void refreshLocalWorld(MinecraftClient client) {
        STORAGE.clear();
        SUSPECT_CHUNKS.clear();
        if (!localWorld(client)) return;
        World world = client.world;
        for (BlockEntity entity : world.getBlockEntities()) {
            BlockPos pos = entity.getPos().toImmutable();
            Block block = world.getBlockState(pos).getBlock();
            if (!isStorage(block)) continue;
            ChunkPos chunk = new ChunkPos(pos);
            STORAGE.computeIfAbsent(chunk, ignored -> new ArrayList<>()).add(pos);
        }
        STORAGE.forEach((chunk, positions) -> {
            long shulkers = positions.stream().filter(pos -> world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock).count();
            if (positions.size() >= 3 || (shulkers > 0 && positions.size() >= 2)) {
                SUSPECT_CHUNKS.put(chunk, positions.size());
            }
        });
    }

    private static boolean isStorage(Block block) {
        return block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.BARREL
                || block instanceof ShulkerBoxBlock || block == Blocks.ENDER_CHEST;
    }

    public static int loadedStorageCount() {
        return STORAGE.values().stream().mapToInt(List::size).sum();
    }

    private static void renderLocalWorld(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!localWorld(client) || (!storageEsp && !suspectFinder)) return;
        Vec3d camera = client.gameRenderer.getCamera().getPos();
        MatrixStack matrices = context.matrices();
        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());

        if (storageEsp) {
            for (List<BlockPos> positions : STORAGE.values()) {
                for (BlockPos pos : positions) {
                    if (pos.getSquaredDistance(camera) > 96 * 96) continue;
                    WorldRenderer.drawBox(matrices, lines, new Box(pos).expand(0.002), 0.95f, 0.35f, 0.95f, 0.95f);
                }
            }
        }
        if (suspectFinder) {
            SUSPECT_CHUNKS.forEach((chunk, count) -> {
                int minY = STORAGE.getOrDefault(chunk, List.of()).stream().mapToInt(BlockPos::getY).min().orElse(client.world.getBottomY());
                int maxY = STORAGE.getOrDefault(chunk, List.of()).stream().mapToInt(BlockPos::getY).max().orElse(client.world.getTopY());
                double x = chunk.getStartX();
                double z = chunk.getStartZ();
                Box box = new Box(x, minY - 1, z, x + 16, maxY + 2, z + 16);
                if (box.getCenter().distanceTo(camera) <= 128) {
                    WorldRenderer.drawBox(matrices, lines, box, 1.0f, 0.58f, 0.15f, 0.9f);
                }
            });
        }
        matrices.pop();
    }
}
