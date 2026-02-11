package com.jeff.horizon.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.util.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class KeyMapping extends net.minecraft.client.KeyMapping {
    private static final Map<String, KeyMapping> ALL = Maps.newHashMap();
    private static final Map<InputConstants.Key, KeyMapping> MAP = Maps.newHashMap();
    private static final Set<String> CATEGORIES = Sets.newHashSet();
    private static final Map<@Nullable Object, @Nullable Object>  CATEGORY_SORT_ORDER = Util.make(Maps.newHashMap(), (hashMap) -> {
        hashMap.put("key.categories.movement", 1);
        hashMap.put("key.categories.gameplay", 2);
        hashMap.put("key.categories.inventory", 3);
        hashMap.put("key.categories.creative", 4);
        hashMap.put("key.categories.multiplayer", 5);
        hashMap.put("key.categories.ui", 6);
        hashMap.put("key.categories.misc", 7);
        hashMap.put("key.category.nuit", 8);
    });
    private final String name;
    private final InputConstants.Key defaultKey;
    private final String category;
    private InputConstants.Key key;
    private boolean isDown;
    private int clickCount;
    public static final KeyMapping.Category NUIT_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("nuit", "category"));

    public KeyMapping(String string, InputConstants.Type type, int i, String string2) {
        super(string, type, i, NUIT_CATEGORY);
        this.name = string;
        this.key = type.getOrCreate(i);
        this.defaultKey = this.key;
        this.category = string2;
        ALL.put(string, this);
        MAP.put(this.key, this);
        CATEGORIES.add(string2);
    }

    public void setKey(InputConstants.Key key) {
        this.key = key;
    }

    public int compareTo(KeyMapping keyMapping) {
        return this.category.equals(keyMapping.category) ? I18n.get(this.name).compareTo(I18n.get(keyMapping.name)) : ((Integer) Objects.requireNonNull(CATEGORY_SORT_ORDER.get(this.category))).compareTo((Integer)CATEGORY_SORT_ORDER.get(keyMapping.category));
    }

    public boolean consumeClick() {
        if (this.clickCount == 0) {
            return false;
        } else {
            --this.clickCount;
            return true;
        }
    }
}
