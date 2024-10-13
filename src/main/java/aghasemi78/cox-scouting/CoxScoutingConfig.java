package com.datbear.coxscouting;

import net.runelite.client.config.*;

@ConfigGroup("coxscouting")
public interface CoxScoutingConfig extends Config
{
    @ConfigItem(
            keyName = "raidStartOrder",
            name = "Raid start order",
            description = "Select the desired raid start order code",
            position = 99 // Adjust position as needed
    )
    default RaidStartOrder raidStartOrder()
    {
        return RaidStartOrder.ANY;
    }

    // Include options for selecting desired rooms
    @ConfigItem(
            keyName = "includeMystics",
            name = "Include Mystics",
            description = "Include Mystics in desired rooms",
            position = 1
    )
    default boolean includeMystics()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeShamans",
            name = "Include Shamans",
            description = "Include Shamans in desired rooms",
            position = 2
    )
    default boolean includeShamans()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeVasa",
            name = "Include Vasa",
            description = "Include Vasa in desired rooms",
            position = 3
    )
    default boolean includeVasa()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeTightrope",
            name = "Include Tightrope",
            description = "Include Tightrope in desired rooms",
            position = 4
    )
    default boolean includeTightrope()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeThieving",
            name = "Include Thieving",
            description = "Include Thieving in desired rooms",
            position = 5
    )
    default boolean includeThieving()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeIceDemon",
            name = "Include Ice Demon",
            description = "Include Ice Demon in desired rooms",
            position = 6
    )
    default boolean includeIceDemon()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeGuardians",
            name = "Include Guardians",
            description = "Include Guardians in desired rooms",
            position = 7
    )
    default boolean includeGuardians()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeVespula",
            name = "Include Vespula",
            description = "Include Vespula in desired rooms",
            position = 8
    )
    default boolean includeVespula()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeTekton",
            name = "Include Tekton",
            description = "Include Tekton in desired rooms",
            position = 9
    )
    default boolean includeTekton()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeVanguards",
            name = "Include Vanguards",
            description = "Include Vanguards in desired rooms",
            position = 10
    )
    default boolean includeVanguards()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeMuttadiles",
            name = "Include Muttadiles",
            description = "Include Muttadiles in desired rooms",
            position = 11
    )
    default boolean includeMuttadiles()
    {
        return false;
    }

    @ConfigItem(
            keyName = "roomCount",
            name = "Room Count",
            description = "Select the desired total number of rooms",
            position = 12
    )
    default RoomCount roomCount()
    {
        return RoomCount.ANY;
    }

    enum RoomCount
    {
        ANY("Any"),
        FIVE("5"),
        SIX("6");

        private final String name;

        RoomCount(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
