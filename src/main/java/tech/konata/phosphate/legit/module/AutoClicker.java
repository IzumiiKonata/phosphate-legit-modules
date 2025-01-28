package tech.konata.phosphate.legit.module;

import org.lwjgl.glfw.GLFW;
import tech.konata.phosphate.api.Extension;
import tech.konata.phosphate.api.PApi;
import tech.konata.phosphate.api.enums.EnumKeybind;
import tech.konata.phosphate.api.events.AttackEvent;
import tech.konata.phosphate.api.events.TickEvent;
import tech.konata.phosphate.api.features.ExtensionModule;
import tech.konata.phosphate.api.interfaces.game.entity.LocalPlayer;
import tech.konata.phosphate.api.interfaces.settings.BooleanSetting;
import tech.konata.phosphate.api.interfaces.settings.NumberSetting;
import tech.konata.phosphate.legit.utils.Timer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author IzumiiKonata
 * Date: 2025/1/28 10:51
 */
public class AutoClicker extends ExtensionModule {

    public AutoClicker() {
        super("Auto Clicker", "Auto clicks for u.", Extension.getAPI().createCategory("Combat"));
        super.addSettings(this.minCPS, this.maxCPS, this.leftClick, this.rightClick, this.hitSelect);

        this.minCPS.setValueCallback(min -> {

            if (min > maxCPS.getValue()) {
                this.minCPS.setValue(maxCPS.getValue());
            }

        });

        this.maxCPS.setValueCallback(max -> {
            if (max < this.minCPS.getValue()) {
                this.maxCPS.setValue(this.minCPS.getValue());
            }
        });
    }

    private final NumberSetting<Integer> minCPS = Extension.getAPI().createNumberSetting("Minimum CPS",  6, 1, 20, 1);
    private final NumberSetting<Integer> maxCPS = Extension.getAPI().createNumberSetting("Maximum CPS",  8, 1, 20, 1);

    private final BooleanSetting leftClick = Extension.getAPI().createBooleanSetting("Left Click", true);
    private final BooleanSetting rightClick = Extension.getAPI().createBooleanSetting("Right Click", false);
    private final BooleanSetting hitSelect = Extension.getAPI().createBooleanSetting("Hit Select", false);

    Timer delayTimer = new Timer();
    private int ticksDown, attackTicks;
    private long nextSwing;

    @Override
    public void onAttackEvent(AttackEvent event) {
        this.attackTicks = 0;
    }

    @Override
    public void onTickEvent(TickEvent event) {

        if (event.isPost())
            return;

        PApi api = Extension.getAPI();
        LocalPlayer thePlayer = api.getMinecraft().getLocalPlayer();

        this.attackTicks++;

        if (delayTimer.isDelayed(this.nextSwing) && (!hitSelect.getValue() || ((hitSelect.getValue() && attackTicks >= 10) || (thePlayer.getHurtTime() > 0 && delayTimer.isDelayed(this.nextSwing))))) {
            final long clicks = (long) (Math.round(this.getRandom(this.minCPS.getValue(), this.maxCPS.getValue())) * 1.5);

            if (api.getGameSettings().isPressed(EnumKeybind.ATTACK)) {
                ticksDown++;
            } else {
                ticksDown = 0;
            }

            this.nextSwing = 1000 / clicks;

            if (rightClick.getValue() && api.getGameSettings().isPressed(EnumKeybind.USE_ITEM) && !api.getGameSettings().isPressed(EnumKeybind.ATTACK)) {
                api.getMinecraft().getLocalPlayer().rightClickMouse();

                if (Math.random() > 0.9) {
                    api.getMinecraft().getLocalPlayer().rightClickMouse();
                }
            }

            if (leftClick.getValue() && ticksDown > 1 && (Math.sin(nextSwing) + 1 > Math.random() || Math.random() > 0.25 || delayTimer.isDelayed(4 * 50)) && !api.getGameSettings().isPressed(EnumKeybind.USE_ITEM)) {
                api.getMinecraft().getLocalPlayer().clickMouse();
            }

            this.delayTimer.reset();
        }
    }

    public double getRandom(double min, double max) {
        if (min == max) {
            return min;
        } else if (min > max) {
            final double d = min;
            min = max;
            max = d;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
