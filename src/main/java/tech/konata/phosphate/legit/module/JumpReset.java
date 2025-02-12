package tech.konata.phosphate.legit.module;

import tech.konata.phosphate.api.Extension;
import tech.konata.phosphate.api.PApi;
import tech.konata.phosphate.api.events.PlayerUpdateEvent;
import tech.konata.phosphate.api.events.SetEntityVelocityEvent;
import tech.konata.phosphate.api.events.TickEvent;
import tech.konata.phosphate.api.features.ExtensionModule;
import tech.konata.phosphate.api.interfaces.ICategory;
import tech.konata.phosphate.api.interfaces.settings.BooleanSetting;
import tech.konata.phosphate.api.interfaces.settings.NumberSetting;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author IzumiiKonata
 * Date: 2025/2/4 17:59
 */
public class JumpReset extends ExtensionModule {

    public JumpReset() {
        super("JumpReset", "Auto Jump Reset.", ICategory.Movement);
        super.addSettings(this.blatant, this.chance);
    }

    public BooleanSetting blatant = Extension.getAPI().createBooleanSetting("Blatant Mode", false);
    public NumberSetting<Float> chance = Extension.getAPI().createNumberSetting("Chance", 80.0f, 0.0f, 100.0f, 0.1f);


    private boolean canJump() {
        PApi api = Extension.getAPI();

        return Math.random() * 100.0f < this.chance.getValue() && api.getMinecraft().getLocalPlayer().isOnGround();
    }

    @Override
    public void onSetEntityVelocityEvent(SetEntityVelocityEvent event) {

        PApi api = Extension.getAPI();

        if (event.getEntityId() == api.getMinecraft().getLocalPlayer().getEntityId()) {

            if (event.isPre() && !blatant.getValue() && this.canJump()) {
                api.getGameSettings().getKeyJump().setPressed(true);
                final java.util.Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        api.getGameSettings().getKeyJump().setPressed(false);
                    }
                }, 50);
            }

            if (event.isPost() && blatant.getValue() && this.canJump()) {
                api.getMinecraft().getLocalPlayer().jump();
            }

        }

    }

    @Override
    public void onTickEvent(TickEvent event) {
        if (event.isPost())
            return;

        PApi api = Extension.getAPI();

        if (!blatant.getValue() && !api.getMinecraft().getLocalPlayer().isOnGround() && !api.getKeyboard().isKeyDown(api.getGameSettings().getKeyJump().getKeyCode())) {
            api.getGameSettings().getKeyJump().setPressed(false);
        }

    }
}
