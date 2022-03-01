/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;


import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ModuleControl {
    protected List<Module> disabledModules;
    public List<Module> originallyActiveModules;

    private ModuleControl() {}

    public static ModuleControl ofJson(JsonObject json) {
        ModuleControl control = new ModuleControl();
        control.disabledModules = new ArrayList<>();
        for (JsonElement module : json.get("modules").getAsJsonArray()) {
            control.disabledModules.add(Modules.get().get(module.getAsString()));
            //if the module doesn't exist, nothing
        }
        List<String> forciblyDisabled = new ArrayList<>();
        control.originallyActiveModules = new ArrayList<>();
        for (Module module : control.disabledModules) {
            if (module != null && module.isActive()) {
                module.toggle();
                forciblyDisabled.add(module.title);
                control.originallyActiveModules.add(module);
            }
        }
        boolean warn = json.get("warn") == null || json.get("warn").getAsBoolean();
        //What if server doesn't want them to know about it? trolls maybe
        if (warn)
            if (!forciblyDisabled.isEmpty()) {
                ChatUtils.info("The Following module" +  (forciblyDisabled.size() == 1 ? " has" : "s have") + " been deactivated: " + Formatting.RED + String.join(Formatting.GRAY + ", " + Formatting.RED, forciblyDisabled));
                mc.getToastManager().add(new MeteorToast(Items.CHEST, "Module" + (forciblyDisabled.size() ==1 ? "" : "s") +" Deactivated", forciblyDisabled.size() + " Module" + (forciblyDisabled.size() == 1 ? " was" : "s were") +" Deactivated"));
            }
        return control;
    }

    public ModuleControl handleJson(JsonObject json) {
        boolean removeOld = json.get("removeOld") != null && json.get("removeOld").getAsBoolean();
        ModuleControl control = this;
        if (removeOld) {
            control = new ModuleControl();
            control.originallyActiveModules = new ArrayList<>();
            control.disabledModules = new ArrayList<>();
        }
        for (JsonElement module : json.get("modules").getAsJsonArray()) {
            control.disabledModules.add(Modules.get().get(module.getAsString()));
            //if the module doesn't exist, nothing
        }
        List<String> forciblyDisabled = new ArrayList<>();
        for (Module module : control.disabledModules) {
            if (module.isActive()) {
                module.toggle();
                forciblyDisabled.add(module.title);
                control.originallyActiveModules.add(module);
            }
        }
        boolean warn = json.get("warn") == null || json.get("warn").getAsBoolean();
        //What if server doesn't want them to know about it? trolls maybe
        if (warn)
            if (!forciblyDisabled.isEmpty()) {
                ChatUtils.info("The Following module" +  (forciblyDisabled.size() == 1 ? " has" : "s have") + " been disabled: " + Formatting.RED + String.join(Formatting.GRAY + ", " + Formatting.RED, forciblyDisabled));
                mc.getToastManager().add(new MeteorToast(Items.CHEST, "Module" + (forciblyDisabled.size() ==1 ? "" : "s") +" Disabled", forciblyDisabled.size() + " Module" + (forciblyDisabled.size() == 1 ? " was" : "s were") +" Disabled"));
            }

        if (removeOld) {
            List<Module> originallyActiveModules = new ArrayList<>(this.originallyActiveModules);
            this.originallyActiveModules = new ArrayList<>();
            for (Module module : originallyActiveModules) {
                if (!control.originallyActiveModules.contains(module))
                module.toggle();
            }
        }
        return control;
    }

    public List<Module> getDisabledModules() {
        return disabledModules;
    }

    public List<Module> getOriginallyEnabledModules() {
        return originallyActiveModules;
    }

    protected void reActivateAll() {
        disabledModules = new ArrayList<>();
        for (Module originallyActiveModule : originallyActiveModules) {
            originallyActiveModule.toggle();
        }
    }
}
