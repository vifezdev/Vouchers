package lol.vifez.vouchers;

import lol.vifez.volt.internal.CommandManager;
import lol.vifez.vouchers.command.VouchersCommand;
import lol.vifez.vouchers.config.ConfigFile;
import lol.vifez.vouchers.listener.VoucherListener;
import lol.vifez.vouchers.manager.VoucherManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class VouchersPlugin extends JavaPlugin {

    private VoucherManager voucherManager;
    private ConfigFile configFile;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configFile = new ConfigFile(this, "vouchers.yml");
        this.voucherManager = new VoucherManager(this);

        getServer().getPluginManager().registerEvents(new VoucherListener(this, voucherManager), this);
        registerCommands();
    }

    public void registerCommands() {
        this.commandManager = new CommandManager(this);
        this.commandManager.register(new VouchersCommand(this, voucherManager));
    }


    public ConfigFile getConfigFile() {
        return configFile;
    }

    @Override
    public void onDisable() {
        voucherManager.saveVouchers();
    }
}
