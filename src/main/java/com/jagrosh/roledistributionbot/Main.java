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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.EnumSet;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        Config config = ConfigFactory.load();
        JDABuilder.createLight(config.getString("bot-token"), GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new Listener(config.getString("command"), config.getLongList("roles")))
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .disableCache(EnumSet.allOf(CacheFlag.class))
                .setChunkingFilter(ChunkingFilter.NONE)
                .build();
    }
}
