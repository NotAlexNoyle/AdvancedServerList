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

package ch.andre601.advancedserverlist.core.compat.papi;

import ch.andre601.advancedserverlist.core.objects.ValueCache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.william278.papiproxybridge.api.PlaceholderAPI;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PAPIUtil{
    
    private static PAPIUtil instance;
    
    private final ValueCache<CompletableFuture<Set<String>>> serversCache = new ValueCache<>(Duration.ofSeconds(10));
    private final Cache<CacheKey, CompletableFuture<String>> textCache = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(10))
        .build();
    private final PlaceholderAPI papi;
    
    private PAPIUtil(){
        this.papi = PlaceholderAPI.createInstance();
    }
    
    public static PAPIUtil get(){
        if(instance != null)
            return instance;
        
        return (instance = new PAPIUtil());
    }
    
    // Make sure the version of PAPIProxyBridge we get has the required methods we need...
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isCompatible(){
        try{
            papi.getClass().getMethod("getServers");
            return true;
        }catch(NoSuchMethodException ex){
            return false;
        }
    }
    
    public String getServer(){
        Set<String> values = serversCache.get(papi::getServers).getNow(Collections.emptySet());
        if(values.isEmpty())
            return null;
        
        return List.copyOf(values).getFirst();
    }
    
    public <P> P getPlayer(Collection<P> players){
        if(players.isEmpty())
            return null;
        
        return List.copyOf(players).getFirst();
    }
    
    public String parse(String text, UUID carrier, UUID player){
        try{
            return textCache.get(new CacheKey(player, text), () -> papi.formatPlaceholders(text, carrier, player)).getNow(text);
        }catch(ExecutionException ex){
            return text;
        }
    }
    
    private record CacheKey(UUID uuid, String text){}
}
