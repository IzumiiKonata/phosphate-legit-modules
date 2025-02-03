package tech.konata.phosphate.legit.module;

import tech.konata.phosphate.api.Extension;
import tech.konata.phosphate.api.PApi;
import tech.konata.phosphate.api.events.SafeWalkEvent;
import tech.konata.phosphate.api.events.TickEvent;
import tech.konata.phosphate.api.features.ExtensionModule;
import tech.konata.phosphate.api.interfaces.ICategory;
import tech.konata.phosphate.api.interfaces.game.Minecraft;
import tech.konata.phosphate.api.interfaces.input.Keyboard;
import tech.konata.phosphate.api.interfaces.settings.EnumSetting;

/**
 * @author IzumiiKonata
 * Date: 2025/2/3 14:08
 */
public class Scaffold extends ExtensionModule {

    public Scaffold() {
        super("Scaffold", "安全行走.", ICategory.Movement);

        super.addSettings(this.mode);

    }

    public EnumSetting<Mode> mode = Extension.getAPI().createEnumSetting("Mode", Mode.Eagle);

    public enum Mode {
        Eagle,
        SafeWalk
    }

    @Override
    public void onSafeWalkEvent(SafeWalkEvent event) {
        if (this.mode.getValue() == Mode.SafeWalk)
            event.setSafeWalk(true);
    }

    @Override
    public void onTickEvent(TickEvent event) {

        if (event.isPost())
            return;

        PApi api = Extension.getAPI();
        Keyboard kb = api.getKeyboard();

        if (this.mode.getValue() == Mode.Eagle) {

            boolean bCanEagle = this.canEagle(0.01);

            if (bCanEagle) {
                api.getGameSettings().getKeySneak().setPressed(true);
            } else {

                if (!kb.isKeyDown(api.getGameSettings().getKeySneak().getKeyCode())) {
                    api.getGameSettings().getKeySneak().setPressed(false);
                }

            }

        }

    }

    @Override
    public void onDisabled() {
        if (this.mode.getValue() == Mode.Eagle) {
            PApi api = Extension.getAPI();
            api.getGameSettings().getKeySneak().setPressed(false);
        }
    }

    public boolean canEagle(final double n) {

        PApi api = Extension.getAPI();
        Minecraft mc = api.getMinecraft();

        double sensitivity = 0.225;

        if (mc.getWorld().getCollidingBoundingBoxes(mc.getLocalPlayer(), mc.getLocalPlayer().getEntityBoundingBox().offset(sensitivity, -n, 0.0)).isEmpty())
            return true;

        if (mc.getWorld().getCollidingBoundingBoxes(mc.getLocalPlayer(), mc.getLocalPlayer().getEntityBoundingBox().offset(-sensitivity, -n, 0.0)).isEmpty())
            return true;

        if (mc.getWorld().getCollidingBoundingBoxes(mc.getLocalPlayer(), mc.getLocalPlayer().getEntityBoundingBox().offset(sensitivity, -n, sensitivity)).isEmpty())
            return true;

        if (mc.getWorld().getCollidingBoundingBoxes(mc.getLocalPlayer(), mc.getLocalPlayer().getEntityBoundingBox().offset(sensitivity, -n, -sensitivity)).isEmpty())
            return true;

        return false;
        
    }

}
