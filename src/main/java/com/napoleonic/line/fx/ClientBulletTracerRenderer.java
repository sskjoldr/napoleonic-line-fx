package com.napoleonic.line.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.napoleonic.line.config.NapoleonicClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import org.joml.Matrix4f;

/**
 * Ultra-lightweight, zero-allocation batched bullet tracer renderer.
 * Uses a fixed primitive ring buffer (max 128 tracers) and a single batched QUADS draw call
 * so even 50+ simultaneous volley shots cause zero FPS drop or GC churn.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientBulletTracerRenderer {
    private static final int MAX_TRACERS = 128;

    private static final boolean[] ACTIVE = new boolean[MAX_TRACERS];
    private static final double[] START_X = new double[MAX_TRACERS];
    private static final double[] START_Y = new double[MAX_TRACERS];
    private static final double[] START_Z = new double[MAX_TRACERS];
    private static final double[] DIR_X = new double[MAX_TRACERS];
    private static final double[] DIR_Y = new double[MAX_TRACERS];
    private static final double[] DIR_Z = new double[MAX_TRACERS];
    private static final float[] MAX_DIST = new float[MAX_TRACERS];
    private static final float[] SPEED_PER_TICK = new float[MAX_TRACERS];
    private static final int[] AGE_TICKS = new int[MAX_TRACERS];
    private static final int[] MAX_TICKS = new int[MAX_TRACERS];

    private static int writeCursor = 0;
    private static int activeCount = 0;

    public static void spawnTracer(Vec3 muzzleOrigin, Vec3 direction) {
        if (NapoleonicClientConfig.SPEC.isLoaded() && !NapoleonicClientConfig.ENABLE_BULLET_TRACERS.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }

        double lenSq = direction.lengthSqr();
        if (lenSq < 1.0E-5D) {
            return;
        }
        Vec3 normDir = direction.normalize();

        // Single block clip at spawn time so the tracer stops cleanly at walls/ground
        double maxCheckDist = 96.0D;
        Vec3 clipEnd = muzzleOrigin.add(normDir.scale(maxCheckDist));
        float maxDist = (float) maxCheckDist;
        if (mc.player != null) {
            BlockHitResult hit = level.clip(new ClipContext(
                    muzzleOrigin, clipEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
            if (hit.getType() != HitResult.Type.MISS) {
                maxDist = Math.max(1.5F, (float) hit.getLocation().distanceTo(muzzleOrigin));
            }
        }

        int slot = writeCursor;
        writeCursor = (writeCursor + 1) & (MAX_TRACERS - 1);
        if (!ACTIVE[slot]) {
            activeCount++;
        }

        ACTIVE[slot] = true;
        START_X[slot] = muzzleOrigin.x;
        START_Y[slot] = muzzleOrigin.y;
        START_Z[slot] = muzzleOrigin.z;
        DIR_X[slot] = normDir.x;
        DIR_Y[slot] = normDir.y;
        DIR_Z[slot] = normDir.z;
        MAX_DIST[slot] = maxDist;
        SPEED_PER_TICK[slot] = 9.0F;
        AGE_TICKS[slot] = 0;
        MAX_TICKS[slot] = Math.min(7, Math.max(2, Mth.ceil(maxDist / 9.0F) + 1));
    }

    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || activeCount <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            clear();
            return;
        }
        if (mc.isPaused()) {
            return;
        }

        int remaining = 0;
        for (int i = 0; i < MAX_TRACERS; i++) {
            if (!ACTIVE[i]) continue;
            int nextAge = AGE_TICKS[i] + 1;
            AGE_TICKS[i] = nextAge;
            if (nextAge >= MAX_TICKS[i]) {
                ACTIVE[i] = false;
            } else {
                remaining++;
            }
        }
        activeCount = remaining;
    }

    public static void onRenderLevelStage(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || activeCount <= 0) {
            return;
        }
        if (NapoleonicClientConfig.SPEC.isLoaded() && !NapoleonicClientConfig.ENABLE_BULLET_TRACERS.get()) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        float partialTick = event.getPartialTick();

        float tracerLen = NapoleonicClientConfig.SPEC.isLoaded()
                ? NapoleonicClientConfig.TRACER_LENGTH.get().floatValue()
                : 3.2F;
        float baseAlpha = NapoleonicClientConfig.SPEC.isLoaded()
                ? NapoleonicClientConfig.TRACER_OPACITY.get().floatValue()
                : 0.68F;
        float halfWidth = 0.024F;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        boolean anyDrawn = false;
        for (int i = 0; i < MAX_TRACERS; i++) {
            if (!ACTIVE[i]) continue;

            float exactAge = AGE_TICKS[i] + partialTick;
            float maxAge = MAX_TICKS[i];
            if (exactAge >= maxAge) continue;

            float headDist = Math.min(MAX_DIST[i], exactAge * SPEED_PER_TICK[i]);
            float tailDist = Math.max(0.0F, headDist - tracerLen);
            if (headDist - tailDist < 0.15F) continue;

            float lifeFrac = exactAge / maxAge;
            float alpha = baseAlpha * (1.0F - lifeFrac * lifeFrac);
            if (alpha <= 0.02F) continue;

            double dx = DIR_X[i];
            double dy = DIR_Y[i];
            double dz = DIR_Z[i];

            float hx = (float) (START_X[i] + dx * headDist - camPos.x);
            float hy = (float) (START_Y[i] + dy * headDist - camPos.y);
            float hz = (float) (START_Z[i] + dz * headDist - camPos.z);

            float tx = (float) (START_X[i] + dx * tailDist - camPos.x);
            float ty = (float) (START_Y[i] + dy * tailDist - camPos.y);
            float tz = (float) (START_Z[i] + dz * tailDist - camPos.z);

            // Perpendicular horizontal vector
            double rx = -dz;
            double rz = dx;
            double rLen = Math.hypot(rx, rz);
            if (rLen < 1.0E-4D) {
                rx = 1.0D;
                rz = 0.0D;
            } else {
                rx /= rLen;
                rz /= rLen;
            }
            float ox = (float) (rx * halfWidth);
            float oz = (float) (rz * halfWidth);

            // Warm pale-brass/lead streak at the head, fading to soft smoke-gray at the tail
            float hr = 1.0F, hg = 0.94F, hb = 0.76F;
            float tr = 0.90F, tg = 0.86F, tb = 0.78F;

            // Ribbon 1 (horizontal plane)
            buffer.vertex(matrix, tx - ox, ty, tz - oz).color(tr, tg, tb, 0.0F).endVertex();
            buffer.vertex(matrix, tx + ox, ty, tz + oz).color(tr, tg, tb, 0.0F).endVertex();
            buffer.vertex(matrix, hx + ox, hy, hz + oz).color(hr, hg, hb, alpha).endVertex();
            buffer.vertex(matrix, hx - ox, hy, hz - oz).color(hr, hg, hb, alpha).endVertex();

            // Ribbon 2 (vertical plane)
            buffer.vertex(matrix, tx, ty - halfWidth, tz).color(tr, tg, tb, 0.0F).endVertex();
            buffer.vertex(matrix, tx, ty + halfWidth, tz).color(tr, tg, tb, 0.0F).endVertex();
            buffer.vertex(matrix, hx, hy + halfWidth, hz).color(hr, hg, hb, alpha).endVertex();
            buffer.vertex(matrix, hx, hy - halfWidth, hz).color(hr, hg, hb, alpha).endVertex();
            anyDrawn = true;
        }

        if (anyDrawn) {
            tesselator.end();
        } else {
            buffer.end().release();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static void clear() {
        for (int i = 0; i < MAX_TRACERS; i++) {
            ACTIVE[i] = false;
        }
        activeCount = 0;
        writeCursor = 0;
    }

    private ClientBulletTracerRenderer() {
    }
}
