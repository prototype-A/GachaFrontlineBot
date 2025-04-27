package com.prototypeA.discordbot.GachaFrontlineBot.test;

import com.prototypeA.discordbot.GachaFrontlineBot.handlers.AbstractTaskHandler;
import com.prototypeA.discordbot.GachaFrontlineBot.tasks.MessageCommandToSlashCommandTask;
import com.prototypeA.discordbot.GachaFrontlineBot.tasks.RegisterServerCommandsTask;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;


final class TaskTest {

    private final String TEST_PREFIX = "~";
    private final AbstractTaskHandler<?> TEST_TASK_1;
    private final AbstractTaskHandler<?> TEST_TASK_2;

    TaskTest() {
        // Can Disable
        TEST_TASK_1 = new MessageCommandToSlashCommandTask(List.of(), TEST_PREFIX);

        // Cannot Disable
        TEST_TASK_2 = new RegisterServerCommandsTask(null);
    }
    
    @Test
    void settingsTest() {
        List<String> task1Settings = TEST_TASK_1.getDefaultSettings().stream().map(setting -> setting.getName()).toList();
        assertTrue(task1Settings.contains("Enable Task: " + ReflectionTestUtils.getField(TEST_TASK_1, "NAME")));
        assertTrue(TEST_TASK_1.getDefaultSettings().get(task1Settings.indexOf("Message Command Prefix")).getValue().equals(TEST_PREFIX));


        List<String> task2Settings = TEST_TASK_2.getDefaultSettings().stream().map(setting -> setting.getName()).toList();
        assertFalse(task2Settings.contains("Enable Task: " + ReflectionTestUtils.getField(TEST_TASK_2, "NAME")));

        assertThrows(UnsupportedOperationException.class, () -> TEST_TASK_1.getDefaultSettings().add(null));
        assertThrows(UnsupportedOperationException.class, () -> TEST_TASK_1.getDefaultSettings().remove(null));
    }

}
