package ru.funtime.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

public final class FunTimeItems extends JavaPlugin implements Listener {
    private NamespacedKey itemId;
    private final Map<String, ItemSpec> specs = new LinkedHashMap<>();

    private record ItemSpec(String id, String name, Material material, List<String> lore,
                            Map<Enchantment, Integer> enchants, PotionEffectType effect, int effectLevel,
                            String[] shape, Map<Character, Material> ingredients) {}

    @Override public void onEnable() {
        itemId = new NamespacedKey(this, "custom_item");
        registerSpecs();
        registerRecipes();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, this::applyTalismanEffects, 20L, 40L);
        getLogger().info("FunTimeItems enabled: /ftitems");
    }

    private void add(String id, String name, Material material, List<String> lore,
                     Map<Enchantment,Integer> enchants, PotionEffectType effect, int level,
                     String[] shape, Map<Character,Material> ingredients) {
        specs.put(id, new ItemSpec(id, name, material, lore, enchants, effect, level, shape, ingredients));
    }
    private Map<Enchantment,Integer> ench(Enchantment e, int n) { return Map.of(e,n); }
    private Map<Character,Material> ing(Object... values) {
        Map<Character,Material> m = new HashMap<>();
        for (int i=0;i<values.length;i+=2) m.put((Character)values[i], (Material)values[i+1]);
        return m;
    }

    private void registerSpecs() {
        add("crusher_pickaxe", "§6Кирка Крушителя", Material.NETHERITE_PICKAXE,
                List.of("§7Особая кирка в стиле FunTime", "§eЭффективность и прочность усилены"),
                Map.of(Enchantment.EFFICIENCY, 6, Enchantment.UNBREAKING, 4, Enchantment.FORTUNE, 3), null, 0,
                new String[]{"DDD"," S "," S "}, ing('D',Material.DIAMOND,'S',Material.STICK));
        add("crusher_shovel", "§6Лопата Крушителя", Material.NETHERITE_SHOVEL,
                List.of("§7Особая лопата в стиле FunTime"),
                Map.of(Enchantment.EFFICIENCY, 6, Enchantment.UNBREAKING, 4), null, 0,
                new String[]{" D "," S "," S "}, ing('D',Material.DIAMOND,'S',Material.STICK));
        add("crusher_hoe", "§6Мотыга Крушителя", Material.NETHERITE_HOE,
                List.of("§7Особая мотыга в стиле FunTime"),
                Map.of(Enchantment.EFFICIENCY, 5, Enchantment.UNBREAKING, 4), null, 0,
                new String[]{"DD "," S "," S "}, ing('D',Material.DIAMOND,'S',Material.STICK));
        add("crusher_helmet", "§6Шлем Крушителя", Material.NETHERITE_HELMET, List.of("§7Броня Крушителя"),
                Map.of(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 4), null, 0,
                new String[]{"DDD","D D"}, ing('D',Material.DIAMOND));
        add("crusher_chestplate", "§6Нагрудник Крушителя", Material.NETHERITE_CHESTPLATE, List.of("§7Броня Крушителя"),
                Map.of(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 4), null, 0,
                new String[]{"D D","DDD","DDD"}, ing('D',Material.DIAMOND));
        add("crusher_leggings", "§6Поножи Крушителя", Material.NETHERITE_LEGGINGS, List.of("§7Броня Крушителя"),
                Map.of(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 4), null, 0,
                new String[]{"DDD","D D","D D"}, ing('D',Material.DIAMOND));
        add("crusher_boots", "§6Ботинки Крушителя", Material.NETHERITE_BOOTS, List.of("§7Броня Крушителя"),
                Map.of(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 4, Enchantment.FEATHER_FALLING, 4), null, 0,
                new String[]{"D D","D D"}, ing('D',Material.DIAMOND));

        talisman("crusher_talisman", "§6Талисман Крушителя", Material.TOTEM_OF_UNDYING, PotionEffectType.STRENGTH, 0, Material.NETHERITE_INGOT, Material.DIAMOND);
        talisman("punisher_talisman", "§cТалисман Карателя", Material.TOTEM_OF_UNDYING, PotionEffectType.STRENGTH, 1, Material.BLAZE_POWDER, Material.IRON_INGOT);
        talisman("discord_talisman", "§5Талисман Раздора", Material.TOTEM_OF_UNDYING, PotionEffectType.HASTE, 0, Material.AMETHYST_SHARD, Material.GOLD_INGOT);
        talisman("tyrant_talisman", "§4Талисман Тирана", Material.TOTEM_OF_UNDYING, PotionEffectType.RESISTANCE, 0, Material.IRON_BLOCK, Material.REDSTONE);
        talisman("wrath_talisman", "§cТалисман Ярости", Material.TOTEM_OF_UNDYING, PotionEffectType.STRENGTH, 1, Material.BLAZE_ROD, Material.REDSTONE);
        talisman("vortex_talisman", "§bТалисман Вихря", Material.TOTEM_OF_UNDYING, PotionEffectType.SPEED, 1, Material.ENDER_PEARL, Material.FEATHER);
        talisman("gloom_talisman", "§8Талисман Мрака", Material.TOTEM_OF_UNDYING, PotionEffectType.RESISTANCE, 0, Material.OBSIDIAN, Material.COAL);
        talisman("demon_talisman", "§4Талисман Демона", Material.TOTEM_OF_UNDYING, PotionEffectType.FIRE_RESISTANCE, 0, Material.MAGMA_CREAM, Material.BLAZE_POWDER);
    }

    private void talisman(String id, String name, Material material, PotionEffectType effect, int level, Material center, Material side) {
        add(id, name, material, List.of("§7Держи в инвентаре для эффекта", "§8Авторский набор, вдохновлённый FunTime"),
                Map.of(), effect, level, new String[]{"SES","SCS","SES"}, ing('S',side,'E',Material.EMERALD,'C',center));
    }

    private ItemStack make(ItemSpec spec) {
        ItemStack stack = new ItemStack(spec.material());
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(spec.name());
        meta.setLore(spec.lore());
        meta.getPersistentDataContainer().set(itemId, PersistentDataType.STRING, spec.id());
        spec.enchants().forEach((e,l) -> meta.addEnchant(e,l,true));
        stack.setItemMeta(meta);
        return stack;
    }

    private void registerRecipes() {
        for (ItemSpec spec : specs.values()) {
            NamespacedKey key = new NamespacedKey(this, spec.id());
            ShapedRecipe recipe = new ShapedRecipe(key, make(spec));
            recipe.shape(spec.shape());
            spec.ingredients().forEach(recipe::setIngredient);
            try { Bukkit.addRecipe(recipe); } catch (IllegalStateException ignored) { }
        }
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) { e.getPlayer().sendMessage("§6FunTimeItems §7загружен. Напиши §e/ftitems§7 для списка предметов."); }

    private void applyTalismanEffects() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (ItemStack stack : p.getInventory().getContents()) {
                if (stack == null || !stack.hasItemMeta()) continue;
                String id = stack.getItemMeta().getPersistentDataContainer().get(itemId, PersistentDataType.STRING);
                ItemSpec spec = id == null ? null : specs.get(id);
                if (spec != null && spec.effect() != null && id.endsWith("_talisman")) {
                    p.addPotionEffect(new PotionEffect(spec.effect(), 80, spec.effectLevel(), true, false, true));
                }
            }
        }
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ftitems")) {
            sender.sendMessage("§6§lFunTimeItems §7— авторский набор предметов:");
            sender.sendMessage("§eКрушитель: §fкирка, лопата, мотыга и полный комплект брони.");
            sender.sendMessage("§eТалисманы: §f" + String.join(", ", specs.keySet().stream().filter(s -> s.endsWith("_talisman")).toList()));
            sender.sendMessage("§7Рецепты доступны в верстаке. Для выдачи: §e/fttalisman <id> §7(оператор).");
            return true;
        }
        if (command.getName().equalsIgnoreCase("fttalisman")) {
            if (!sender.hasPermission("funtimeitems.admin")) { sender.sendMessage("§cНедостаточно прав."); return true; }
            if (!(sender instanceof Player p)) { sender.sendMessage("Команду можно выполнить только игроку."); return true; }
            if (args.length != 1) { sender.sendMessage("§cИспользование: /fttalisman <id>"); return true; }
            String id = args[0].toLowerCase(Locale.ROOT);
            ItemSpec spec = specs.get(id);
            if (spec == null) { sender.sendMessage("§cНе найден предмет. Список: /ftitems"); return true; }
            p.getInventory().addItem(make(spec)).values().forEach(item -> p.getWorld().dropItemNaturally(p.getLocation(), item));
            sender.sendMessage("§aВыдано: " + spec.name());
            return true;
        }
        return false;
    }
}
Нажмите зелёную Commit changes, затем ещё раз зелёную Commit changes в окне.

Напишите «готово», и я дам шаг 3: исправим plugin.yml тем же способом.

теперь всё норм?

Нет, всё ещё неправильно. В пути src/main повторяется три раза, а нужен один раз: src/main/java/ru/funtime/items. Так происходит, когда новый файл создаётся изнутри какой-то папки: GitHub дописывает новый путь к текущему.

Чтобы этого не было, будем открывать создание файла по готовой ссылке. Она всегда открывает пустой корень репозитория.

Шаг 1. Удалите этот файл:

Нажмите на FunTimeItems.java в списке.
Справа вверху нажмите три точки ⋯, затем Delete file.
Нажмите зелёную Commit changes, затем ещё раз зелёную в окне.

Шаг 2. Создайте файл заново по ссылке. Вставьте её в адресную строку браузера:

https://github.com/sanapelesanko-arch/funtime-items-plugin/new/main
В поле имени впишите: src/main/java/ru/funtime/items/FunTimeItems.java
В большое окно вставьте тот же Java-код из моего прошлого сообщения.
Нажмите Commit changes, затем ещё раз зелёную в окне.

Когда закончите, напишите «готово». Потом проверим plugin.yml тем же способом и уберём остальные лишние папки.

а скрипт

Вот Java-код. Скопируйте всё и вставьте в большое окно нового файла:

java
package ru.funtime.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

public final class FunTimeItems extends JavaPlugin implements Listener {
    private NamespacedKey itemId;
    private final Map<String, ItemSpec> specs = new LinkedHashMap<>();

    private record ItemSpec(String id, String name, Material material, List<String> lore,
                            Map<Enchantment, Integer> enchants, PotionEffectType effect, int effectLevel,
                            String[] shape, Map<Character, Material> ingredients) {}

    @Override public void onEnable() {
        itemId = new NamespacedKey(this, "custom_item");
        registerSpecs();
        registerRecipes();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, this::applyTalismanEffects, 20L, 40L);
        getLogger().info("FunTimeItems enabled: /ftitems");
    }

    private void add(String id, String name, Material material, List<String> lore,
                     Map<Enchantment,Integer> enchants, PotionEffectType effect, int level,
                     String[] shape, Map<Character,Material> ingredients) {
        specs.put(id, new ItemSpec(id, name, material, lore, enchants, effect, level, shape, ingredients));
    }
    private Map<Enchantment,Integer> ench(Enchantment e, int n) { return Map.of(e,n); }
    private Map<Character,Material> ing(Object... values) {
        Map<Character,Material> m = new HashMap<>();
        for (int i=0;i<values.length;i+=2) m.put((Character)values[i], (Material)values[i+1]);
        return m;
    }

    private void registerSpecs() {
        add("crusher_pickaxe", "§6Кирка Крушителя", Material.NETHERITE_PICKAXE,
                List.of("§7Особая кирка в стиле FunTime", "§eЭффективность и прочность усилены"),
                Map.of(Enchantment.EFFICIENCY, 6, Enchantment.UNBREAKING, 4, Enchantment.FORTUNE, 3), null, 0,
                new String[]{"DDD"," S "," S "}, ing('D',Material.DIAMOND,'S',Material.STICK));
        add("crusher_shovel", "§6Лопата Крушителя", Material.NETHERITE_SHOVEL,
                List.of("§7Особая лопата в стиле FunTime"),
                Map.of(Enchantment.EFFICIENCY, 6, Enchantment.UNBREAKING, 4), null, 0,
                new String[]{" D "," S "," S "}, ing('D',Material.DIAMOND,'S',Material.STICK));
        add("crusher_hoe", "§6Мотыга Крушителя", Material.NETHERITE_HOE,
                List.of("§7Особая мотыга в стиле FunTime"),
                Map.of(Enchantment.EFFICIENCY, 5, Enchantment.UNBREAKING, 4), null, 0,
                new String[]{"DD "," S "," S "}, ing('D',Material.DIAMOND,'S',Material.STICK));
        add("crusher_helmet", "§6Шлем Крушителя", Material.NETHERITE_HELMET, List.of("§7Броня Крушителя"),
                Map.of(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 4), null, 0,
                new String[]{"DDD","D D"}, ing('D',Material.DIAMOND));
        add("crusher_chestplate", "§6Нагрудник Крушителя", Material.NETHERITE_CHESTPLATE, List.of("§7Броня Крушителя"),
                Map.of(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 4), null, 0,
                new String[]{"D D","DDD","DDD"}, ing('D',Material.DIAMOND));
        add("crusher_leggings", "§6Поножи Крушителя", Material.NETHERITE_LEGGINGS, List.of("§7Броня Крушителя"),
                Map.of(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 4), null, 0,
                new String[]{"DDD","D D","D D"}, ing('D',Material.DIAMOND));
        add("crusher_boots", "§6Ботинки Крушителя", Material.NETHERITE_BOOTS, List.of("§7Броня Крушителя"),
                Map.of(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 4, Enchantment.FEATHER_FALLING, 4), null, 0,
                new String[]{"D D","D D"}, ing('D',Material.DIAMOND));

        talisman("crusher_talisman", "§6Талисман Крушителя", Material.TOTEM_OF_UNDYING, PotionEffectType.STRENGTH, 0, Material.NETHERITE_INGOT, Material.DIAMOND);
        talisman("punisher_talisman", "§cТалисман Карателя", Material.TOTEM_OF_UNDYING, PotionEffectType.STRENGTH, 1, Material.BLAZE_POWDER, Material.IRON_INGOT);
        talisman("discord_talisman", "§5Талисман Раздора", Material.TOTEM_OF_UNDYING, PotionEffectType.HASTE, 0, Material.AMETHYST_SHARD, Material.GOLD_INGOT);
        talisman("tyrant_talisman", "§4Талисман Тирана", Material.TOTEM_OF_UNDYING, PotionEffectType.RESISTANCE, 0, Material.IRON_BLOCK, Material.REDSTONE);
        talisman("wrath_talisman", "§cТалисман Ярости", Material.TOTEM_OF_UNDYING, PotionEffectType.STRENGTH, 1, Material.BLAZE_ROD, Material.REDSTONE);
        talisman("vortex_talisman", "§bТалисман Вихря", Material.TOTEM_OF_UNDYING, PotionEffectType.SPEED, 1, Material.ENDER_PEARL, Material.FEATHER);
        talisman("gloom_talisman", "§8Талисман Мрака", Material.TOTEM_OF_UNDYING, PotionEffectType.RESISTANCE, 0, Material.OBSIDIAN, Material.COAL);
        talisman("demon_talisman", "§4Талисман Демона", Material.TOTEM_OF_UNDYING, PotionEffectType.FIRE_RESISTANCE, 0, Material.MAGMA_CREAM, Material.BLAZE_POWDER);
    }

    private void talisman(String id, String name, Material material, PotionEffectType effect, int level, Material center, Material side) {
        add(id, name, material, List.of("§7Держи в инвентаре для эффекта", "§8Авторский набор, вдохновлённый FunTime"),
                Map.of(), effect, level, new String[]{"SES","SCS","SES"}, ing('S',side,'E',Material.EMERALD,'C',center));
    }

    private ItemStack make(ItemSpec spec) {
        ItemStack stack = new ItemStack(spec.material());
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(spec.name());
        meta.setLore(spec.lore());
        meta.getPersistentDataContainer().set(itemId, PersistentDataType.STRING, spec.id());
        spec.enchants().forEach((e,l) -> meta.addEnchant(e,l,true));
        stack.setItemMeta(meta);
        return stack;
    }

    private void registerRecipes() {
        for (ItemSpec spec : specs.values()) {
            NamespacedKey key = new NamespacedKey(this, spec.id());
            ShapedRecipe recipe = new ShapedRecipe(key, make(spec));
            recipe.shape(spec.shape());
            spec.ingredients().forEach(recipe::setIngredient);
            try { Bukkit.addRecipe(recipe); } catch (IllegalStateException ignored) { }
        }
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) { e.getPlayer().sendMessage("§6FunTimeItems §7загружен. Напиши §e/ftitems§7 для списка предметов."); }

    private void applyTalismanEffects() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (ItemStack stack : p.getInventory().getContents()) {
                if (stack == null || !stack.hasItemMeta()) continue;
                String id = stack.getItemMeta().getPersistentDataContainer().get(itemId, PersistentDataType.STRING);
                ItemSpec spec = id == null ? null : specs.get(id);
                if (spec != null && spec.effect() != null && id.endsWith("_talisman")) {
                    p.addPotionEffect(new PotionEffect(spec.effect(), 80, spec.effectLevel(), true, false, true));
                }
            }
        }
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ftitems")) {
            sender.sendMessage("§6§lFunTimeItems §7— авторский набор предметов:");
            sender.sendMessage("§eКрушитель: §fкирка, лопата, мотыга и полный комплект брони.");
            sender.sendMessage("§eТалисманы: §f" + String.join(", ", specs.keySet().stream().filter(s -> s.endsWith("_talisman")).toList()));
            sender.sendMessage("§7Рецепты доступны в верстаке. Для выдачи: §e/fttalisman <id> §7(оператор).");
            return true;
        }
        if (command.getName().equalsIgnoreCase("fttalisman")) {
            if (!sender.hasPermission("funtimeitems.admin")) { sender.sendMessage("§cНедостаточно прав."); return true; }
            if (!(sender instanceof Player p)) { sender.sendMessage("Команду можно выполнить только игроку."); return true; }
            if (args.length != 1) { sender.sendMessage("§cИспользование: /fttalisman <id>"); return true; }
            String id = args[0].toLowerCase(Locale.ROOT);
            ItemSpec spec = specs.get(id);
            if (spec == null) { sender.sendMessage("§cНе найден предмет. Список: /ftitems"); return true; }
            p.getInventory().addItem(make(spec)).values().forEach(item -> p.getWorld().dropItemNaturally(p.getLocation(), item));
            sender.sendMessage("§aВыдано: " + spec.name());
            return true;
        }
        return false;
    }
}
