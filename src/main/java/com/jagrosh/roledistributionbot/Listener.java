/*
 * Copyright 2020 John Grosh (john.a.grosh@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.roledistributionbot;

import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Listener implements EventListener
{
    private final String command, command2;
    private final List<Long> roles;
    
    public Listener(String command, String command2, List<Long> roles)
    {
        this.command = command;
        this.command2 = command2;
        this.roles = roles;
    }
    
    @Override
    public void onEvent(GenericEvent ge)
    {
        if (ge instanceof GuildMemberJoinEvent)
            onGuildMemberJoin((GuildMemberJoinEvent) ge);
        else if (ge instanceof GuildMessageReceivedEvent)
            onGuildMessageReceived((GuildMessageReceivedEvent) ge);
    }
    
    private void onGuildMemberJoin(GuildMemberJoinEvent event)
    {
        addRole(event.getMember());
    }
    
    private void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        Member member = event.getMember();
        if(member != null 
                && member.hasPermission(Permission.ADMINISTRATOR) 
                && event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES))
        {
            // distribute roles
            if(event.getMessage().getContentRaw().equalsIgnoreCase(command))
            {
                event.getMessage().addReaction("\u23F1").queue(); // ⏱
                event.getGuild().loadMembers(m -> addRole(m))
                        .onSuccess(v -> event.getMessage().addReaction("\u2705").queue()); // ✅
            }
            // count roles
            else if (event.getMessage().getContentRaw().equalsIgnoreCase(command2))
            {
                event.getMessage().addReaction("\u23F1").queue(); // ⏱
                List<Role> validRoles = getValidRoles(event.getGuild());
                Object lock = new Object();
                int[] counts = new int[validRoles.size() + 1];
                event.getGuild().loadMembers(m -> 
                {
                    synchronized(lock)
                    {
                        counts[counts.length - 1]++;
                        Role role = getRole(m, validRoles);
                        if(m.getRoles().contains(role))
                            counts[validRoles.indexOf(role)]++;
                    }
                }).onSuccess(v -> 
                {
                    synchronized(lock)
                    {
                        StringBuilder sb = new StringBuilder("`Members` - `" + counts[counts.length - 1] + "`");
                        for(int i = 0; i < validRoles.size(); i++)
                            sb.append("\n`").append(validRoles.get(i).getName()).append("` - `").append(counts[i]).append("`");
                        event.getChannel().sendMessage(sb.toString()).queue();
                    }
                });
            }
        }
    }
    
    private void addRole(Member member)
    {
        // first, use the config for roles
        List<Role> validRoles = getValidRoles(member.getGuild());
        
        // if the config didn't have any valid roles for this guild
        // then we skip
        if(validRoles.isEmpty())
            return;
        
        Role role = getRole(member, validRoles);
        if(!member.getRoles().contains(role))
            member.getGuild().addRoleToMember(member, role).queue();
    }
    
    private List<Role> getValidRoles(Guild guild)
    {
        return roles.stream()
                .map(id -> guild.getRoleById(id))
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }
    
    private Role getRole(Member member, List<Role> validRoles)
    {
        int hash = (int) (member.getUser().getTimeCreated().toEpochSecond() % validRoles.size());
        return validRoles.get(hash);
    }
}
