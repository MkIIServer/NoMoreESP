package tw.mics.spigot.plugin.nomoreesp.command;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tw.mics.spigot.plugin.nomoreesp.NoMoreESP;

public class TestCommand implements CommandExecutor{
	NoMoreESP plugin;
	List<Material> blockBlockList;
	public TestCommand(NoMoreESP i){
		this.plugin = i;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§4this command must run on player");
			return true;
		}
		Player player = (Player) sender;
		Player target = plugin.getServer().getPlayer(args[0]);
		this.plugin.checkBlock(player, target);
		return true;
	}
}
