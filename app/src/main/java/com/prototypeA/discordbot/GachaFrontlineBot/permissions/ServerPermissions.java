package com.prototypeA.discordbot.GachaFrontlineBot.permissions;

import com.prototypeA.discordbot.GachaFrontlineBot.Setting;

import java.util.Map;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.PartialMember;


/**
 * (Partial)Member implementation for server permissions.
 */
public final class ServerPermissions extends AbstractServerPermissions {

    /**
     * Constructs a new instance of this class that represents the 
     * permissions of the specified server.
     * 
     * @param serverOwnerId The id of the server owner.
     * @param serverSettings The list of the server's settings 
     * stored in this application.
     */
    ServerPermissions(Snowflake serverOwnerId, Map<String, Setting> serverSettings) {
        super(serverOwnerId != null ? serverOwnerId.asString() : null, serverSettings);
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
    public boolean canUseAdminFunctionality(PartialMember member) {
        return canUseAdminFunctionality(getServerMember(member));
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
    public boolean canUseApplication(PartialMember member) {
        return canUseApplication(getServerMember(member));
    }

    /**
     * Returns a simplified representation containing only 
     * the string ID and list of string role IDs of the 
     * passed server member.
     * 
     * @param member The server member to get a simplified 
     * representation of.
     * @return A simplified representation of the server member.
     */
    private ServerMember getServerMember(PartialMember member) {
        return new ServerMember(
            member.getId()
                .asString(),
            member.getRoleIds()
                .stream()
                .map(Snowflake::asString)
                .toList()
        );
    }

    /**
     * Checks if the server member is specifically an admin 
     * user or has an admin role.
     * 
     * @param member The server member to check.
     * @return True if the server member is an admin.
     */
    public boolean isAdminUser(PartialMember member) {
        return isAdminUser(getServerMember(member));
    }

    /**
     * Checks if the server member is specifically blacklisted 
     * or has a blacklisted role.
     * 
     * @param member The server member to check.
     * @return True if the server member is blacklisted.
     */
    public boolean isBlacklistedUser(PartialMember member) {
        return isBlacklistedUser(getServerMember(member));
    }

    /**
     * Checks if the server member is the owner of the server.
     * 
     * @param member The server member to check.
     * @return True if the server member is the server owner.
     */
    public boolean isServerOwner(PartialMember member) {
        //return SERVER_OWNER_ID.equals(member.getId());
        return isServerOwner(getServerMember(member));
    }

    /**
     * Checks if the server member is specifically whitelisted 
     * or has a whitelisted role.
     * 
     * @param member The server member to check.
     * @return True if the server member is whitelisted.
     */
    public boolean isWhitelistedUser(PartialMember member) {
        return isWhitelistedUser(getServerMember(member));
    }
}
