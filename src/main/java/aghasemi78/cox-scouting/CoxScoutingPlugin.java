package com.datbear.coxscouting;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.util.Text;

import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.client.plugins.coxscouting.CoxScoutingConfig.RoomCount;
import net.runelite.client.plugins.raids.Raid;
import net.runelite.client.plugins.raids.RaidsPlugin;
import net.runelite.client.plugins.raids.RaidRoom;
import net.runelite.client.plugins.raids.events.RaidReset;
import net.runelite.client.plugins.raids.events.RaidScouted;

@Slf4j
@PluginDescriptor(
        name = "Cox Scouting",
        description = "Helps with scouting for raids with specific rooms by disabling reload option when a desirable layout is found",
        tags = {"raid", "cox", "layout"},
        enabledByDefault = false
)
public class CoxScoutingPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private CoxScoutingConfig config;

    @Inject
    private ClientThread clientThread;

    @Getter
    private Raid currentRaid;

    private boolean inRaidChambers = false;
    private boolean desirableRaidFound = false;

    @Provides
    CoxScoutingConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(CoxScoutingConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        log.info("CoxScouting plugin started!");
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("CoxScouting plugin stopped!");
        currentRaid = null;
        inRaidChambers = false;
        desirableRaidFound = false;
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if (event.getVarbitId() == Varbits.IN_RAID)
        {
            boolean inRaid = client.getVarbitValue(Varbits.IN_RAID) == 1;
            if (inRaid && !inRaidChambers)
            {
                inRaidChambers = true;
            }
            else if (!inRaid && inRaidChambers)
            {
                inRaidChambers = false;
                currentRaid = null;
                desirableRaidFound = false;
            }
        }
    }

    @Subscribe
    public void onRaidScouted(RaidScouted event)
    {
        this.currentRaid = event.getRaid();

        // Access the raid layout code
        String layoutCode = currentRaid.getLayout().toCodeString();

        // Get the first 3 letters of the layout code
        String raidStartOrderCode = layoutCode.length() >= 3 ? layoutCode.substring(0, 3).toUpperCase() : layoutCode.toUpperCase();

        // Get the selected raid start order from config
        RaidStartOrder selectedStartOrder = config.raidStartOrder();

        // Check if the selected start order matches the raid's start order code
        boolean startOrderMatches = selectedStartOrder == RaidStartOrder.ANY ||
                raidStartOrderCode.equals(selectedStartOrder.toString());


        // List of room names to ignore (case-insensitive)
        List<String> roomsToIgnore = Arrays.asList("farming", "scavengers", "end", "start", "empty");

        // Build the room order string, ignoring specified rooms
        List<String> roomNames = Arrays.stream(currentRaid.getRooms())
                .filter(room -> room != null)
                .filter(room -> !roomsToIgnore.contains(room.getName().toLowerCase()))
                .map(RaidRoom::getName)
                .collect(Collectors.toList());

        // Count the total number of rooms (excluding ignored ones)
        int totalRooms = roomNames.size();

        // Collect the rooms selected by the user
        List<String> selectedRooms = getSelectedRooms();

        // Check if all selected rooms are present in the raid layout
        Set<String> raidRoomSet = roomNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> selectedRoomSet = selectedRooms.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        boolean roomsMatch = raidRoomSet.containsAll(selectedRoomSet);

        // Check if the total number of rooms matches the user's selection
        boolean roomCountMatches = doesRoomCountMatch(totalRooms);

        // Determine if the raid is desirable
        if (!selectedRooms.isEmpty() && roomsMatch && roomCountMatches && startOrderMatches)
        {
            desirableRaidFound = true;
        }
        else if (!selectedRooms.isEmpty())
        {
            desirableRaidFound = false;
        }
        else
        {
            desirableRaidFound = false;
        }
    }

    private List<String> getSelectedRooms()
    {
        // Collect selected rooms from the config
        List<String> selectedRooms = new ArrayList<>();

        if (config.includeMystics())
        {
            selectedRooms.add("Mystics");
        }
        if (config.includeShamans())
        {
            selectedRooms.add("Shamans");
        }
        if (config.includeVasa())
        {
            selectedRooms.add("Vasa");
        }
        if (config.includeTightrope())
        {
            selectedRooms.add("Tightrope");
        }
        if (config.includeThieving())
        {
            selectedRooms.add("Thieving");
        }
        if (config.includeIceDemon())
        {
            selectedRooms.add("Ice Demon");
        }
        if (config.includeGuardians())
        {
            selectedRooms.add("Guardians");
        }
        if (config.includeVespula())
        {
            selectedRooms.add("Vespula");
        }
        if (config.includeTekton())
        {
            selectedRooms.add("Tekton");
        }
        if (config.includeVanguards())
        {
            selectedRooms.add("Vanguards");
        }
        if (config.includeMuttadiles())
        {
            selectedRooms.add("Muttadiles");
        }

        return selectedRooms;
    }

    private boolean doesRoomCountMatch(int totalRooms)
    {
        RoomCount selectedRoomCount = config.roomCount();
        if (selectedRoomCount == RoomCount.ANY)
        {
            return true;
        }
        else if (selectedRoomCount == RoomCount.FIVE)
        {
            return totalRooms == 5;
        }
        else if (selectedRoomCount == RoomCount.SIX)
        {
            return totalRooms == 6;
        }
        return false;
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!inRaidChambers)
        {
            return;
        }

        MenuEntry entry = event.getMenuEntry();
        String option = Text.removeTags(entry.getOption()).toLowerCase();
        String target = Text.removeTags(entry.getTarget()).toLowerCase();

        if (target.contains("steps"))
        {
            if (desirableRaidFound)
            {
                // When raid is desirable, deprioritize "Climb" and "Reload"
                if (option.equals("climb") || option.equals("reload"))
                {
                    entry.setDeprioritized(true);
                }
            }
            else
            {
                // When raid is not desirable, prioritize "Reload"
                if (option.equals("reload"))
                {
                    entry.setDeprioritized(false);
                    // Do not change the type or MenuAction
                }
                else if (option.equals("climb"))
                {
                    entry.setDeprioritized(true);
                }
            }
        }
    }

    @Subscribe(priority = -1) // Ensure this runs after other menu modifications
    public void onMenuOpened(MenuOpened event)
    {
        if (!inRaidChambers)
        {
            return;
        }

        MenuEntry[] menuEntries = client.getMenuEntries();
        List<MenuEntry> entries = new ArrayList<>(Arrays.asList(menuEntries));
        MenuEntry reloadEntry = null;
        MenuEntry climbEntry = null;
        MenuEntry walkHereEntry = null;

        // Identify the menu entries for "Reload", "Climb", and "Walk here"
        for (MenuEntry entry : entries)
        {
            String option = Text.removeTags(entry.getOption()).toLowerCase();
            String target = Text.removeTags(entry.getTarget()).toLowerCase();

            if (target.contains("steps"))
            {
                if (option.equals("reload"))
                {
                    reloadEntry = entry;
                }
                else if (option.equals("climb"))
                {
                    climbEntry = entry;
                }
                else if (option.equals("walk here"))
                {
                    walkHereEntry = entry;
                }
            }
        }

        if (!desirableRaidFound)
        {
            // Undesirable raid: Make "Reload" the default left-click action
            if (reloadEntry != null)
            {
                entries.remove(reloadEntry);
                entries.add(reloadEntry);
                client.setMenuEntries(entries.toArray(new MenuEntry[0]));
            }
        }
        else
        {
            // Desirable raid: Make "Walk here" the default left-click action
            if (walkHereEntry != null)
            {
                entries.remove(walkHereEntry);
                entries.add(walkHereEntry);
                client.setMenuEntries(entries.toArray(new MenuEntry[0]));
            }
        }
    }

    @Subscribe
    public void onRaidReset(RaidReset event)
    {
        // Reset the current raid when the raid ends or the player leaves
        currentRaid = null;
        desirableRaidFound = false;
    }

    @Subscribe
    public void onPluginChanged(PluginChanged event)
    {
        // Ensure that the Raids plugin is enabled
        if (event.getPlugin() instanceof RaidsPlugin)
        {
            if (event.isLoaded())
            {
                log.info("Raids plugin has been enabled.");
            }
            else
            {
                log.warn("Raids plugin has been disabled. This plugin depends on the Raids plugin.");
            }
        }
    }
}
