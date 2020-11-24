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
import net.dv8tion.jda.api.Permission;
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
    private final String command;
    private final List<Long> roles;
    
    public Listener(String command, List<Long> roles)
    {
        this.command = command;
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
                && event.getMessage().getContentRaw().equalsIgnoreCase(command)
                && event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES))
        {
            event.getMessage().addReaction("\u23F1").queue(); // ⏱
            event.getGuild().loadMembers(m -> addRole(member))
                    .onSuccess(v -> event.getMessage().addReaction("\u2705").queue()); // ✅
        }
    }
    
    private void addRole(Member member)
    {
        int hash = (int) (member.getUser().getTimeCreated().toEpochSecond() % roles.size());
        Role role = member.getGuild().getRoleById(roles.get(hash));
        if(!member.getRoles().contains(role))
            member.getGuild().addRoleToMember(member, role).queue();
    }
}
