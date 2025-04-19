package com.prototypeA.discordbot.GachaFrontlineBot.test;

import com.prototypeA.discordbot.GachaFrontlineBot.commands.DieRollCommand;
import com.prototypeA.discordbot.GachaFrontlineBot.commands.SettingsCommand;
import com.prototypeA.discordbot.GachaFrontlineBot.handlers.AbstractSlashCommandHandler;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import discord4j.common.util.Snowflake;


final class CommandTest {

    private final AbstractSlashCommandHandler TEST_COMMAND_1;
    private final String COMMAND_1_NAME;
    private final AbstractSlashCommandHandler TEST_COMMAND_2;
    private final String COMMAND_2_NAME;


    CommandTest() {
        // Not Admin, Not Single Instance, Can Disable
        TEST_COMMAND_1 = new DieRollCommand();
        COMMAND_1_NAME = "ROLL";

        // Admin, Single Instance, Cannot Disable
        TEST_COMMAND_2 = new SettingsCommand();
        COMMAND_2_NAME = "Settings";
    }

    @Test
    void adminCommandTest() {
        assertFalse((boolean) ReflectionTestUtils.getField(TEST_COMMAND_1, "IS_ADMIN_COMMAND"));
        assertTrue((boolean) ReflectionTestUtils.getField(TEST_COMMAND_2, "IS_ADMIN_COMMAND"));
    }

    @Test
    void commandNameTest() {
        assertTrue(TEST_COMMAND_1.getCommandName().equals(COMMAND_1_NAME.toLowerCase()));
        assertTrue(TEST_COMMAND_2.getCommandName().equals(COMMAND_2_NAME.toLowerCase()));
    }

    @Test
    void settingsTest() {
        List<String> command1Settings = TEST_COMMAND_1.getDefaultSettings().stream().map(setting -> setting.getName()).toList();
        assertTrue(command1Settings.contains("Enable Command: /" + COMMAND_1_NAME.toLowerCase()));
        List<String> command2Settings = TEST_COMMAND_2.getDefaultSettings().stream().map(setting -> setting.getName()).toList();
        assertFalse(command2Settings.contains("Enable Command: /" + COMMAND_2_NAME.toLowerCase()));

        assertThrows(UnsupportedOperationException.class, () -> TEST_COMMAND_1.getDefaultSettings().add(null));
        assertThrows(UnsupportedOperationException.class, () -> TEST_COMMAND_1.getDefaultSettings().remove(null));
    }

    @Test
    void singleInstanceCommandTest() {
        Snowflake serverId = Snowflake.of(0);

        assertFalse((boolean) ReflectionTestUtils.getField(TEST_COMMAND_1, "SINGLE_INSTANCE"));
        assertFalse(ReflectionTestUtils.invokeMethod(TEST_COMMAND_1, "lockCommandForServer", serverId));
        
        assertTrue((boolean) ReflectionTestUtils.getField(TEST_COMMAND_2, "SINGLE_INSTANCE"));
        assertTrue(ReflectionTestUtils.invokeMethod(TEST_COMMAND_2, "lockCommandForServer", serverId));
        assertFalse(ReflectionTestUtils.invokeMethod(TEST_COMMAND_2, "lockCommandForServer", serverId));
        assertTrue(ReflectionTestUtils.invokeMethod(TEST_COMMAND_2, "unlockCommandForServer", serverId));
        assertFalse(ReflectionTestUtils.invokeMethod(TEST_COMMAND_2, "unlockCommandForServer", serverId));
    }
}
