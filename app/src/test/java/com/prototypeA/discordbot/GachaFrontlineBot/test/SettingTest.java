package com.prototypeA.discordbot.GachaFrontlineBot.test;

import com.prototypeA.discordbot.GachaFrontlineBot.Setting;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.Test;


final class SettingTest {

    @Test
    void booleanValueTest() {
        Setting testBooleanSetting1 = new Setting(
            "True Boolean Value Setting",
            "",
            "True",
            Setting.Type.Boolean
        );
        Setting testBooleanSetting2 = new Setting(
            "False Boolean Value Setting",
            "",
            "False",
            Setting.Type.Boolean
        );

        assertTrue(Boolean.parseBoolean(testBooleanSetting1.getValue()));
        assertFalse(Boolean.parseBoolean(testBooleanSetting2.getValue()));
    }

    @Test
    void multipleValuesTest() {
        Setting testMultipleValuesSetting1 = new Setting(
            "Multiple Values Setting 1",
            "",
            "",
            Setting.Type.Role
        );
        Setting testMultipleValuesSetting2 = new Setting(
            "Multiple Values Setting 2",
            "",
            "1",
            Setting.Type.Role
        );
        Setting testMultipleValuesSetting3 = new Setting(
            "Multiple Values Setting 3",
            "",
            "2, 3, 4",
            Setting.Type.User
        );

        assertTrue(testMultipleValuesSetting1.getValues().size() == 0);
        assertTrue(testMultipleValuesSetting1.getValues().equals(List.of()));
        assertTrue(testMultipleValuesSetting2.getValues().size() == 1);
        assertTrue(testMultipleValuesSetting2.getValues().equals(List.of("1")));
        assertTrue(testMultipleValuesSetting3.getValues().size() == 3);
        assertTrue(testMultipleValuesSetting3.getValues().equals(List.of("2", "3", "4")));
    }
}
