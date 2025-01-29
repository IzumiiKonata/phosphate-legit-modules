package tech.konata.phosphate.legit.utils;

import tech.konata.phosphate.api.Extension;
import tech.konata.phosphate.api.interfaces.game.entity.Entity;
import tech.konata.phosphate.api.interfaces.game.entity.LivingEntity;
import tech.konata.phosphate.api.interfaces.game.entity.LocalPlayer;

/**
 * @author IzumiiKonata
 * Date: 2025/1/29 12:02
 */
public class RotationUtils {

    public static double yawDiff(double x1, double z1, double yaw, double x2, double z2) {
        double result = 0.0;
        double xDiff = x2 - x1;
        double zDiff = z2 - z1;
        if (zDiff > 0.0 && xDiff > 0.0) {
            result = Math.toDegrees(-Math.atan(xDiff / zDiff));
        } else if (zDiff > 0.0 && xDiff < 0.0) {
            result = Math.toDegrees(-Math.atan(xDiff / zDiff));
        } else if (zDiff < 0.0 && xDiff > 0.0) {
            result = -90.0 + Math.toDegrees(Math.atan(zDiff / xDiff));
        } else if (zDiff < 0.0 && xDiff < 0.0) {
            result = 90.0 + Math.toDegrees(Math.atan(zDiff / xDiff));
        }

        double mod = Math.abs(result - yaw) % 360.0;
        return mod > 180.0 ? 360.0 - mod : mod;
    }

    public static boolean isYawNegative(double x1, double z1, double Yaw, double x2, double z2) {
        double result = 0.0;
        double xDiff = x2 - x1;
        double zDiff = z2 - z1;
        if (zDiff > 0.0 && xDiff > 0.0) {
            result = Math.toDegrees(-Math.atan(xDiff / zDiff));
        } else if (zDiff > 0.0 && xDiff < 0.0) {
            result = Math.toDegrees(-Math.atan(xDiff / zDiff));
        } else if (zDiff < 0.0 && xDiff > 0.0) {
            result = -90.0 + Math.toDegrees(Math.atan(zDiff / xDiff));
        } else if (zDiff < 0.0 && xDiff < 0.0) {
            result = 90.0 + Math.toDegrees(Math.atan(zDiff / xDiff));
        }

        int toRad = (int) toRad((result - Yaw) % 360.0);
        return toRad < 0;
    }

    public static double toRad(double yaw) {
        yaw %= 360.0;
        if (yaw >= 180.0) {
            yaw -= 360.0;
        }

        if (yaw < -180.0) {
            yaw += 360.0;
        }

        return yaw;
    }

    public static int calcPitch(LivingEntity from, LivingEntity to) {
        double diffX = to.getX() - from.getX();
        double diffY = to.getY() - 0.3 - from.getY();
        double diffZ = to.getZ() - from.getZ();

        double diff = sqrt_float(diffX * diffX + diffZ * diffZ);
        float deltaY = (float) (-(Math.atan2(diffY, diff) * 180.0 / Math.PI));
        float result = (float) toRad(from.getRotationPitch() - deltaY);
        return (int) result;
    }

    public static float sqrt_float(double var0) {
        return (float) Math.sqrt(var0);
    }

    public static boolean isPitchBelowZero(LivingEntity from, LivingEntity to) {
        double diffX = to.getX() - from.getX();
        double diffY = to.getY() - 0.3 - from.getY();
        double diffZ = to.getZ() - from.getZ();

        double difference = sqrt_float(diffX * diffX + diffZ * diffZ);
        float deltaY = (float) (-(Math.atan2(diffY, difference) * 180.0 / Math.PI));
        float result = (float) toRad(from.getRotationPitch() - deltaY);
        return result < 0.0F;
    }

    public static int calculateRelativeAngle(Entity from, Entity to) {
        double diff = 0.0;
        double xDiff = to.getX() - from.getX();
        double zDiff = to.getZ() - from.getZ();
        if (zDiff > 0.0 && xDiff > 0.0) {
            diff = Math.toDegrees(-Math.atan(xDiff / zDiff));
        } else if (zDiff > 0.0 && xDiff < 0.0) {
            diff = Math.toDegrees(-Math.atan(xDiff / zDiff));
        } else if (zDiff < 0.0 && xDiff > 0.0) {
            diff = -90.0 + Math.toDegrees(Math.atan(zDiff / xDiff));
        } else if (zDiff < 0.0 && xDiff < 0.0) {
            diff = 90.0 + Math.toDegrees(Math.atan(zDiff / xDiff));
        }

        int result = (int) (Math.abs(diff - (double) from.getRotationYaw()) % 360.0);
        return result > 180 ? 360 - result : result;
    }

    public static void setRotation(float yaw, float pitch) {
        LocalPlayer lp = Extension.getAPI().getMinecraft().getLocalPlayer();
        float var3 = lp.getRotationPitch();
        float var4 = lp.getRotationYaw();
        lp.setYaw((float) ((double) lp.getRotationYaw() + (double) yaw * 0.15));
        lp.setPitch((float) ((double) lp.getRotationPitch() - (double) pitch * 0.15));
        if (lp.getRotationPitch() < -90.0F) {
            lp.setPitch(-90.0F);
        }

        if (lp.getRotationPitch() > 90.0F) {
            lp.setPitch(90.0F);
        }

        lp.setPrevRotationPitch(lp.getPrevRotationPitch() + lp.getRotationPitch() - var3);
        lp.setPrevRotationYaw(lp.getPrevRotationYaw() + lp.getRotationYaw() - var4);
    }

    public static boolean entityAlive(LivingEntity ent) {
        return ent.getHealth() <= 0.0F;
    }

}
