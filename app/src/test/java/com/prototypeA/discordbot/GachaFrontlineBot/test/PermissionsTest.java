package com.prototypeA.discordbot.GachaFrontlineBot.test;

import com.prototypeA.discordbot.GachaFrontlineBot.Setting;
import com.prototypeA.discordbot.GachaFrontlineBot.permissions.ServerMember;
import com.prototypeA.discordbot.GachaFrontlineBot.permissions.ServerPermissions;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import discord4j.common.util.Snowflake;


final class PermissionsTest {

    private final String SERVER_OWNER_ID = "0";
    private final String ADMIN_USER_ID = "1";
    private final String BLACKLISTED_USER_ID = "2";
    private final String WHITELISTED_USER_ID = "3";
    private final List<String> ADMIN_ROLES = List.of("4");
    private final List<String> BLACKLIST_ROLES = List.of("5");
    private final List<String> WHITELIST_ROLES = List.of("6");

    private ServerPermissions testPermissions;

    PermissionsTest() {
        try {
            Constructor<ServerPermissions> constructor = ServerPermissions.class.getDeclaredConstructor(Snowflake.class, Map.class);
            constructor.setAccessible(true);

            // Test passing null arguments to constructor
            testPermissions = constructor.newInstance(null, null);
            ServerMember user = getTestWhitelistedUser();
            assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", user));
            assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", user));
            assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", user));
            assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(testPermissions, "isBlacklistedUser", user));
            assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(testPermissions, "isServerOwner", user));
            assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(testPermissions, "isWhitelistedUser", user));
            ReflectionTestUtils.setField(testPermissions, "SERVER_OWNER_ID", SERVER_OWNER_ID);
        } catch (Exception e) {
            System.out.println("Failed to construct permissions.");
            e.printStackTrace();
        }
    }


	@Test
    void adminRoleTest() {
        ServerMember adminUser = getTestAdminUser();
        ServerMember nonAdminUser = getTestWhitelistedUser();
        ServerMember serverOwner = getTestServerOwner();
        ReflectionTestUtils.setField(
            testPermissions,
            "SERVER_SETTINGS",
            settingsListToMap(List.of(
                new Setting("Admin Roles", "", listToSettingValue(ADMIN_ROLES), Setting.Type.Role),
                new Setting("Admin Users", "", "", Setting.Type.User),
                new Setting("Blacklisted Roles", "", "", Setting.Type.Role),
                new Setting("Blacklisted Users", "", "", Setting.Type.User),
                new Setting("Whitelisted Roles", "", "", Setting.Type.Role),
                new Setting("Whitelisted Users", "", "", Setting.Type.User)
            ))
        );

        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", adminUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", nonAdminUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", serverOwner));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", adminUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", nonAdminUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", serverOwner));
    }

	@Test
    void adminUserTest() {
        ServerMember adminUser = getTestAdminUser();
        ServerMember nonAdminUser = getTestWhitelistedUser();
        ServerMember serverOwner = getTestServerOwner();
        ReflectionTestUtils.setField(
            testPermissions,
            "SERVER_SETTINGS",
            settingsListToMap(List.of(
                new Setting("Admin Roles", "", "", Setting.Type.Role),
                new Setting("Admin Users", "", adminUser.getId(), Setting.Type.User),
                new Setting("Blacklisted Roles", "", "", Setting.Type.Role),
                new Setting("Blacklisted Users", "", "", Setting.Type.User),
                new Setting("Whitelisted Roles", "", "", Setting.Type.Role),
                new Setting("Whitelisted Users", "", "", Setting.Type.User)
            ))
        );

        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", adminUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", nonAdminUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", serverOwner));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", adminUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", nonAdminUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", serverOwner));
    }

	@Test
    void blacklistedServerOwnerTest() {
        ServerMember serverOwner = getTestServerOwner();
        ReflectionTestUtils.setField(
            testPermissions,
            "SERVER_SETTINGS",
            settingsListToMap(List.of(
                new Setting("Admin Roles", "", "", Setting.Type.Role),
                new Setting("Admin Users", "", "", Setting.Type.User),
                new Setting("Blacklisted Roles", "", listToSettingValue(serverOwner.getRoles()), Setting.Type.Role),
                new Setting("Blacklisted Users", "", serverOwner.getId(), Setting.Type.User),
                new Setting("Whitelisted Roles", "", "", Setting.Type.Role),
                new Setting("Whitelisted Users", "", "", Setting.Type.User)
            ))
        );

        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", serverOwner));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", serverOwner));
    }

    @Test
    void blacklistedRoleTest() {
        ServerMember blacklistedUser = getTestBlacklistedUser();
        ServerMember nonBlacklistedUser = getTestWhitelistedUser();
        Map<String, Setting> settings = 
        settingsListToMap(List.of(
            new Setting("Admin Roles", "", "", Setting.Type.Role),
            new Setting("Admin Users", "", "", Setting.Type.User),
            new Setting("Blacklisted Roles", "", listToSettingValue(BLACKLIST_ROLES), Setting.Type.Role),
            new Setting("Blacklisted Users", "", "", Setting.Type.User),
            new Setting("Whitelisted Roles", "", "", Setting.Type.Role),
            new Setting("Whitelisted Users", "", "", Setting.Type.User)
        ));
        ReflectionTestUtils.setField(
            testPermissions,
            "SERVER_SETTINGS",settings
        );

        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isBlacklistedUser", blacklistedUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isBlacklistedUser", nonBlacklistedUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", blacklistedUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", nonBlacklistedUser));
    }

    @Test
    void blacklistedUserTest() {
        ServerMember blacklistedUser = getTestBlacklistedUser();
        ServerMember nonBlacklistedUser = getTestWhitelistedUser();
        ReflectionTestUtils.setField(
            testPermissions,
            "SERVER_SETTINGS",
            settingsListToMap(List.of(
                new Setting("Admin Roles", "", "", Setting.Type.Role),
                new Setting("Admin Users", "", "", Setting.Type.User),
                new Setting("Blacklisted Roles", "", "", Setting.Type.Role),
                new Setting("Blacklisted Users", "", blacklistedUser.getId(), Setting.Type.User),
                new Setting("Whitelisted Roles", "", "", Setting.Type.Role),
                new Setting("Whitelisted Users", "", "", Setting.Type.User)
            ))
        );

        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isBlacklistedUser", blacklistedUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isBlacklistedUser", nonBlacklistedUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", blacklistedUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", nonBlacklistedUser));
    }

	@Test
    void emptySettingsTest() {
        ServerMember serverOwner = getTestServerOwner();
        ServerMember adminUser = getTestAdminUser();
        ServerMember user1 = getTestBlacklistedUser();
        ServerMember user2 = getTestWhitelistedUser();
        ReflectionTestUtils.setField(
            testPermissions,
            "SERVER_SETTINGS",
            settingsListToMap(List.of(
                new Setting("Admin Roles", "", "", Setting.Type.Role),
                new Setting("Admin Users", "", "", Setting.Type.User),
                new Setting("Blacklisted Roles", "", "", Setting.Type.Role),
                new Setting("Blacklisted Users", "", "", Setting.Type.User),
                new Setting("Whitelisted Roles", "", "", Setting.Type.Role),
                new Setting("Whitelisted Users", "", "", Setting.Type.User)
            ))
        );

        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", adminUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", user1));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", user2));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isAdminUser", serverOwner));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isBlacklistedUser", adminUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isBlacklistedUser", user1));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isBlacklistedUser", user2));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isBlacklistedUser", serverOwner));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isWhitelistedUser", adminUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isWhitelistedUser", user1));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isWhitelistedUser", user2));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isWhitelistedUser", serverOwner));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", adminUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", user1));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", user2));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseAdminFunctionality", serverOwner));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", adminUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", user1));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", user2));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", serverOwner));
    }

    @Test
    void whitelistedRoleTest() {
        ServerMember whitelistedUser = getTestWhitelistedUser();
        ServerMember nonWhitelistedUser = getTestBlacklistedUser();
        ReflectionTestUtils.setField(
            testPermissions,
            "SERVER_SETTINGS",
            settingsListToMap(List.of(
                new Setting("Admin Roles", "", "", Setting.Type.Role),
                new Setting("Admin Users", "", "", Setting.Type.User),
                new Setting("Blacklisted Roles", "", "", Setting.Type.Role),
                new Setting("Blacklisted Users", "", "", Setting.Type.User),
                new Setting("Whitelisted Roles", "", listToSettingValue(WHITELIST_ROLES), Setting.Type.Role),
                new Setting("Whitelisted Users", "", "", Setting.Type.User)
            ))
        );

        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isWhitelistedUser", whitelistedUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isWhitelistedUser", nonWhitelistedUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", whitelistedUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", nonWhitelistedUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", getTestServerOwner()));
    }

    @Test
    void whitelistedUserTest() {
        ServerMember whitelistedUser = getTestWhitelistedUser();
        ServerMember nonWhitelistedUser = getTestBlacklistedUser();
        ReflectionTestUtils.setField(
            testPermissions,
            "SERVER_SETTINGS",
            settingsListToMap(List.of(
                new Setting("Admin Roles", "", "", Setting.Type.Role),
                new Setting("Admin Users", "", "", Setting.Type.User),
                new Setting("Blacklisted Roles", "", "", Setting.Type.Role),
                new Setting("Blacklisted Users", "", "", Setting.Type.User),
                new Setting("Whitelisted Roles", "", "", Setting.Type.Role),
                new Setting("Whitelisted Users", "", whitelistedUser.getId(), Setting.Type.User)
            ))
        );

        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "isWhitelistedUser", whitelistedUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "isWhitelistedUser", nonWhitelistedUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", whitelistedUser));
        assertFalse(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", nonWhitelistedUser));
        assertTrue(ReflectionTestUtils.invokeMethod(testPermissions, "canUseApplication", getTestServerOwner()));
    }


    private ServerMember getTestAdminUser() {
        return new ServerMember(ADMIN_USER_ID, ADMIN_ROLES);
    }

    private ServerMember getTestServerOwner() {
        return new ServerMember(SERVER_OWNER_ID, BLACKLIST_ROLES);
    }

    private ServerMember getTestBlacklistedUser() {
        return new ServerMember(BLACKLISTED_USER_ID, BLACKLIST_ROLES);
    }

    private ServerMember getTestWhitelistedUser() {
        return new ServerMember(WHITELISTED_USER_ID, WHITELIST_ROLES);
    }

    private String listToSettingValue(List<String> list) {
        return list.toString()
            .replace("[", "")
            .replace("]", "");
    }

    private Map<String, Setting> settingsListToMap(List<Setting> settings) {
        return settings.stream()
            .collect(Collectors.toMap(Setting::getName, Function.identity()));
    }
}
