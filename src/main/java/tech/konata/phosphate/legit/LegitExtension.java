package tech.konata.phosphate.legit;

import tech.konata.phosphate.api.Extension;
import tech.konata.phosphate.api.PApi;
import tech.konata.phosphate.legit.module.AimAssist;
import tech.konata.phosphate.legit.module.AutoClicker;
import tech.konata.phosphate.legit.module.Eagle;
import tech.konata.phosphate.legit.module.JumpReset;

/**
 * @author IzumiiKonata
 * Date: 2025/1/27 21:26
 */
public class LegitExtension extends Extension {

    @Override
    public void onLoad(PApi pApi) {
        System.out.println("Legit extension is loaded!");

        // 注册模块
        pApi.registerModule(new AutoClicker());
        pApi.registerModule(new AimAssist());
        pApi.registerModule(new Eagle());
        pApi.registerModule(new JumpReset());
    }

    @Override
    public void onStop(PApi pApi) {
    }

    @Override
    public String getName() {
        return "Legit Extension";
    }

    @Override
    public String getVersion() {
        return "0.4";
    }
}
