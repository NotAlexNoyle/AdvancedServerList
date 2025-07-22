/*
 * MIT License
 *
 * Copyright (c) 2022-2023 Andre_601
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package ch.andre601.advancedserverlist.paper.objects.impl;

import ch.andre601.advancedserverlist.api.bukkit.objects.BukkitPlayer;
import ch.andre601.advancedserverlist.core.objects.CachedPlayer;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public record PaperPlayerImpl(@Nullable OfflinePlayer player, CachedPlayer cachedPlayer, int protocol) implements BukkitPlayer{
    
    @Override
    public String getName(){
        return player == null || player.getName() == null ? cachedPlayer.name() : player.getName();
    }
    
    @Override
    public int getProtocol(){
        return protocol;
    }
    
    @Override
    public UUID getUUID(){
        return player == null ? cachedPlayer.uuid() : player.getUniqueId();
    }
    
    @Override
    public @Nullable OfflinePlayer getPlayer(){
        return player;
    }
    
    @Override
    public boolean hasPlayedBefore(){
        return player != null && player.hasPlayedBefore();
    }
    
    @Override
    public boolean isBanned(){
        return player != null && player.isBanned();
    }
    
    @Override
    public boolean isWhitelisted(){
        return player != null && player.isWhitelisted();
    }
}
