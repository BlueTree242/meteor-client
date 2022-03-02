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
import org.jetbrains.annotations.Nullable;


import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ModuleControl {
    protected List<Module> disabledModules;
    public List<Module> originallyActiveModules;

    private ModuleControl() {}

    public static ModuleControl ofJson(@Nullable ModuleControl origin, JsonObject json) {
        String mode = json.get("mode") == null ? "BLACKLIST" : json.get("mode").getAsString();
        boolean removeOld = json.get("removeOld") != null && json.get("removeOld").getAsBoolean();
        ModuleControl control = origin;
        if (removeOld) {
            control = new ModuleControl();
            control.originallyActiveModules = new ArrayList<>();
            control.disabledModules = new ArrayList<>();
        }
        if (control == null) {
            control = new ModuleControl();
            control.originallyActiveModules = new ArrayList<>();
            control.disabledModules = new ArrayList<>();
        }
        control.disabledModules = new ArrayList<>();
        addModules(mode, json, control.disabledModules);
        control.originallyActiveModules = new ArrayList<>();
        for (Module module : control.disabledModules) {
            if (module != null && module.isActive()) {
                module.toggle();
                control.originallyActiveModules.add(module);
            }
        }
        boolean notify = json.get("notify") == null || json.get("notify").getAsBoolean();
        //What if server doesn't want them to know about it? trolls maybe
        if (notify)
            notifyUser(mode, control.disabledModules);

        if (removeOld && origin != null) {
            List<Module> originallyActiveModules = new ArrayList<>(origin.originallyActiveModules);
            origin.originallyActiveModules = new ArrayList<>();
            for (Module module : originallyActiveModules) {
                if (!control.originallyActiveModules.contains(module))
                    module.toggle();
            }
        }
        return control;
    }
    public ModuleControl handleJson(JsonObject json) {
        String mode = json.get("mode") == null ? "BLACKLIST" : json.get("mode").getAsString();
        boolean removeOld = json.get("removeOld") != null && json.get("removeOld").getAsBoolean();
        ModuleControl control = this;
        if (removeOld) {
            control = new ModuleControl();
            control.originallyActiveModules = new ArrayList<>();
            control.disabledModules = new ArrayList<>();
        }
        addModules(mode, json, control.disabledModules);
        for (Module module : control.disabledModules) {
            if (module.isActive()) {
                module.toggle();
                control.originallyActiveModules.add(module);
            }
        }
        boolean notify = json.get("notify") == null || json.get("notify").getAsBoolean();
        //What if server doesn't want them to know about it? trolls maybe
        if (notify)
            notifyUser(mode, disabledModules);
        return control;
    }

    private static List<String> convertModulesToNames(List<Module> modules) {
        List<String> result = new ArrayList<>();
        for (Module module : modules) {
            result.add(module.title);
        }
        return result;
    }

    private static void notifyUser(String mode, List<Module> disabledModules) {
        if (mode.equalsIgnoreCase("BLACKLIST")) {
            if (!disabledModules.isEmpty()) {
                ChatUtils.info("The Following module" +  (disabledModules.size() == 1 ? " has" : "s have") + " been disabled: " + Formatting.RED + String.join(Formatting.GRAY + ", " + Formatting.RED, convertModulesToNames(disabledModules)));
            }
        }else {
            if (!disabledModules.isEmpty()) {
                List<Module> notDisabled = new ArrayList<>(Modules.get().getList());
                notDisabled.removeAll(disabledModules);
                if (!notDisabled.isEmpty())
                ChatUtils.info("The Following module" + (disabledModules.size() == 1 ? " were" : "s were") + "n't disabled " + Formatting.RED + String.join(Formatting.GRAY + ", " + Formatting.RED, convertModulesToNames(notDisabled)) +
                    Formatting.GRAY + ". All Other Modules are disabled");
                else {
                    ChatUtils.info("All Modules were disabled.");
                }
            }
        }
        if (!disabledModules.isEmpty())
            mc.getToastManager().add(new MeteorToast(Items.NETHER_STAR, "Module" + (disabledModules.size() ==1 ? "" : "s") +" disabled", disabledModules.size() + " Module" + (disabledModules.size() == 1 ? " was" : "s were") +" disabled"));
    }

    private static List<Module> module(String name) {
        List<Module> result = new ArrayList<>();
        if (name.startsWith("category:")) {
            String finalName = name.replaceFirst("category:", "");
            for (Module module : Modules.get().getList()) {
                if (module.category.name.equalsIgnoreCase(finalName)) {
                    result.add(module);

                }
            }
        } else if (name.startsWith("contains:")) {
            String finalName = name.replaceFirst("contains:", "");
            for (Module module : Modules.get().getList()) {
                if (module.name.contains(finalName)) {
                    result.add(module);
                }
            }
        } else result.add(Modules.get().get(name));
        return result;
    }

    private static void addModules(String mode, JsonObject json, List<Module> disabledModules) {
        if (mode.equalsIgnoreCase("WHITELIST")) disabledModules.addAll(Modules.get().getList());
        for (JsonElement module : json.get("modules").getAsJsonArray()) {
            if (mode.equalsIgnoreCase("BLACKLIST"))
            disabledModules.addAll(module(module.getAsString()));
            else disabledModules.removeAll(module(module.getAsString()));
        }
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
