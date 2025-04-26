package com.prototypeA.discordbot.GachaFrontlineBot.permissions;

import com.prototypeA.discordbot.GachaFrontlineBot.Setting;

import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;


/**
 * The base of server permission comparison logic.
 */
public abstract class AbstractServerPermissions {

    protected final String SERVER_OWNER_ID;
    protected final Map<String, Setting> SERVER_SETTINGS;

    /**
     * Constructs a new instance of this class that represents the 
     * permissions of the specified server.
     * 
     * @param serverOwnerId The id of the server owner.
     * @param serverSettings The list of the server's settings 
     * stored in this application.
     */
    protected AbstractServerPermissions(String serverOwnerId, Map<String, Setting> serverSettings) {
        if (serverOwnerId == null) {
            SERVER_OWNER_ID = "-1";
        } else {
            SERVER_OWNER_ID = serverOwnerId;
        }
        if (serverSettings == null) {
            SERVER_SETTINGS = Map.of();
        } else {
            SERVER_SETTINGS = serverSettings;
        }
    }


    /**
     * Checks if the server member is allowed to use this 
     * application's administrative functionality. The 
     * member must not be blacklisted and must be specifically 
     * an admin user or have an admin role, or they must be 
     * the server owner.
     * 
     * @param member The server member to check.
     * @return True if the server member is allowed to use 
     * non-administrative functions of this application.
     */
    protected boolean canUseAdminFunctionality(ServerMember member) {
        // Blacklist takes precedence over admins
        return !isBlacklistedUser(member) &&
            isAdminUser(member) ||
            isServerOwner(member);
    }

    /**
     * Checks if the server member is allowed to use this 
     * application's (non-administrative) functionality. 
     * The member must either not be blacklisted or is 
     * whitelisted if it is not empty, or they must be 
     * the server owner.
     * 
     * @param member The server member to check.
     * @return True if the server member is allowed to use 
     * non-administrative functions of this application.
     */
    protected boolean canUseApplication(ServerMember member) {
        // Blacklist takes precedence over whitelist
        return !isBlacklistedUser(member) &&
            isWhitelistedUser(member) ||
            isServerOwner(member);
    }

    /**
     * Checks if the server member is specifically an admin 
     * user or has an admin role. The server owner will always 
     * be an admin.
     * 
     * @param member The server member to check.
     * @return True if the server member is an admin.
     */
    protected boolean isAdminUser(ServerMember member) {
        // Server owner is always an admin
        return isServerOwner(member) ||
            // Blacklist takes precedence
            !isBlacklistedUser(member) &&
            // Member has admin role
            CollectionUtils.containsAny(
                member.getRoles(),
                SERVER_SETTINGS.containsKey("Admin Roles")
                    ? SERVER_SETTINGS.get("Admin Roles")
                        .getValues()
                    : List.of()
            ) ||
            // Member is specified as an admin user
            (SERVER_SETTINGS.containsKey("Admin Users")
                ? SERVER_SETTINGS.get("Admin Users")
                    .getValues()
                : List.of())
                .contains(member.getId());
    }

    /**
     * Checks if the server member is specifically blacklisted 
     * or has a blacklisted role. The server owner is unaffected.
     * 
     * @param member The server member to check.
     * @return True if the server member is blacklisted.
     */
    protected boolean isBlacklistedUser(ServerMember member) {
        List<String> roleBlacklist = SERVER_SETTINGS.containsKey("Blacklisted Roles")
            ? SERVER_SETTINGS.get("Blacklisted Roles")
                .getValues()
            : List.of();
        List<String> userBlacklist = SERVER_SETTINGS.containsKey("Blacklisted Users")
            ? SERVER_SETTINGS.get("Blacklisted Users")
                .getValues()
            : List.of();

        // Server owner is unaffected
        return !isServerOwner(member) &&
            // Member has blacklisted role
            (CollectionUtils.containsAny(member.getRoles(), roleBlacklist) ||
            // Member is specifically blacklisted
            userBlacklist.contains(member.getId()));
    }

    /**
     * Checks if the server member is the owner of the server.
     * 
     * @param member The server member to check.
     * @return True if the server member is the server owner.
     */
    protected boolean isServerOwner(ServerMember member) {
        return SERVER_OWNER_ID.equals(member.getId());
    }

    /**
     * Checks if the server member is specifically whitelisted or 
     * has a whitelisted role. A server member will be considered 
     * whitelisted if the whitelist is empty and they are not 
     * blacklisted. The server owner will always be whitelisted.
     * 
     * @param member The server member to check.
     * @return True if the server member is whitelisted.
     */
    protected boolean isWhitelistedUser(ServerMember member) {
        List<String> roleWhitelist = SERVER_SETTINGS.containsKey("Whitelisted Roles")
            ? SERVER_SETTINGS.get("Whitelisted Roles")
                .getValues()
            : List.of();
        List<String> userWhitelist = SERVER_SETTINGS.containsKey("Whitelisted Roles")
            ? SERVER_SETTINGS.get("Whitelisted Users")
                .getValues()
            : List.of();

        // Server owner is always whitelisted
        return isServerOwner(member) ||
            // Blacklist takes precedence over whitelist
            !isBlacklistedUser(member) &&
            // Whitelist is empty
            (roleWhitelist.isEmpty() &&
                userWhitelist.isEmpty()) ||
            // Member has whitelisted role or
            (CollectionUtils.containsAny(member.getRoles(), roleWhitelist) ||
                // Member is specifically whitelisted
                userWhitelist.contains(member.getId()));
    }
}
