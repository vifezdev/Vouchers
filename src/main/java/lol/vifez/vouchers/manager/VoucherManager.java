package lol.vifez.vouchers.manager;

import lol.vifez.vouchers.VouchersPlugin;
import lol.vifez.vouchers.util.CC;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class VoucherManager {

    private final VouchersPlugin plugin;
    private final Map<String, Voucher> vouchers = new HashMap<>();
    private final FileConfiguration config;
    private final String voucherItemNameFormat;

    public VoucherManager(VouchersPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigFile().getConfig();
        this.voucherItemNameFormat = config.getString("voucher-item-name", "&aVoucher: <voucher>");
        loadVouchers();
    }

    public String getVoucherItemNameFormat() {
        return voucherItemNameFormat;
    }

    public void loadVouchers() {
        vouchers.clear();

        ConfigurationSection vouchersSection = config.getConfigurationSection("VOUCHERS");
        if (vouchersSection == null) return;

        vouchersSection.getKeys(false).forEach(key -> {
            ConfigurationSection section = vouchersSection.getConfigurationSection(key);
            if (section == null) return;

            Material material = parseMaterialOrDefault(section.getString("ITEM"), Material.PAPER);
            String itemName = section.getString("ITEM_NAME", null);
            List<String> actions = section.getStringList("ACTIONS");
            List<String> broadcast = section.getStringList("BROADCAST");
            List<String> lore = section.getStringList("LORE");

            vouchers.put(key.toUpperCase(Locale.ROOT), new Voucher(key.toUpperCase(Locale.ROOT), material, itemName, actions, broadcast, lore));
        });
    }

    public void saveVouchers() {
        ConfigurationSection vouchersSection = config.createSection("VOUCHERS");
        vouchers.values().forEach(voucher -> {
            ConfigurationSection section = vouchersSection.createSection(voucher.getName());
            section.set("ITEM", voucher.getItem().name());
            Optional.ofNullable(voucher.getItemName()).ifPresent(name -> section.set("ITEM_NAME", name));
            section.set("ACTIONS", voucher.getActions());
            section.set("BROADCAST", voucher.getBroadcast());
            section.set("LORE", voucher.getLore());
        });
        plugin.getConfigFile().save();
    }

    public boolean createVoucher(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        if (vouchers.containsKey(key)) return false;

        vouchers.put(key, new Voucher(key, Material.PAPER, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        saveVouchers();
        return true;
    }

    public boolean deleteVoucher(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        if (vouchers.remove(key) == null) return false;

        saveVouchers();
        return true;
    }

    public Voucher getVoucher(String name) {
        if (name == null) return null;
        return vouchers.get(name.toUpperCase(Locale.ROOT));
    }

    public Map<String, Voucher> getVouchers() {
        return Collections.unmodifiableMap(vouchers);
    }

    public ItemStack buildVoucherItem(String voucherName) {
        Voucher voucher = getVoucher(voucherName);
        return voucher != null ? voucher.buildItem(voucherItemNameFormat) : null;
    }

    private Material parseMaterialOrDefault(String materialName, Material defaultMaterial) {
        if (materialName == null) return defaultMaterial;
        try {
            return Material.valueOf(materialName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return defaultMaterial;
        }
    }

    public static class Voucher {
        private final String name;
        private Material item;
        private String itemName;
        private List<String> actions;
        private List<String> broadcast;
        private List<String> lore;

        public Voucher(String name, Material item, String itemName, List<String> actions, List<String> broadcast, List<String> lore) {
            this.name = name;
            this.item = item;
            this.itemName = itemName;
            this.actions = new ArrayList<>(actions);
            this.broadcast = new ArrayList<>(broadcast);
            this.lore = new ArrayList<>(lore);
        }

        public String getName() {
            return name;
        }

        public Material getItem() {
            return item;
        }

        public void setItem(Material item) {
            this.item = item;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public List<String> getActions() {
            return Collections.unmodifiableList(actions);
        }

        public void setActions(List<String> actions) {
            this.actions = new ArrayList<>(actions);
        }

        public List<String> getBroadcast() {
            return Collections.unmodifiableList(broadcast);
        }

        public void setBroadcast(List<String> broadcast) {
            this.broadcast = new ArrayList<>(broadcast);
        }

        public List<String> getLore() {
            return Collections.unmodifiableList(lore);
        }

        public void setLore(List<String> lore) {
            this.lore = new ArrayList<>(lore);
        }

        public ItemStack buildItem(String globalDisplayNameFormat) {
            ItemStack itemStack = new ItemStack(item);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) return itemStack;

            String displayName = (itemName == null || itemName.isEmpty())
                    ? CC.translate(globalDisplayNameFormat.replace("<voucher>", name))
                    : CC.translate(itemName);

            meta.setDisplayName(displayName);

            if (!lore.isEmpty()) {
                meta.setLore(lore.stream().map(CC::translate).collect(Collectors.toList()));
            }

            itemStack.setItemMeta(meta);
            return itemStack;
        }
    }
}