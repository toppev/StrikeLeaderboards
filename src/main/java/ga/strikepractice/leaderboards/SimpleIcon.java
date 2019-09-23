package ga.strikepractice.leaderboards;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
@AllArgsConstructor
public class SimpleIcon {

    private ItemStack item;
    private String tag;
    private int slot;

}
