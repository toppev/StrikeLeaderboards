package ga.strikepractice.leaderboards;

import org.bukkit.inventory.ItemStack;

public class SimpleIcon {

	
	private ItemStack item;
	private String tag;
	private int slot;
	
	public SimpleIcon(ItemStack item, String tag, int slot) {
		this.tag = tag.toLowerCase().replace("-", "_");
		this.slot = slot;
		this.item = item;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public String getTag() {
		return tag;
	}
	
	public int getSlot() {
		return slot;
	}
}
