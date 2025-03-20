package com.prototypeA.discordbot.GachaFrontlineBot.permissions;

import java.util.List;


/**
 * Class that represents a member of a server, meant to be a 
 * simplified representation of Discord's {@code PartialMember} 
 * entity and is used for checking if that member has the 
 * necessary permissions to use this application.
 */
public final class ServerMember {

    private final String ID;
    private final List<String> ROLES;
    
    /**
     * Constructs a new simplified representation of a server member.
     * 
     * @param id The ID of the server member.
     * @param roles The list of role IDs of the server member.
     */
    public ServerMember(String id, List<String> roles) {
        ID = id;
        ROLES = roles;
    }


    /**
     * Returns the ID of this server member.
     * 
     * @return The ID of this server member.
     */
    public String getId() {
        return ID;
    }

    /**
     * Returns the list of role IDs this server member has.
     * 
     * @return The list of role IDs of this server member.
     */
    public List<String> getRoles() {
        return ROLES;
    }
}
