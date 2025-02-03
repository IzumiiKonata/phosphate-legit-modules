package tech.konata.phosphate.legit.module;

import tech.konata.phosphate.api.Extension;
import tech.konata.phosphate.api.PApi;
import tech.konata.phosphate.api.enums.EnumMovingObjectType;
import tech.konata.phosphate.api.features.ExtensionModule;
import tech.konata.phosphate.api.interfaces.game.Minecraft;
import tech.konata.phosphate.api.interfaces.game.MovingObjectPosition;
import tech.konata.phosphate.api.interfaces.game.entity.Entity;
import tech.konata.phosphate.api.interfaces.game.entity.LivingEntity;
import tech.konata.phosphate.api.interfaces.game.entity.LocalPlayer;
import tech.konata.phosphate.api.interfaces.game.entity.Player;
import tech.konata.phosphate.api.interfaces.settings.BooleanSetting;
import tech.konata.phosphate.api.interfaces.settings.NumberSetting;
import tech.konata.phosphate.legit.utils.RotationUtils;

import java.util.List;
import java.util.Random;

/**
 * @author IzumiiKonata
 * Date: 2025/1/28 21:20
 */
public class AimAssist extends ExtensionModule {

    private final Random yawRandomizer = new Random();
    private final Random pitchRandomizer = new Random();
    NumberSetting<Double> horizontalSpeed = Extension.getAPI().createNumberSetting("Horizontal Speed", 5.0, 1.0, 20.0, 0.1);
    NumberSetting<Double> verticalSpeed = Extension.getAPI().createNumberSetting("Vertical Speed", 5.0, 1.0, 20.0, 0.1);
    NumberSetting<Double> distance = Extension.getAPI().createNumberSetting("Distance", 5.0, 1.0, 8.0, 0.1);
    NumberSetting<Float> fov = Extension.getAPI().createNumberSetting("Fov", 100.0f, 1.0f, 360.0f, 1.0f);
    BooleanSetting clickAim = Extension.getAPI().createBooleanSetting("Click Aim", true);
    BooleanSetting onTarget = Extension.getAPI().createBooleanSetting("Aim While On Target", true);
    BooleanSetting strafeInc = Extension.getAPI().createBooleanSetting("Strafe Increase", false);
    BooleanSetting checkBlockBreak = Extension.getAPI().createBooleanSetting("Check block break", false);
    private float modifiedPitch;
    private float targetYaw;
    private float verticalSpeedModifier;
    private int deltaYaw;
    private int refreshDelay;
    private boolean lastPitchBelowZero;
    private float modifiedYaw;
    private boolean aimthreadCreated;
    private double randomDelay;
    private float targetPitch;
    private boolean lastNegativeYaw;
    private double lastDeltaX;
    private double lastDeltaZ;
    private int yawUpdateCounter;
    private int blockBreakDelay;
    private double cYaw;
    private float yawMod;
    private int randomPitchModifier;
    private int pitchUpdateCounter;
    private float horizontalSpeedModifier;
    private int randomYawModifier;
    private float pitchMod;
    private int deltaPitch;
    private LivingEntity target;

    public AimAssist() {
        super("Aim Assist", "Auto aims 4 u.", Extension.getAPI().createCategory("Combat"));
        super.addSettings(this.horizontalSpeed, this.verticalSpeed, this.distance, this.fov, this.clickAim, this.onTarget, this.strafeInc, this.checkBlockBreak);

        this.horizontalSpeedModifier = 0.0F;
        this.verticalSpeedModifier = 0.0F;
        this.target = null;
        this.blockBreakDelay = 0;
    }

    public double getInterpolatedX(LivingEntity entity) {
        float tick = Extension.getAPI().getMinecraft().getRenderPartialTicks();
        return entity.getLastTickPosX() + (entity.getX() - entity.getLastTickPosX()) * (double) tick;
    }

    public double getInterpolatedZ(LivingEntity entity) {
        float tick = Extension.getAPI().getMinecraft().getRenderPartialTicks();
        return entity.getLastTickPosZ() + (entity.getZ() - entity.getLastTickPosZ()) * (double) tick;
    }

    public double randDouble(Random rand, double min, double max) {
        return min + (max - min) * rand.nextDouble();
    }

    private void aimTarget() {
        this.updateRandomVariables();
        double deltaX = this.getInterpolatedX(this.target);
        double deltaZ = this.getInterpolatedZ(this.target);
        double vX = deltaX - this.lastDeltaX;
        double vZ = deltaZ - this.lastDeltaZ;
        this.lastDeltaX = deltaX;
        this.lastDeltaZ = deltaZ;

        PApi api = Extension.getAPI();
        Minecraft mc = api.getMinecraft();

        LocalPlayer playerSP = mc.getLocalPlayer();
        double eyeHeight = 1.7;
        double tYaw = RotationUtils.yawDiff(this.getInterpolatedX(playerSP), this.getInterpolatedZ(playerSP), playerSP.getRotationYaw(), deltaX + vX * eyeHeight, deltaZ + vZ * eyeHeight);
        boolean isNegYaw = RotationUtils.isYawNegative(this.getInterpolatedX(playerSP), this.getInterpolatedZ(playerSP), playerSP.getRotationYaw(), deltaX + vX * eyeHeight, deltaZ + vZ * eyeHeight);
        int tPitch = Math.abs(RotationUtils.calcPitch(playerSP, this.target)) - 10;
        float yawRandomModifier = 1.0F;
        float pitchRandomModifier = 1.0F;
        yawRandomModifier = (float) ((double) yawRandomModifier + this.randDouble(this.yawRandomizer, 0.0, 2.0));
        yawRandomModifier = (float) ((double) yawRandomModifier + tYaw / 50.0);
        if (Math.abs(tYaw - this.cYaw) > 6.0) {
            yawRandomModifier = (float) ((double) yawRandomModifier + tYaw / 35.0);
        }

        double randDist = ((9.0F - playerSP.getDistanceToPosition(this.target.getX(), this.target.getY(), this.target.getZ())) / 2.5F - 2.0F);
        randDist = Math.max(0.0, randDist);
        yawRandomModifier = (float) ((double) yawRandomModifier + randDist);
        if (this.strafeInc.getValue() && (!isNegYaw && playerSP.getMoveStrafing() > 0.0F || isNegYaw && playerSP.getMoveStrafing() < 0.0F)) {
            yawRandomModifier = (float) ((double) yawRandomModifier * 1.6);
        }

        if (playerSP.getDistanceToPosition(this.target.getX(), this.target.getY(), this.target.getZ()) < 0.5F) {
            yawRandomModifier /= 5.0F;
        }

        yawRandomModifier /= 90.0F;
        pitchRandomModifier /= 90.0F;
        float yawModifier = isNegYaw ? -yawRandomModifier : yawRandomModifier;
        boolean pitchBelowZero = RotationUtils.isPitchBelowZero(playerSP, this.target);
        float pitchModifier = pitchBelowZero ? pitchRandomModifier : -pitchRandomModifier;
        if (tYaw < 5.0) {
            yawModifier = 0.0F;
            this.modifiedYaw *= 0.7F;
            if (isNegYaw && playerSP.getMoveStrafing() > 0.0F || !isNegYaw && playerSP.getMoveStrafing() < 0.0F) {
                this.modifiedYaw *= 0.5F;
            }

        }

        if (isNegYaw != this.lastNegativeYaw) {
            this.modifiedYaw = -this.modifiedYaw;
            this.yawMod = -this.yawMod;
            this.targetYaw = 0.0F;
        }

        if (pitchBelowZero != this.lastPitchBelowZero) {
            this.modifiedPitch = -this.modifiedPitch;
            this.pitchMod = -this.pitchMod;
            this.targetPitch = 0.0F;
        }

        if (tPitch < 5) {
            pitchModifier = 0.0F;
            this.modifiedPitch *= 0.7F;
        }

        this.yawMod += yawModifier;
        this.pitchMod += pitchModifier;
        yawModifier = this.modifiedYaw;
        pitchModifier = this.modifiedPitch;
        if (Math.abs(yawModifier) > 10.0F) {
            this.yawMod = 0.0F;
            this.modifiedYaw = 0.0F;
        } else {
            float yawAdj = yawModifier * 0.15F;
            if (tYaw <= 9.0) {
                yawAdj = (float) ((double) yawAdj / (10.0 - tYaw));
            }

            if (Float.isNaN(yawAdj)) {
                this.yawMod = 0.0F;
                this.modifiedYaw = 0.0F;
            } else {
                this.adjustYaw(yawAdj);
                float pitchAdj = (float) ((double) pitchModifier * 0.15);
                if (Float.isNaN(pitchAdj)) {
                    this.pitchMod = 0.0F;
                    this.modifiedPitch = 0.0F;
                    return;
                }

                this.adjustPitch(pitchAdj);

                this.lastPitchBelowZero = pitchBelowZero;
                this.lastNegativeYaw = isNegYaw;
                ++this.yawUpdateCounter;
                if (this.yawUpdateCounter > 10) {
                    this.cYaw = tYaw;
                    this.yawUpdateCounter = 0;
                }
            }
        }
    }

    void adjustYaw(float yawAdjustment) {
        if (yawAdjustment != 0.0F) {
            yawAdjustment *= 5.0F;
            float hs = this.horizontalSpeed.getValue().floatValue();
            float yawDiff = (float) RotationUtils.calculateRelativeAngle(Extension.getAPI().getMinecraft().getLocalPlayer(), this.target);
            if (yawDiff <= 10.0F) {
                this.horizontalSpeedModifier = hs;
            }

            if (this.horizontalSpeedModifier > 0.0F) {
                hs -= this.horizontalSpeedModifier / 3.0F;
                this.horizontalSpeedModifier -= yawDiff / 200.0F;
            }

            float adj = 1.0F * hs * yawAdjustment;
            this.targetYaw += adj;
        } else {
            this.targetYaw = 0.0F;
        }
    }

    void adjustPitch(float pitchAdjustment) {
        if (pitchAdjustment != 0.0F) {
            pitchAdjustment *= 5.0F;
            float vs = this.verticalSpeed.getValue().floatValue() + 20;
            float pitchDiff = (float) RotationUtils.calcPitch(Extension.getAPI().getMinecraft().getLocalPlayer(), this.target);
            if (pitchDiff <= 10.0F) {
                this.verticalSpeedModifier = vs;
            }

            if (this.verticalSpeedModifier > 0.0F) {
                vs -= this.verticalSpeedModifier / 3.0F;
                this.verticalSpeedModifier -= pitchDiff / 200.0F;
            }

            float adj = 1.0F * vs * pitchAdjustment;
            this.targetPitch += adj;
        } else {
            this.targetPitch = 0.0F;
        }
    }

    public int getRandomInRangeRange(Random random, int min, int max) {
        return random.nextInt(max - min) + min;
    }

    private void updateRandomVariables() {
        ++this.randomDelay;
        if (this.randomDelay >= (double) (250 + this.pitchRandomizer.nextInt(50))) {
            this.randomDelay = getRandomInRangeRange(this.pitchRandomizer, -100, -50);
            this.randomYawModifier = getRandomInRangeRange(this.pitchRandomizer, -1, 2);
            this.randomPitchModifier = getRandomInRangeRange(this.pitchRandomizer, -1, 2);
        }

        int yawRand = this.randomYawModifier;
        int pitchRand = this.randomPitchModifier;

        // spin seeds
        this.pitchRandomizer.nextInt(10);
        this.pitchRandomizer.nextInt(10);

        if (this.pitchRandomizer.nextInt(10) < 2) {
            yawRand = 0;
        }

        if (this.pitchRandomizer.nextInt(10) < 2) {
            pitchRand = 0;
        }

        if (this.randomDelay < 0.0) {
            yawRand = 0;
            pitchRand = 0;
        }

        if (this.pitchRandomizer.nextInt(20) == 1) {
            this.deltaYaw += yawRand;
            this.deltaPitch += pitchRand;
        }

        if (this.targetYaw > 0.0F && this.deltaYaw < 0 || this.targetYaw < 0.0F && this.deltaYaw > 0) {
            this.deltaYaw = 0;
        }
    }

    void resetVars() {
        this.targetYaw = 0.0F;
        this.targetPitch = 0.0F;
        this.randomYawModifier = 0;
        this.randomPitchModifier = 0;
        this.deltaYaw = 0;
        this.deltaPitch = 0;
    }

    private boolean canAim() {
        if (Extension.getAPI().getMinecraft().getLocalPlayer() == null || !Extension.getAPI().getMinecraft().isInWorld()) {
            return false;
        } else {
            if (this.checkBlockBreak.getValue()) {
                MovingObjectPosition mouseOver = Extension.getAPI().getMinecraft().getMouseOver();
                if (mouseOver != null && mouseOver.getTypeOfHit() == EnumMovingObjectType.BLOCK) {
                    this.blockBreakDelay = 250;
                    return false;
                }

                if (this.blockBreakDelay > 0) {
                    --this.blockBreakDelay;
                }

                return this.blockBreakDelay <= 0;
            }

            return true;
        }
    }

    @Override
    public void onEnabled() {
        if (!this.aimthreadCreated) {
            this.aimthreadCreated = true;
            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1L);
                        if (isEnabled()) {
                            processAim();
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void processAim() {

        PApi api = Extension.getAPI();
        Minecraft mc = api.getMinecraft();

        if (mc.isInWorld() && mc.getLocalPlayer() != null) {
            if (!this.canAim()) {
                this.resetVars();
            } else {

                if (this.clickAim.getValue() && !api.getGameSettings().getKeyAttack().isKeyDown()) {
                    this.target = null;
                    this.resetVars();
                } else {
                    if (this.target != null && (RotationUtils.entityAlive(this.target) || mc.getLocalPlayer().getDistanceToPosition(this.target.getX(), this.target.getY(), this.target.getZ()) > this.distance.getValue())) {
                        this.resetVars();
                        this.target = null;
                    }

                    if (this.clickAim.getValue() && api.getGameSettings().getKeyAttack().isKeyDown() && this.target == null || !this.clickAim.getValue()) {
                        LivingEntity target = this.getTarget();
                        if (!this.clickAim.getValue()) {
                            ++this.refreshDelay;
                            if (this.refreshDelay > 700 || this.target == null) {
                                this.target = target;
                                this.refreshDelay = 0;
                            }
                        } else {
                            this.target = target;
                        }
                    }

                    if (mc.isInWorld()) {
                        if (this.target != null) {
                            this.updateAimModifiers();
                            this.aimTarget();
                        } else {
                            this.resetVars();
                        }
                    }
                }
            }
        }
    }

    void updateAimModifiers() {
        ++this.pitchUpdateCounter;
        if (this.pitchUpdateCounter > 10) {
            this.modifiedPitch = this.pitchMod;
            this.modifiedYaw = this.yawMod;
            this.yawMod = 0.0F;
            this.pitchMod = 0.0F;
            this.pitchUpdateCounter = 0;
        }
    }

    @Override
    public void onUpdateCameraAndRenderEvent() {
        if (Extension.getAPI().getMinecraft().isInWorld()) {
            float mouseSensitivity = Extension.getAPI().getGameSettings().getMouseSensitivity();
            this.targetYaw += (float) this.deltaYaw;
            this.targetPitch += (float) this.deltaPitch;
            int oYaw = (int) this.targetYaw;
            int oPitch = (int) (-this.targetPitch);
            float factor1 = mouseSensitivity * 0.6F + 0.2F;
            float f3d = factor1 * factor1 * factor1 * 8.0F;
            float yaw = (float) oYaw * f3d;
            float pitch = (float) oPitch * f3d;
            RotationUtils.setRotation(yaw, pitch);
            this.targetYaw = 0.0F;
            this.targetPitch = 0.0F;
            this.deltaYaw = 0;
            this.deltaPitch = 0;
        }
    }

    private boolean noAction() {
        return clickAim.getValue() && !Extension.getAPI().getGameSettings().getKeyAttack().isKeyDown();
    }

    private Player getTarget() {

        PApi api = Extension.getAPI();
        Minecraft mc = api.getMinecraft();

        final List<? extends Entity> players = mc.getWorld().getLoadedPlayers();

        Player target = null;
        double maxAng = 360.0;
        for (final Entity entity : players) {

            if (!(entity instanceof Player))
                continue;

            Player entityPlayer = (Player) entity;

            if (entityPlayer != mc.getLocalPlayer()) {
                double dist = mc.getLocalPlayer().getDistanceToPosition(entityPlayer.getX(), entityPlayer.getY(), entityPlayer.getZ());
                if (dist > distance.getValue())
                    continue;

                double ang = RotationUtils.calculateRelativeAngle(mc.getLocalPlayer(), entityPlayer);
                if (ang < maxAng && ang <= this.fov.getValue() / 2.0) {
                    maxAng = ang;
                    target = entityPlayer;
                }
            }
        }
        return target;
    }

}
